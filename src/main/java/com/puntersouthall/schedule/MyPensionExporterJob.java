package com.puntersouthall.schedule;

import com.puntersouthall.model.PunterSouthallCustomModel;
import com.puntersouthall.webservice.alfresco.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springmodules.workflow.jbpm31.JbpmFactoryLocator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyPensionExporterJob implements StatefulJob {
	
	final Logger logger = Logger.getLogger(MyPensionExporterJob.class);
	private String outputLocation;
	
	public void setOutputLocation(String outputLocation) {
		this.outputLocation = outputLocation;
	}
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		AuthenticationUtil.runAsSystem(new RunAsWork<String>() { 
			public String doWork() {
				logger.info("Mypension exported started.");	
				ServiceRegistry serviceRegistry = getServiceRegistry();
				NodeService nodeService = serviceRegistry.getNodeService();
				SearchService searchService = serviceRegistry.getSearchService();
				ContentService contentService = serviceRegistry.getContentService();
				StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");			

				SimpleDateFormat dateFormatAlfresco = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat dateFormatMyPension = new SimpleDateFormat("dd-MM-yyyy");
				Date today = new Date();
				
				String query = "@psdoc\\:webDoc:true AND @cm\\:modified:\"" + dateFormatAlfresco.format(today) + "\"";	
				
				SearchParameters sp = new SearchParameters();
				sp.setLanguage(SearchService.LANGUAGE_LUCENE);
				sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
				sp.setMaxItems(-1);
				sp.setLimit(0);
				sp.setMaxPermissionChecks(300000);
				sp.setMaxPermissionCheckTimeMillis(1);
				sp.setLimitBy(LimitBy.UNLIMITED);
				sp.setQuery(query);
				logger.debug("Search params : " + sp.toString());
				ResultSet rs = searchService.query(sp);		
				logger.debug("Number of documents found : " + rs.length());
				
				try {
				    if (rs.length() != 0) {					    	
				    	
				    	for (int x = 0; x < rs.length(); x++) {				    		
				    		NodeRef documentNodeRef = rs.getNodeRef(x);
				    		ContentData contentData = (ContentData) nodeService.getProperty(documentNodeRef, ContentModel.PROP_CONTENT);
				    	    String mimeType = contentData.getMimetype();
				    	    
				    		if(mimeType.equals(MimetypeMap.MIMETYPE_PDF)) {
				    			Integer centralID = (Integer) nodeService.getProperty(documentNodeRef, PunterSouthallCustomModel.PROP_CENTRAL_ID);
				    			Integer memberID = (Integer) nodeService.getProperty(documentNodeRef, PunterSouthallCustomModel.PROP_MEMBER_RECORD_ID);
				    			Integer schemeID = (Integer) nodeService.getProperty(documentNodeRef, PunterSouthallCustomModel.PROP_SCHEME_ID);
					    	    String name = (String) nodeService.getProperty(documentNodeRef, ContentModel.PROP_NAME);
					    	    // mypension database removes any .pdf in the filename so need to do the same otherwise link on website wont work
					    	    name = name.replaceAll("(?i)\\.PDF","");
					    	    String webDocType = (String) nodeService.getProperty(documentNodeRef, PunterSouthallCustomModel.PROP_WEB_DOC_TYPE);
					    	    Date docDate = (Date) nodeService.getProperty(documentNodeRef, PunterSouthallCustomModel.PROP_DOC_DATE);
					    	    
					    	    PunterSouthallService psService = new PunterSouthallService();
								IPunterSouthallService iPSService = psService.getBasicHttpBindingIPunterSouthallService();
								
								String outputFolderPath = outputLocation;
									
					    	    if (centralID != null) {
					    	    	String memberTypeString = "{ \"CentralMemberRecordID\" : " + String.valueOf(centralID) + ", \"DatabaseMemberID\" : " + String.valueOf(memberID) + " }";

					    	    	ArrayOfMemberType memberDetails = iPSService.getMemberDetails(memberTypeString);
					    	    	// 	Will only be one member for a single central ID and Database ID
					    	    	MemberType memberType = memberDetails.getMemberType().get(0);
					    	    	outputFolderPath = outputFolderPath.concat(createMemberFolderPath(memberType.getPenScopeSchemeID().getValue(), memberType.getDatabaseMemberID().getValue()));
					    	    }
					    	    else if (schemeID != null) {
					    	    	String schemeTypeString = "{ \"SchemeID\" : " + String.valueOf(schemeID) + " }";
					    	    	ArrayOfSchemeType schemeDetails = iPSService.getSchemeDetails(schemeTypeString);
					    	    	// 	Should only be one scheme for a single CMDB scheme ID 
					    	    	SchemeType schemeType = schemeDetails.getSchemeType().get(0);
					    	    	String penScopeIDString = schemeType.getSystemSchemeID().getValue();
					    	    	Integer penScopeID = Integer.parseInt(penScopeIDString);
					    	    	outputFolderPath = outputFolderPath.concat(createSchemeFolderPath(penScopeID, webDocType));
					    	    }
					    	    File outputFile = new File(outputFolderPath);
							    outputFile.mkdirs();
					    	    String outputFilePath = outputFolderPath + name + "_" + webDocType + "_" + dateFormatMyPension.format(docDate) + ".pdf";
					    	    createFile(contentService, documentNodeRef, outputFilePath);
				    		}
				    	}					    	
				    }					    
				}
				catch (Exception ex){
					logger.error("Document export failed");
					logger.error(ex.getMessage());
				}
				finally {
					logger.info("Mypension document export complete");
				    rs.close();					    
				}													
				
				return "ok";
			}						
		});
	}	
	
	private String createMemberFolderPath(Integer schemeID, Integer memberID) {
		return schemeID.toString() + "\\Members\\" + memberID.toString() + "\\";
	}
	
	private String createSchemeFolderPath(Integer schemeID, String webDocType) {
		String returnPath = schemeID.toString();
		if (webDocType.equals("Form"))
			returnPath = returnPath.concat("\\Forms\\");
		else
			returnPath = returnPath.concat("\\Documents\\");
		
		return returnPath;
	}
	
	private void createFile(ContentService contentService, NodeRef documentNodeRef, String outputFilePath) {
		try {
			ContentReader reader = contentService.getReader(documentNodeRef, ContentModel.PROP_CONTENT);
		    InputStream originalInputStream = reader.getContentInputStream();
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    final int BUF_SIZE = 1 << 8; //1KiB buffer
		    byte[] buffer = new byte[BUF_SIZE];
		    int bytesRead = -1;
		    while((bytesRead = originalInputStream.read(buffer)) > -1) {
		    	outputStream.write(buffer, 0, bytesRead);
		    }
		    originalInputStream.close();
		    byte[] binaryData = outputStream.toByteArray();		    
		    FileOutputStream out = new FileOutputStream(outputFilePath);
		    out.write(binaryData);		    
		    out.close();
		    logger.info("File exported : " + outputFilePath);
		}
		catch (Exception ex){
			logger.error("Document not exported");
			logger.error(ex.getMessage());
		}
		finally {
		}	
	}
	
	private ServiceRegistry getServiceRegistry(){
		BeanFactoryLocator factoryLocator = new JbpmFactoryLocator();
		BeanFactoryReference factoryReference = factoryLocator.useBeanFactory(null);
		BeanFactory factory = factoryReference.getFactory();
		return (ServiceRegistry) factory.getBean(ServiceRegistry.SERVICE_REGISTRY);
	}
}
