package com.puntersouthall.webscripts;

import com.puntersouthall.helper.AuthorityHelper;
import com.puntersouthall.helper.NodeServiceHelper;
import com.puntersouthall.webservice.alfresco.*;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;

;

public class CreateClientFolder extends AbstractWebScript
{
	private SearchService searchService;
	private NodeService nodeService;
	private ContentService contentService;
	private PermissionService permissionService;
	private AuthorityService authorityService;
	
	 static Logger  log = Logger.getLogger(CreateClientFolder.class);
	
	 /**
     * Inject the Alfresco service registry
     * 
     * @param serviceRegistry Alfresco service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.searchService = serviceRegistry.getSearchService();
        this.nodeService = serviceRegistry.getNodeService();
        this.contentService = serviceRegistry.getContentService();
        this.permissionService = serviceRegistry.getPermissionService();
        this.authorityService = serviceRegistry.getAuthorityService();
    }
    
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
    	//set variables
    	NodeRef adminDocsFolderNodeRef;
    	NodeRef cashieringDocsFolderNodeRef;
    	NodeRef payrollDocsFolderNodeRef;
    	String schemeFolderName = "";
    	String currentClientID = "";
    	String myClientName = "";
    	
    	//read Client ID that has been input
    	final String myClientID = req.getParameter("clientID");
    	final String buildAll = req.getParameter("buildAll");
    	res.getWriter().write("Input: " + myClientID + "\n Build all?" + buildAll + "\n \n");
    	AuthenticationUtil.setRunAsUserSystem();
    	
    	//get object with all client ID's and names
    	PunterSouthallService psService = new PunterSouthallService();
    	IPunterSouthallService iPSService = psService.getBasicHttpBindingIPunterSouthallService();
    	ArrayOfClientType clientArray = iPSService.getClientNames();
    	
    	
    	//get Client Name for ID to create folders for
    	for (ClientType thisClient : clientArray.getClientType()) {
    		if(thisClient.getClientID().toString().equals(myClientID) || buildAll.equals("true")){
    			myClientName = thisClient.getClientName().getValue();
    			currentClientID = thisClient.getClientID().toString();
    			res.getWriter().write("Client is: " + currentClientID + " - " + myClientName + "\n");
    		
  
		    	//get list of Admin and Payroll schemes for this client
		    	ArrayOfSchemeType adminSchemeArray = iPSService.getSchemeNamesForClient(myClientName, "Admin");
		    	ArrayOfSchemeType payrollSchemeArray = iPSService.getSchemeNamesForClient(myClientName, "Payroll");
		
		    	//get nodeRef for 'Clients' folder
		    	NodeRef clientsParentNodeRef = getClientsFolder(searchService);
		    	//Create parent Client folder
		    	String clientFolderName = currentClientID + " - " + myClientName;
		    	
		    	try{
		    		NodeRef clientFolderNodeRef = NodeServiceHelper.createChildNode(nodeService,clientsParentNodeRef, clientFolderName, ContentModel.TYPE_FOLDER);
		    		
		    		if (clientFolderNodeRef != null){
		    			res.getWriter().write(clientFolderName + " - Client Folder successfully created \n");
		    			
		    	    	adminDocsFolderNodeRef = NodeServiceHelper.createChildNode(nodeService,clientFolderNodeRef, "Admin Documents", ContentModel.TYPE_FOLDER);
		    	    	cashieringDocsFolderNodeRef = NodeServiceHelper.createChildNode(nodeService,clientFolderNodeRef, "Cashiering Documents", ContentModel.TYPE_FOLDER);
		    	    	payrollDocsFolderNodeRef = NodeServiceHelper.createChildNode(nodeService,clientFolderNodeRef, "Payroll Documents", ContentModel.TYPE_FOLDER);
		    	    	
		    	    	//get authority
		    	    	String adminReadOnlyAuthority = "ALF-Client-" + currentClientID + "-Admin-ReadOnly";
		    	    	String adminFullAuthority = "ALF-Client-" + currentClientID + "-Admin-FullAccess";
		    	    	String cashieringReadOnlyAuthority = "ALF-Client-" + currentClientID + "-Cashiering-ReadOnly";
		    	    	String cashieringFullAuthority = "ALF-Client-" + currentClientID + "-Cashiering-FullAccess";
		    	    	String payrollReadOnlyAuthority = "ALF-Client-" + currentClientID + "-Payroll-ReadOnly";
		    	    	String payrollFullAuthority = "ALF-Client-" + currentClientID + "-Payroll-FullAccess";
		    	    	
		    	    	//check if authority exists and create authority if it doesn't exist
		    	    	 AuthorityHelper.createAuthority(log, authorityService, adminReadOnlyAuthority);
		    	    	 AuthorityHelper.createAuthority(log, authorityService, adminFullAuthority);
		    	    	 AuthorityHelper.createAuthority(log, authorityService, cashieringReadOnlyAuthority);
		    	    	 AuthorityHelper.createAuthority(log, authorityService, cashieringFullAuthority);
		    	    	 AuthorityHelper.createAuthority(log, authorityService, payrollReadOnlyAuthority);
		    	    	 AuthorityHelper.createAuthority(log, authorityService, payrollFullAuthority);
		    	    	
		    	    	//add security settings
		    	    	permissionService.setInheritParentPermissions(adminDocsFolderNodeRef, false);		    	
		    	    	permissionService.setPermission(adminDocsFolderNodeRef, "GROUP_" + adminReadOnlyAuthority, PermissionService.CONTRIBUTOR, true);
		    	    	permissionService.setPermission(adminDocsFolderNodeRef, "GROUP_" + adminFullAuthority, PermissionService.COORDINATOR, true);
		    	    	permissionService.setInheritParentPermissions(cashieringDocsFolderNodeRef, false);
		    	    	permissionService.setPermission(cashieringDocsFolderNodeRef, "GROUP_" + cashieringReadOnlyAuthority, PermissionService.CONTRIBUTOR, true);
		    	    	permissionService.setPermission(cashieringDocsFolderNodeRef, "GROUP_" + cashieringFullAuthority, PermissionService.COORDINATOR, true);
		    	    	permissionService.setInheritParentPermissions(payrollDocsFolderNodeRef, false);
		    	    	permissionService.setPermission(payrollDocsFolderNodeRef, "GROUP_" + payrollReadOnlyAuthority, PermissionService.CONSUMER, true);
		    	    	permissionService.setPermission(payrollDocsFolderNodeRef, "GROUP_" + payrollFullAuthority, PermissionService.CONTRIBUTOR, true);
		    	    	
				    	//Create Scheme folders for Admin and Cashiering		  
				        //loop through admin Scheme names and create folders
				    	for ( SchemeType thisScheme : adminSchemeArray.getSchemeType()) {
				    		schemeFolderName = thisScheme.getSchemeID().toString() + " - " + thisScheme.getSchemeName().getValue();
				    		NodeRef adminSchemeFolderNodeRef = NodeServiceHelper.createChildNode(nodeService,adminDocsFolderNodeRef, schemeFolderName, ContentModel.TYPE_FOLDER);
				    		NodeRef cashieringSchemeFolderNodeRef = NodeServiceHelper.createChildNode(nodeService,cashieringDocsFolderNodeRef, schemeFolderName, ContentModel.TYPE_FOLDER);
				    		
				    		NodeRef adminSchemeFolderMemberDocsNodeRef = NodeServiceHelper.createChildNode(nodeService,adminSchemeFolderNodeRef, "Member Documents", ContentModel.TYPE_FOLDER);
				    		NodeRef adminSchemeFolderSchemeDocsNodeRef = NodeServiceHelper.createChildNode(nodeService,adminSchemeFolderNodeRef, "Scheme Documents", ContentModel.TYPE_FOLDER);

				    		NodeRef cashieringSchemeFolderMemberDocsNodeRef = NodeServiceHelper.createChildNode(nodeService,cashieringSchemeFolderNodeRef, "Member Documents", ContentModel.TYPE_FOLDER);
				    		NodeRef cashieringSchemeFolderSchemeDocsNodeRef = NodeServiceHelper.createChildNode(nodeService,cashieringSchemeFolderNodeRef, "Scheme Documents", ContentModel.TYPE_FOLDER);

				    	} 
				    	
				    	//Create Scheme folders for Payroll	
				    	schemeFolderName = "";
				        //loop through payroll Scheme names and create folders
				    	for ( SchemeType thisScheme : payrollSchemeArray.getSchemeType()) {
				    		schemeFolderName = thisScheme.getSchemeID().toString() + " - " + thisScheme.getSchemeName().getValue();
				    		NodeRef payrollSchemeFolderNodeRef = NodeServiceHelper.createChildNode(nodeService,payrollDocsFolderNodeRef, schemeFolderName, ContentModel.TYPE_FOLDER);
				    	
				    		NodeRef payrollSchemeFolderMemberDocsNodeRef = NodeServiceHelper.createChildNode(nodeService,payrollSchemeFolderNodeRef, "Member Documents", ContentModel.TYPE_FOLDER);
				    		NodeRef payrollSchemeFolderSchemeDocsNodeRef = NodeServiceHelper.createChildNode(nodeService,payrollSchemeFolderNodeRef, "Scheme Documents", ContentModel.TYPE_FOLDER);

				    	} 
				    	
		    	    	//create unknown scheme for payroll clients
		    	    	NodeRef payrollUnknownsFolderNodeRef = NodeServiceHelper.createChildNode(nodeService,payrollDocsFolderNodeRef, "0 - Unknown - Check Legacy Fields", ContentModel.TYPE_FOLDER);
			    		NodeRef payrollSchemeFolderMemberDocsNodeRef = NodeServiceHelper.createChildNode(nodeService,payrollUnknownsFolderNodeRef, "Member Documents", ContentModel.TYPE_FOLDER);
			    		NodeRef payrollSchemeFolderSchemeDocsNodeRef = NodeServiceHelper.createChildNode(nodeService,payrollUnknownsFolderNodeRef, "Scheme Documents", ContentModel.TYPE_FOLDER);

		    		}
		    			   		
	        	}
		    	finally{
		    	}
    		}    	
    	}   	
    }    
    

    
    
	//returns noderef for Company home\Clients
    public static NodeRef getClientsFolder(SearchService searchService) {
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home/cm:Clients\"");
        NodeRef clientsNodeRef = null;
        try {
            if (rs.length() == 0) {
                throw new AlfrescoRuntimeException("Didn't find Clients Folder");
            }
            clientsNodeRef = rs.getNodeRef(0);
        } finally {
            rs.close();
        }
        return clientsNodeRef;
    }
}
