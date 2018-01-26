package com.puntersouthall.schedule;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.importer.FileImporterException;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.VmShutdownListener.VmShutdownException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class imports documents from the file system and saves them to the repository
 * Its primary use is to imported scanned documents from a network drive
 * 
 * @author pedwards
 *
 */
public class PSGDocumentImporter {
	private static Log logger = LogFactory.getLog(PSGDocumentImporter.class);
	private NodeRef logFileNodeRef;
	private String officeLocations;
	private ServiceRegistry serviceRegistry;
	private FileFolderService fileFolderService;
	private SearchService searchService;
	private NodeService nodeService;
	private ContentService contentService;
	private JobLockService jobLockService;
	private String repositoryRoot;
	private String fileSystemRoot;
	private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "PSGDocumentImporterJob"); 
	private static final long LOCK_TTL = 30000L;

	//************************************************************************************************
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
		this.fileFolderService = serviceRegistry.getFileFolderService();
		this.searchService = serviceRegistry.getSearchService();
		this.nodeService = serviceRegistry.getNodeService();
		this.contentService = serviceRegistry.getContentService();
		this.jobLockService = serviceRegistry.getJobLockService();
	}
	
	public ServiceRegistry getServiceRegistry() {
		return this.serviceRegistry;
	}
	//************************************************************************************************
	/**
	 * Acquire the job lock
	 */
	private String acquireLock(JobLockRefreshCallback lockCallback)
	{
		// Get lock
		String lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL);

		// Register the refresh callback which will keep the lock alive
		jobLockService.refreshLock(lockToken, LOCK_QNAME, LOCK_TTL, lockCallback);

		if (logger.isDebugEnabled())
		{
			logger.debug("lock acquired: " + LOCK_QNAME + ": " + lockToken);
		}

		return lockToken;
	}
	//************************************************************************************************
	/**
	 * Release the lock after the job completes
	 */
	private void releaseLock(LockCallback lockCallback, String lockToken)
	{
		if (lockCallback != null)
		{
			lockCallback.running.set(false);
		}

		if (lockToken != null)
		{
			jobLockService.releaseLock(lockToken, LOCK_QNAME);
			if (logger.isDebugEnabled())
			{
				logger.debug("Lock released: " + LOCK_QNAME + ": " + lockToken);
			}
		}
	}
	//************************************************************************************************
	private class LockCallback implements JobLockRefreshCallback
	{
		final AtomicBoolean running = new AtomicBoolean(true);

		@Override
		public boolean isActive()
		{
			return running.get();
		}

		@Override
		public void lockReleased()
		{
			running.set(false);
			if (logger.isDebugEnabled())
			{
				logger.debug("Lock release notification: " + LOCK_QNAME);
			}
		}
	}

	//************************************************************************************************
	public void setOfficeLocations(String officeLocations) {
		this.officeLocations = officeLocations;
	}
	
	//************************************************************************************************
	public void setRepositoryRoot(String repositoryRoot) {
		this.repositoryRoot = repositoryRoot;
	}
	
	//************************************************************************************************
	public void setFileSystemRoot(String filesystemRoot) {
		this.fileSystemRoot = filesystemRoot;
	}
	
	//************************************************************************************************
	/**
	 * Executer implementation
	 */
	public void execute() {
		logger.debug("Running the PSG Document Importer scheduled job");
		LockCallback lockCallback = new LockCallback();
		String lockToken = null;
		try
		{
			lockToken = acquireLock(lockCallback);	
			// list of file import mapping
			// eg : Map<location of input file outside alfresco, lucene search to find folder location within Alfresco to put file>
			Map<String, String> uploadPathNames = new HashMap<String,String>();
			String[] offices = officeLocations.split(",");
			logger.debug("Offices = " + offices);
			logger.debug("repositoryRoot = " + repositoryRoot);
			logger.debug("fileSystemRoot  = " + fileSystemRoot);
			for (int i=0; i<offices.length; i++) {
				String fileSystemPath = fileSystemRoot.concat("/"+offices[i]);
				String repoPath="PATH:\"/app:company_home"+(repositoryRoot.replace("/","/cm:") + "/cm:"+offices[i]+"\"");
				repoPath = repoPath.replace(" " ,"_x0020_");
				logger.debug("Adding fileSystemPath : " + fileSystemPath);
				logger.debug("Adding repoPath : " + repoPath);
				uploadPathNames.put(fileSystemPath, repoPath);
			}
			
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
		           logger.debug("Found destination folder node ref for " + key);
		       }
		       finally
		       {
		           rs.close();
		       }
		       NodeRef documentLibrary = searchResultNodeRef;							
			
				File uploadPath = new File(key);
				
				if (uploadPath.isDirectory()) {
				    logger.debug("Found source folder on file system : " + key);
					
					File[] files = uploadPath.listFiles();
					if (files != null) {
                        Arrays.sort(files, new Comparator<Object>() {
                            public int compare(Object o1, Object o2) {

                                if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                                    return -1;
                                } else if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                                    return +1;
                                } else {
                                    return 0;
                                }
                            }

                        });
                    }
					logger.debug("Found " + files.length + " files to upload from " + key);
					for (File file : files) {
					    String officeName  = key.substring(key.lastIndexOf('/')+1);
						NodeRef uploadedFile = createFile(serviceRegistry, documentLibrary, file, officeName );
						String fileName = file.getName();
						
						if (uploadedFile == null) { 
							logger.error("File not uploaded : " + fileName);
						}
						else {							
							logger.info("File uploaded : " + fileName);
							
							if(file.delete())
								logger.info("File deleted : " + fileName);
							else
								logger.error("File not deleted : " + fileName);
						}
					}
				}
				else {
				    logger.warn("No source folder found on file system : " + key);
                }
			}
		}
		catch (LockAcquisitionException e)
		{
			// Job being done by another process
			if (logger.isDebugEnabled())
			{
				logger.error("Document Import already underway.");
				logger.error(e.getMessage());
			}
		}
		catch (VmShutdownException e)
		{
			// Aborted
			if (logger.isDebugEnabled())
			{
				logger.error("Document Import aborted.");
				logger.error(e.getMessage());
			}
		}
		finally
		{
			releaseLock(lockCallback, lockToken);
			logger.debug("Document Import Job Lock released");
		}
	}
	
	//************************************************************************************************
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
	
	//************************************************************************************************
	private NodeRef createFile(ServiceRegistry serviceRegistry, NodeRef parentNodeRef, File file, String officeName) {
		
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

        // create the node to represent the file
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
        titledProps.put(ContentModel.PROP_DESCRIPTION, officeName);

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
        catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
		}
        
        return fileNodeRef;	
	}
}
