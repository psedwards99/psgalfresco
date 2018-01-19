package com.puntersouthall.actions;

import com.puntersouthall.helper.NodeServiceHelper;
import com.puntersouthall.helper.PSErrorLoggingHelper;
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

public class LaunchWorkflowFromScannedDocumentAction extends ActionExecuterAbstractBase {

    /** the logger */
    private final Log logger = LogFactory.getLog(LaunchWorkflowFromScannedDocumentAction.class);

    /** workflow properties */
    private String PROP_SOURCE_SYSTEM;
    private String PROP_ADMIN_SCHEME_GROUP;
    private String ATTACHMENTS_TARGET_FOLDER_PATH;
    private String ARCHIVED_EMAIL_FOLDER_PATH;
    private String PROP_BIRM_INDEXING_TEAM;
    private String PROP_BRIS_INDEXING_TEAM;
    private String PROP_CHEL_INDEXING_TEAM;
    private String PROP_EDIN_INDEXING_TEAM;
    private String PROP_LOND_INDEXING_TEAM;
    private String PROP_NEWC_INDEXING_TEAM;
    private String PROP_PERT_INDEXING_TEAM;
    private String PROP_WOKO_INDEXING_TEAM;

    /** Alfresco services */
    private ServiceRegistry serviceRegistry;
    private NodeService nodeService;
    private WorkflowService workflowService;
    private PersonService personService;
    private FileFolderService fileFolderService;
    private SearchService searchService;
    private ContentService contentService;
	private AuthenticationService authenticationService;
    
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
        this.workflowService = serviceRegistry.getWorkflowService();
        this.personService = serviceRegistry.getPersonService();
        this.fileFolderService = serviceRegistry.getFileFolderService();
        this.searchService = serviceRegistry.getSearchService();
        this.contentService = serviceRegistry.getContentService();
        this.authenticationService = serviceRegistry.getAuthenticationService();
    }
    
    public void setPROP_SOURCE_SYSTEM(String pROP_SOURCE_SYSTEM) {
        PROP_SOURCE_SYSTEM = pROP_SOURCE_SYSTEM;
    }

    public void setPROP_ADMIN_SCHEME_GROUP(String pROP_ADMIN_SCHEME_GROUP) {
        PROP_ADMIN_SCHEME_GROUP = pROP_ADMIN_SCHEME_GROUP;
    }

    public void setATTACHMENTS_TARGET_FOLDER_PATH(String aTTACHMENTS_TARGET_FOLDER_PATH) {
        ATTACHMENTS_TARGET_FOLDER_PATH = aTTACHMENTS_TARGET_FOLDER_PATH;
    }
    
    public void setPROP_BIRM_INDEXING_TEAM(String pROP_BIRM_INDEXING_TEAM){
    	PROP_BIRM_INDEXING_TEAM = pROP_BIRM_INDEXING_TEAM;
    }
    public void setPROP_BRIS_INDEXING_TEAM(String pROP_BRIS_INDEXING_TEAM){
    	PROP_BRIS_INDEXING_TEAM = pROP_BRIS_INDEXING_TEAM;
    }
    public void setPROP_CHEL_INDEXING_TEAM(String pROP_CHEL_INDEXING_TEAM){
    	PROP_CHEL_INDEXING_TEAM = pROP_CHEL_INDEXING_TEAM;
    }
    public void setPROP_EDIN_INDEXING_TEAM(String pROP_EDIN_INDEXING_TEAM){
    	PROP_EDIN_INDEXING_TEAM = pROP_EDIN_INDEXING_TEAM;
    }
    public void setPROP_LOND_INDEXING_TEAM(String pROP_LOND_INDEXING_TEAM){
    	PROP_LOND_INDEXING_TEAM = pROP_LOND_INDEXING_TEAM;
    }
    public void setPROP_NEWC_INDEXING_TEAM(String pROP_NEWC_INDEXING_TEAM){
    	PROP_NEWC_INDEXING_TEAM = pROP_NEWC_INDEXING_TEAM;
    }
    public void setPROP_PERT_INDEXING_TEAM(String pROP_PERT_INDEXING_TEAM){
    	PROP_PERT_INDEXING_TEAM = pROP_PERT_INDEXING_TEAM;
    }
    public void setPROP_WOKO_INDEXING_TEAM(String pROP_WOKO_INDEXING_TEAM){
    	PROP_WOKO_INDEXING_TEAM = pROP_WOKO_INDEXING_TEAM;
    }

    @Override
    protected void executeImpl(Action action, final NodeRef actionedUponNodeRef) {

    	
        // Run under the system admin context so that we can launch the workflows ok within Alfresco
        AuthenticationUtil.runAsSystem(new RunAsWork<String>() {
            public String doWork() {     
            	
            			// get logged in user
            			String loggedInUser = authenticationService.getCurrentUserName();
            	
                		// The workflow package
	                    NodeRef workflowPackage = workflowService.createPackage(null);
	                    // Set initiator as the creator
	                    NodeRef initiator = personService.getPerson((String) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_CREATOR));
	                    Serializable initiatorHomeFolder = nodeService.getProperty(initiator, ContentModel.PROP_HOMEFOLDER);
	
	                    
	                    // get the name of the folder one level up (2 alfresco levels)
	                    String office =  (String) nodeService.getProperty(nodeService.getPrimaryParent(nodeService.getPrimaryParent(actionedUponNodeRef).getParentRef()).getParentRef(),ContentModel.PROP_NAME);
	                    if (logger.isDebugEnabled()){
	                    logger.debug("Office name : " + office + " length : " + office.length() + office.getClass());
	                    }
	                    // Get the target folder noderef where move the attachments
	                    NodeRef targetFolderNoderef = null;
	                    List<String> pathElements = Arrays.asList(StringUtils.split(ATTACHMENTS_TARGET_FOLDER_PATH, '/'));
	
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

	                        
	                        String WorkflowGroupAssignee;
	                        logger.debug("Office name : " + office + " length : " + office.length() + office.getClass());
	                        if(office.equals("Birmingham")){
	                        	WorkflowGroupAssignee = PROP_BIRM_INDEXING_TEAM;
	                        	logger.debug("Birmingham found: " + WorkflowGroupAssignee);
	                        }
	                        else if(office.equals("Bristol")){
	                        	WorkflowGroupAssignee = PROP_BRIS_INDEXING_TEAM;
	                        }
	                        else if(office.equals("Chelmsford")){
	                        	WorkflowGroupAssignee = PROP_CHEL_INDEXING_TEAM;
	                        }  
	                        else if(office.equals("Edinburgh")){
	                        	WorkflowGroupAssignee = PROP_EDIN_INDEXING_TEAM;
	                        }       
	                        else if(office.equals("London")){
	                        	WorkflowGroupAssignee = PROP_LOND_INDEXING_TEAM;
	                        }       
	                        else if(office.equals("Newcastle")){
	                        	WorkflowGroupAssignee = PROP_NEWC_INDEXING_TEAM;
	                        }
	                        else if(office.equals("Perth")){
	                        	WorkflowGroupAssignee = PROP_PERT_INDEXING_TEAM;
	                        }   
	                        else if(office.equals("Wokingham")){
	                        	WorkflowGroupAssignee = PROP_WOKO_INDEXING_TEAM;
	                        } 
	                        else{
	                        	NodeServiceHelper.moveToErrorFolder(nodeService, searchService, actionedUponNodeRef, "Workflow Errors");
	                        	return "failed";	                        	 
	                        }	                        
	                        
	                        
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
	                        workflowProps.put(PunterSouthallWorkflowModel.PROP_SOURCE_SYSTEM, ("Scanning - " + office).toString());
	                        workflowProps.put(PunterSouthallWorkflowModel.PROP_ADMIN_SCHEME_GROUP, WorkflowGroupAssignee);
	                        
	                        // Create workflow
	                        WorkflowDefinition workflowDefinition = workflowService.getDefinitionByName("activiti$adminMemberWorkflowProcess");
	                        // Start the workflow
	                        if (logger.isDebugEnabled()){
	                        logger.debug("Workflow group: " + WorkflowGroupAssignee);
	                        }

	                        WorkflowPath workflowPath = workflowService.startWorkflow(workflowDefinition.getId(), workflowProps);
	
	                        if (workflowPath.getId() != null) {
	                            if (logger.isDebugEnabled()) {
	                                logger.debug("LaunchWorkflowFromEmailAction.executeImpl: Workflow started successfully.");
	                            }                           
	                        }
	                    } catch (InvalidStoreRefException e) {
	                       PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(), e.getMessage().toString());
	                    } catch (FileNotFoundException e) {
	                        PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(), e.getMessage().toString());
	                    }
                
                
                return "OK";
            }
        });
    }

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// TODO Auto-generated method stub
		
	}
	
}
