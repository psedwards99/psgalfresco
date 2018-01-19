package com.puntersouthall.actions;

import com.puntersouthall.helper.NodeServiceHelper;
import com.puntersouthall.helper.PSErrorLoggingHelper;
import com.puntersouthall.model.PunterSouthallCustomModel;
import com.puntersouthall.model.PunterSouthallFormsModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;


public class DocumentFilerAction extends ActionExecuterAbstractBase {
	
    /** the logger */
    private final Log logger = LogFactory.getLog(DocumentFilerAction.class);

    /** Alfresco services */
    private ServiceRegistry serviceRegistry;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private SearchService searchService;
    private AuthenticationService authenticationService;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
        this.fileFolderService = serviceRegistry.getFileFolderService();
        this.searchService = serviceRegistry.getSearchService();
        this.authenticationService = serviceRegistry.getAuthenticationService();
    }

    @Override
    protected void executeImpl(Action action, final NodeRef actionedUponNodeRef) {

        // Run under the system admin context so that we can launch the workflows ok within Alfresco
        AuthenticationUtil.runAsSystem(new RunAsWork<String>() {
            public String doWork() {
                
            	// get logged in user
            	String loggedInUser = authenticationService.getCurrentUserName();
            	
            	QName documentType =  nodeService.getType(actionedUponNodeRef);
            	
            	Integer clientID = (Integer) nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_CLIENT_ID);
            	String clientName = (String) nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_CLIENT_NAME);
            	Integer schemeID = (Integer) nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_SCHEME_ID);
            	String schemeName = (String) nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_SCHEME_NAME);
            	Integer memeberRecordID = (Integer) nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_MEMBER_RECORD_ID);
            	String activity = (String) nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_ACTIVITY);
            	

            	String departmentFolder = "";
            	
            	
            	/*logger.debug("Test Client ID: " + clientID);
            	logger.debug("Test Client Name: " + clientName);
            	logger.debug("Test Scheme ID: " + schemeID);
            	logger.debug("Test Scheme Name: " + schemeName);
            	logger.debug("Test Member Record ID: " + memeberRecordID);
            	logger.debug("Test Activity: " + activity);
            	logger.debug("Test Cashiering Type: " + cashieringType);
            	logger.debug("Docuemnt Type: " + documentType);*/
            	
            	if (documentType.equals(PunterSouthallCustomModel.TYPE_ADMIN_SCHEME_DOCUMENT) || documentType.equals(PunterSouthallCustomModel.TYPE_ADMIN_MEMBER_DOCUMENT) || documentType.equals(PunterSouthallCustomModel.TYPE_ADMIN_SCHEME_CONTROL_DOCUMENT) ){
            		logger.debug("Document is admin document type");
            		departmentFolder = "Admin Documents";
            		
            	}
            	else if (documentType.equals(PunterSouthallCustomModel.TYPE_PAYROLL_SCHEME_DOCUMENT) || documentType.equals(PunterSouthallCustomModel.TYPE_PAYROLL_MEMBER_DOCUMENT) ){
            		logger.debug("Document is payroll document type");
            		departmentFolder = "Payroll Documents";
            		activity = (String) nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_SUB_ACTIVITY);
            	}
            	else if (documentType.equals(PunterSouthallCustomModel.TYPE_CASHIERING_SCHEME_DOCUMENT) || documentType.equals(PunterSouthallCustomModel.TYPE_CASHIERING_MEMBER_DOCUMENT)){
            		logger.debug("Document is cashiering document type");
                	ArrayList<String> cashieringType =  (ArrayList<String>) nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_TYPE);
                	activity = cashieringType.get(0);
            		departmentFolder = "Cashiering Documents";            		
            	}
            	else if (documentType.equals(PunterSouthallFormsModel.TYPE_NEW_PAYROLL_STARTER_FORM)){
            		logger.debug("Document is admin document type");
            		departmentFolder = "Admin Documents";
            	}
            		
            	else{
            		// call error method and write to log file
            		PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(), "Document couldn't be filed, unexpected document type " + documentType + " - Document moved to filing errors folder");
            		NodeServiceHelper.moveToErrorFolder(nodeService, searchService, actionedUponNodeRef, "Filing Errors");
       			 	return "Failed";
            	}          	        	
            	
            	StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

            	//ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home/cm:Uploads\"");
            	ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,"+PATH:\"/app:company_home/cm:Clients/*\" AND @cm\\:name:\"" + clientID + "\" AND TYPE:\"cm:folder\"");
            	
            	// find the client folder
            	NodeRef searchResultNodeRef = null;
            	String searchResultFolderName = null; 
            	try{
            		if (rs.length() == 1){
            			searchResultNodeRef = rs.getNodeRef(0);
            			if (logger.isDebugEnabled()){
                		logger.debug("SUCCESS - Search result node (1 unique result) : " + searchResultNodeRef + "  -  " + nodeService.getProperty(searchResultNodeRef, ContentModel.PROP_NAME));  
            			}
            		}
            		else{          		
            			// call error method and write to log file
            			if (logger.isDebugEnabled()){
            			logger.debug("search failed, number of results from search : " + rs.length());
            			}
            			 for(ResultSetRow row : rs)
            	            {
            	                NodeRef currentNodeRef = row.getNodeRef();
            	                if (logger.isDebugEnabled()){
            	                logger.debug(currentNodeRef +"  -  " + row.getValue(ContentModel.PROP_NAME));
            	                }
            	            }
            			 PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(), "Unique Client folder could not be found for client ID: " + clientID + " number of folders found: " + rs.length() + " - Document moved to filing errors folder");
            			 NodeServiceHelper.moveToErrorFolder(nodeService, searchService, actionedUponNodeRef, "Filing Errors");
            			 return "Failed";
            		}
            	}
            	finally{
				     rs.close();
            	}
            	// file at this stage if a scheme control file
        		if (documentType.equals(PunterSouthallCustomModel.TYPE_ADMIN_SCHEME_CONTROL_DOCUMENT)){
        			//get the "Admin Documents Folder" it will already exist
        			NodeRef adminDocumentsFolder = NodeServiceHelper.createChildNode(nodeService,searchResultNodeRef, "Admin Documents",ContentModel.TYPE_FOLDER);
        			NodeRef schemeControlFolder = NodeServiceHelper.createChildNode(nodeService, adminDocumentsFolder, "Scheme Control Documents", ContentModel.TYPE_FOLDER);
            		nodeService.moveNode(actionedUponNodeRef, schemeControlFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) nodeService.getProperty(actionedUponNodeRef,ContentModel.PROP_NAME)));
            		if (logger.isDebugEnabled()){
            		logger.debug("path of moved file - " + nodeService.getPath(actionedUponNodeRef));
            		}
        			return "OK";
        		}
        	
            	
            	//for normal document types.
            	if (!documentType.equals(PunterSouthallCustomModel.TYPE_ADMIN_SCHEME_CONTROL_DOCUMENT)){
                	// find the relevant system folder 
                	String clientFolderName = (String) nodeService.getProperty(searchResultNodeRef, ContentModel.PROP_NAME);
                	rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,"+PATH:\"/app:company_home/cm:Clients/cm:" + ISO9075.encode(clientFolderName)+ "/cm:" + ISO9075.encode(departmentFolder) + "/*\" AND @cm\\:name:\"" + schemeID + "\" AND TYPE:\"cm:folder\"");
                	try{
                		if (rs.length() == 1){
                			searchResultNodeRef = rs.getNodeRef(0);
                			if (logger.isDebugEnabled()){
                    		logger.debug("SUCCESS - Search result node (1 unique result) : " + searchResultNodeRef + "  -  " + nodeService.getProperty(searchResultNodeRef, ContentModel.PROP_NAME));         			
                			}
            			}
                		else{          		
                			// call error method and write to log file
                			logger.debug("search failed, number of results from search : " + rs.length());
                			 for(ResultSetRow row : rs)
                	            {
                	                NodeRef currentNodeRef = row.getNodeRef();
                	                searchResultNodeRef = null;
                	                if (logger.isDebugEnabled()){
                	                logger.debug(currentNodeRef +"  -  " + row.getValue(ContentModel.PROP_NAME));
                	                }
                	                
                	            }
                			 PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(), "Unique Scheme folder could not be found for scheme ID: " + schemeID + " number of folders found: " + rs.length() + " - Document moved to filing errors folder");
                			 NodeServiceHelper.moveToErrorFolder(nodeService, searchService, actionedUponNodeRef, "Filing Errors");
                			 return "Failed";
                		}
                	}
                	finally{
    				     rs.close();
                	}
            	}

         
            		//create folder
            		if(documentType.equals(PunterSouthallCustomModel.TYPE_ADMIN_SCHEME_DOCUMENT) || documentType.equals(PunterSouthallCustomModel.TYPE_CASHIERING_SCHEME_DOCUMENT) || documentType.equals(PunterSouthallCustomModel.TYPE_PAYROLL_SCHEME_DOCUMENT)){
	            		NodeRef schemeDocumentsNodeRef = NodeServiceHelper.createChildNode(nodeService, searchResultNodeRef, "Scheme Documents", ContentModel.TYPE_FOLDER);
	            		NodeRef activityFolderNodeRef = NodeServiceHelper.createChildNode(nodeService,schemeDocumentsNodeRef, activity, ContentModel.TYPE_FOLDER);
	            		
	            		nodeService.moveNode(actionedUponNodeRef, activityFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) nodeService.getProperty(actionedUponNodeRef,ContentModel.PROP_NAME)));
	            		if (logger.isDebugEnabled()){
	            		logger.debug("path of moved file - " + nodeService.getPath(actionedUponNodeRef));
	            		}
            		}
            		else if(documentType.equals(PunterSouthallCustomModel.TYPE_ADMIN_MEMBER_DOCUMENT)|| documentType.equals(PunterSouthallCustomModel.TYPE_CASHIERING_MEMBER_DOCUMENT) || documentType.equals(PunterSouthallCustomModel.TYPE_PAYROLL_MEMBER_DOCUMENT) || documentType.equals(PunterSouthallFormsModel.TYPE_NEW_PAYROLL_STARTER_FORM)){
            		
            			String memberRangeFolder = getMemberRangeFolderNameFromMemberID(memeberRecordID);
            			String memberFolder = getMemberFolderNameFromMemberID(memeberRecordID);
            			
            			NodeRef schemeDocumentsNodeRef = NodeServiceHelper.createChildNode(nodeService, searchResultNodeRef, "Member Documents", ContentModel.TYPE_FOLDER);
            			
	            		NodeRef memberRangeFolderNodeRef = NodeServiceHelper.createChildNode(nodeService, schemeDocumentsNodeRef, memberRangeFolder, ContentModel.TYPE_FOLDER);
	            		NodeRef memberFolderNodeRef = NodeServiceHelper.createChildNode(nodeService, memberRangeFolderNodeRef, memberFolder, ContentModel.TYPE_FOLDER);
	            		
	            		nodeService.moveNode(actionedUponNodeRef, memberFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) nodeService.getProperty(actionedUponNodeRef,ContentModel.PROP_NAME)));
	            		if (logger.isDebugEnabled()){
	            		logger.debug("path of moved file - " + nodeService.getPath(actionedUponNodeRef));
	            		}
            		}

                return "OK";
            }
        });
    }


    public static String getMemberRangeFolderNameFromMemberID(Integer memberID ){

    	Integer lowerBound = (memberID / 5000) * 5000;
    	Integer upperBound = lowerBound + 4999;
    	String pad = "0000000";
    	String lbString = pad.substring(0, 7 - lowerBound.toString().length() ) + lowerBound.toString();
    	String ubString = pad.substring(0, 7 - upperBound.toString().length() ) + upperBound.toString();
    	String folderRangeName = lbString + " - " + ubString;
    	 	
    	
    	return folderRangeName;
    }
    
   public static String getMemberFolderNameFromMemberID(Integer memberID ){

    	String pad = "0000000";
    	String folderName = pad.substring(0, 7 - memberID.toString().length() ) + memberID.toString();
	
    	return folderName;
    }
    

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// TODO Auto-generated method stub
		
	}
    

}
