package com.puntersouthall.actions;

import com.puntersouthall.helper.NodeServiceHelper;
import com.puntersouthall.helper.PSErrorLoggingHelper;
import com.puntersouthall.model.PunterSouthallCustomModel;
import com.puntersouthall.model.PunterSouthallWorkflowModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaunchWorkflowFromUserDocumentAction extends ActionExecuterAbstractBase {

    /** the logger */
    private final Log logger = LogFactory.getLog(LaunchWorkflowFromUserDocumentAction.class);

    /** workflow properties */
    private String PROP_SOURCE_SYSTEM;
    private String PROP_ADMIN_SCHEME_GROUP;
    private String USER_ATTACHMENTS_TARGET_FOLDER_PATH;
    private String ARCHIVED_EMAIL_FOLDER_PATH;
    private String PROP_BIRM_INXEXING_TEAM;
    private String PROP_BRIS_INXEXING_TEAM;
    private String PROP_EDIN_INXEXING_TEAM;
    private String PROP_LOND_INXEXING_TEAM;
    private String PROP_NEWC_INXEXING_TEAM;
    private String PROP_WOKO_INXEXING_TEAM;

    /** Alfresco services */
    private ServiceRegistry serviceRegistry;
    private NodeService nodeService;
    private WorkflowService workflowService;
    private PersonService personService;
    private FileFolderService fileFolderService;
    private SearchService searchService;
    private ContentService contentService;
    private AuthorityService authorityService;
    private AuthenticationService authenticationService;
    
	
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
        this.workflowService = serviceRegistry.getWorkflowService();
        this.personService = serviceRegistry.getPersonService();
        this.fileFolderService = serviceRegistry.getFileFolderService();
        this.searchService = serviceRegistry.getSearchService();
        this.contentService = serviceRegistry.getContentService();
        this.authorityService = serviceRegistry.getAuthorityService();
        this.authenticationService = serviceRegistry.getAuthenticationService();
    }
    

    public void setPROP_ADMIN_SCHEME_GROUP(String pROP_ADMIN_SCHEME_GROUP) {
        PROP_ADMIN_SCHEME_GROUP = pROP_ADMIN_SCHEME_GROUP;
    }

    public void setUSER_ATTACHMENTS_TARGET_FOLDER_PATH(String uSER_ATTACHMENTS_TARGET_FOLDER_PATH) {
    	USER_ATTACHMENTS_TARGET_FOLDER_PATH = uSER_ATTACHMENTS_TARGET_FOLDER_PATH;
    }
    

    @Override
    protected void executeImpl(Action action, final NodeRef actionedUponNodeRef) {

    	
        // Run under the system admin context so that we can launch the workflows ok within Alfresco
        AuthenticationUtil.runAsSystem(new RunAsWork<String>() {
            public String doWork() {
            	
            	// get logged in user
            	String loggedInUser = authenticationService.getCurrentUserName();
            	
                // Check if the node has the email aspect
                if (nodeService.hasAspect(actionedUponNodeRef, PunterSouthallCustomModel.ASPECT_WORKFLOW)) {
                	if (logger.isDebugEnabled()){
            		logger.debug("Has workflow aspect: " + actionedUponNodeRef);
                	}
            		// The workflow package
                    NodeRef workflowPackage = workflowService.createPackage(null);
                    // Set initiator as the creator
                    NodeRef initiator = personService.getPerson((String) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_CREATOR));
                    Serializable initiatorHomeFolder = nodeService.getProperty(initiator, ContentModel.PROP_HOMEFOLDER);
                    
                    // Get the target folder noderef where move the attachments
                    NodeRef targetFolderNoderef = null;
                    List<String> pathElements = Arrays.asList(StringUtils.split(USER_ATTACHMENTS_TARGET_FOLDER_PATH, '/'));

                    FileInfo fileInfo = null;
                    try {
                    	
                        fileInfo = fileFolderService.resolveNamePath(NodeServiceHelper.getCompanyHome(searchService), pathElements);
                        // Get the target folder noderef
                        targetFolderNoderef = fileInfo.getNodeRef();
                        if (logger.isDebugEnabled()){
                        logger.debug("Attachments folder : "+targetFolderNoderef);
                        }
                        // Add the document to the workflow package
                        ChildAssociationRef childAssoc = nodeService.getPrimaryParent(actionedUponNodeRef);
                        nodeService.addChild(workflowPackage, actionedUponNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, childAssoc.getQName());
                        // move the node to the folder "Uploads/Holding Folder"
                        nodeService.moveNode(actionedUponNodeRef, targetFolderNoderef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) nodeService.getProperty(actionedUponNodeRef,ContentModel.PROP_NAME)));

                        
                        String WorkflowGroupAssignee = "";
                    	String clientID = Integer.toString((Integer) nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_CLIENT_ID));
                        //get admin client group                        
                        WorkflowGroupAssignee = "GROUP_ALF-Client-" + clientID + "-Admin-FullAccess";
                        
                        // Set the parameters for the workflow
                        Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>();
                        // Create QNames for all properties
                        QName qnameInitiator = QName.createQName(null, "initiator");
                        QName qnameInitiatorHome = QName.createQName(null, "initiatorhome");
                        workflowProps.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
                        workflowProps.put(qnameInitiator, initiator);             
                        workflowProps.put(qnameInitiatorHome, initiatorHomeFolder);
                        workflowProps.put(ContentModel.PROP_OWNER, nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_CREATOR));
                        workflowProps.put(WorkflowModel.PROP_DESCRIPTION, nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_NAME));
                        workflowProps.put(PunterSouthallWorkflowModel.PROP_DATE_RECEIVED, nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_CREATED));
                        workflowProps.put(PunterSouthallWorkflowModel.PROP_ADMIN_SCHEME_GROUP, WorkflowGroupAssignee);
                        
                        workflowProps.put(PunterSouthallWorkflowModel.PROP_CLIENT_ID, nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_CLIENT_ID));
                        workflowProps.put(PunterSouthallWorkflowModel.PROP_CLIENT_NAME, nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_CLIENT_NAME));
                        workflowProps.put(PunterSouthallWorkflowModel.PROP_ADMIN_SCHEME_ID, nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_SCHEME_ID));
                        workflowProps.put(PunterSouthallWorkflowModel.PROP_ADMIN_SCHEME_NAME, nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_SCHEME_NAME));
                        workflowProps.put(PunterSouthallWorkflowModel.PROP_ACTIVITY, nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_ACTIVITY));
                        workflowProps.put(PunterSouthallWorkflowModel.PROP_SUB_ACTIVITY, nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_SUB_ACTIVITY));
                        
                        if (nodeService.getType(actionedUponNodeRef).equals(PunterSouthallCustomModel.TYPE_ADMIN_SCHEME_DOCUMENT)){
                        	workflowProps.put(PunterSouthallWorkflowModel.PROP_WORKFLOW_TASK_TYPE, "Admin-SchemeWork");
                        }
                        else if(nodeService.getType(actionedUponNodeRef).equals(PunterSouthallCustomModel.TYPE_ADMIN_MEMBER_DOCUMENT)){
                        	workflowProps.put(PunterSouthallWorkflowModel.PROP_WORKFLOW_TASK_TYPE, "Admin-MemberWork");
                        	workflowProps.put(PunterSouthallWorkflowModel.PROP_CENTRAL_ID, nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_CENTRAL_ID));
                        	workflowProps.put(PunterSouthallWorkflowModel.PROP_RELEVANT_MEMBER_ID, nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_MEMBER_RECORD_ID));
                        	workflowProps.put(PunterSouthallWorkflowModel.PROP_FIRST_NAME, nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_FIRST_NAME));
                        	workflowProps.put(PunterSouthallWorkflowModel.PROP_SURNAME, nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_SURNAME));
                        	workflowProps.put(PunterSouthallWorkflowModel.PROP_DOB, nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_DOB));
                        	workflowProps.put(PunterSouthallWorkflowModel.PROP_NINO, nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_NINO));
                        }
                        else{
                        	PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(), "Unexpected document type of: " + nodeService.getType(actionedUponNodeRef));
                        	NodeServiceHelper.moveToErrorFolder(nodeService, searchService, actionedUponNodeRef, "Workflow Errors");
                        	return "failed";
                        }
                        
                        
                        // Create workflow
                        WorkflowDefinition workflowDefinition = workflowService.getDefinitionByName("activiti$adminMemberWorkflowProcess");
                        // Start the workflow
                        WorkflowPath workflowPath = workflowService.startWorkflow(workflowDefinition.getId(), workflowProps);

                        if (workflowPath.getId() != null) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("LaunchWorkflowFromEmailAction.executeImpl: Workflow started successfully.");
                            }                           
                        }
                    } catch (InvalidStoreRefException e) {
                        //logger.error(e.getMessage());
                        PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(),e.getMessage().toString());
                    } catch (FileNotFoundException e) {
                        //logger.error(e.getMessage());
                        PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(),e.getMessage().toString());
                    }          	
                }
                else{
                	PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(),"Workflow Aspect is missing");
                	NodeServiceHelper.moveToErrorFolder(nodeService, searchService, actionedUponNodeRef, "Workflow Errors");
                }
                
                return "OK";
            }
        });
    }

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// TODO Auto-generated method stub
		
	}
	
	public class MultipleAdminTeamsException extends Exception {

	    public MultipleAdminTeamsException(String message) {
	        super(message);
	    }

	    public MultipleAdminTeamsException(String message, Throwable throwable) {
	        super(message, throwable);
	    }

	}

	
}
