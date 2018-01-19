package com.puntersouthall.schedule;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.importer.FileImporterException;
import org.alfresco.repo.importer.FileImporterImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springmodules.workflow.jbpm31.JbpmFactoryLocator;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AutomatedDocumentImporterJob extends FileImporterImpl implements StatefulJob {

	final Logger logger = Logger.getLogger(AutomatedDocumentImporterJob.class);
	
	private String scanningLocation;
	
	public void setScanningLocation(String scanningLocation) {
		this.scanningLocation = scanningLocation;
	}
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		AuthenticationUtil.runAsSystem(new RunAsWork<String>() { 
			public String doWork() {
				logger.debug("document importer started...");
				// list of file import mapping
				// eg : Map<location of input file outside alfresco, lucene search to find folder location within Alfresco to put file>
				Map<String, String> uploadPathNames = new HashMap<String,String>();
				uploadPathNames.put(scanningLocation.concat("/Scanning - Birmingham"), "PATH:\"/app:company_home/cm:Uploads/cm:Scanning/cm:Birmingham/cm:Paper_x0020_Post\"");
				uploadPathNames.put(scanningLocation.concat("/Scanning - Bristol"), "PATH:\"/app:company_home/cm:Uploads/cm:Scanning/cm:Bristol/cm:Paper_x0020_Post\"");
				uploadPathNames.put(scanningLocation.concat("/Scanning - Chelmsford"), "PATH:\"/app:company_home/cm:Uploads/cm:Scanning/cm:Chelmsford/cm:Paper_x0020_Post\"");
				uploadPathNames.put(scanningLocation.concat("/Scanning - Edinburgh"), "PATH:\"/app:company_home/cm:Uploads/cm:Scanning/cm:Edinburgh/cm:Paper_x0020_Post\"");
				uploadPathNames.put(scanningLocation.concat("/Scanning - London"), "PATH:\"/app:company_home/cm:Uploads/cm:Scanning/cm:London/cm:Paper_x0020_Post\"");
				uploadPathNames.put(scanningLocation.concat("/Scanning - Newcastle"), "PATH:\"/app:company_home/cm:Uploads/cm:Scanning/cm:Newcastle/cm:Paper_x0020_Post\"");
				uploadPathNames.put(scanningLocation.concat("/Scanning - Perth"), "PATH:\"/app:company_home/cm:Uploads/cm:Scanning/cm:Perth/cm:Paper_x0020_Post\"");
				uploadPathNames.put(scanningLocation.concat("/Scanning - Wokingham"), "PATH:\"/app:company_home/cm:Uploads/cm:Scanning/cm:Wokingham/cm:Paper_x0020_Post\"");
				// Get registries and services
				ServiceRegistry serviceRegistry = getServiceRegistry();
				SearchService searchService = serviceRegistry.getSearchService();
				
				StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");			
				
				for (String key : uploadPathNames.keySet())
				{	
				// get the noderef based on the search path 	
				   ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, uploadPathNames.get(key));
			       NodeRef searchResultNodeRef = null;
			       try
			       {
			           if (rs.length() != 1)
			           {
			               throw new AlfrescoRuntimeException("Didn't find single folder based on search : " + uploadPathNames.get(key));
			           }
			           searchResultNodeRef = rs.getNodeRef(0);
			       }
			       finally
			       {
			           rs.close();
			       }
			       NodeRef documentLibrary = searchResultNodeRef;							
				
					File uploadPath = new File(key);
					
					if (uploadPath.isDirectory()) {
						
						File[] files = uploadPath.listFiles();
						Arrays.sort( files, new Comparator<Object>()
						{
						    public int compare(Object o1, Object o2) {

						        if (((File)o1).lastModified() < ((File)o2).lastModified()) {
						            return -1;
						        } else if (((File)o1).lastModified() > ((File)o2).lastModified()) {
						            return +1;
						        } else {
						            return 0;
						        }
						    }

						}); 
						
						for (File file : files) {						
							NodeRef uploadedFile = createFile(serviceRegistry, documentLibrary, file);
							String fileName = file.getName();
							
							if (uploadedFile == null) { 
								System.out.println("File not uploaded : " + fileName);
							}
							else {							
								System.out.println("File uploaded : " + fileName);
								
								if(file.delete())
									System.out.println("File deleted : " + fileName);
								else
									System.out.println("File not deleted : " + fileName);
							}
						}
					}									
				}
				return "ok";
			}						
		});
	}	
		
	private QName getAssocTypeQName(ServiceRegistry serviceRegistry, NodeRef parentNodeRef)
    {
		NodeService nodeService = serviceRegistry.getNodeService();
		DictionaryService dictionaryService = serviceRegistry.getDictionaryService();
        // check the parent node's type to determine which association to use
        QName parentNodeTypeQName = nodeService.getType(parentNodeRef);
        QName assocTypeQName = null;
        if (dictionaryService.isSubClass(parentNodeTypeQName, ContentModel.TYPE_CONTAINER))
        {
            // it may be a root node or something similar
            assocTypeQName = ContentModel.ASSOC_CHILDREN;
        }
        else if (dictionaryService.isSubClass(parentNodeTypeQName, ContentModel.TYPE_FOLDER))
        {
            // more like a directory
            assocTypeQName = ContentModel.ASSOC_CONTAINS;
        }
        return assocTypeQName;
    }
	
	private NodeRef createFile(ServiceRegistry serviceRegistry, NodeRef parentNodeRef, File file) {
		
		AuthenticationService authenticationService = serviceRegistry.getAuthenticationService();
		ContentService contentService = serviceRegistry.getContentService();
		MimetypeService mimetypeService = serviceRegistry.getMimetypeService();
		NodeService nodeService = serviceRegistry.getNodeService();
		
		QName assocTypeQName = getAssocTypeQName(serviceRegistry, parentNodeRef);
        if (assocTypeQName == null)
        {
            throw new IllegalArgumentException(
                    "Unable to create file.  " +
                    "Parent type is inappropriate: " + nodeService.getType(parentNodeRef));
        }
        
        // Identify the type of the file
        FileContentReader reader = new FileContentReader(file);
        String mimetype = mimetypeService.guessMimetype(file.getName(), reader);

        // create properties for content type
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>(3, 1.0f);
        contentProps.put(ContentModel.PROP_NAME, file.getName());
        contentProps.put(
                ContentModel.PROP_CONTENT,
                new ContentData(null, mimetype, 0L, "UTF-8"));
        String currentUser = authenticationService.getCurrentUserName();
        contentProps.put(ContentModel.PROP_CREATOR, currentUser == null ? "unknown" : currentUser);

        // create the node to represent the node
        String assocName = QName.createValidLocalName(file.getName());
        ChildAssociationRef assocRef = nodeService.createNode(
                parentNodeRef,
                assocTypeQName,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, assocName),
                ContentModel.TYPE_CONTENT, contentProps);

        NodeRef fileNodeRef = assocRef.getChildRef();
        
        // apply the titled aspect - title and description
        Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(5);
        titledProps.put(ContentModel.PROP_TITLE, file.getName());
        String FilePathStr = file.getPath();
        String ScanningOffice = FilePathStr.substring(FilePathStr.indexOf("Scanning"), FilePathStr.indexOf("\\",FilePathStr.indexOf("Scanning")));
        titledProps.put(ContentModel.PROP_DESCRIPTION, ScanningOffice);

        nodeService.addAspect(fileNodeRef, ContentModel.ASPECT_TITLED, titledProps);

         // get a writer for the content and put the file
        ContentWriter writer = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
        try
        {
            writer.putContent(new BufferedInputStream(new FileInputStream(file)));
        }
        catch (ContentIOException e)
        {
            throw new FileImporterException("Failed to load content from "+file.getPath(), e);
        }
        catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        
        return fileNodeRef;	
	}
	
	private ServiceRegistry getServiceRegistry(){
		BeanFactoryLocator factoryLocator = new JbpmFactoryLocator();
		BeanFactoryReference factoryReference = factoryLocator.useBeanFactory(null);
		BeanFactory factory = factoryReference.getFactory();
		return (ServiceRegistry) factory.getBean(ServiceRegistry.SERVICE_REGISTRY);
	}
}
