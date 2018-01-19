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

public class LaunchWorkflowFromEmailAction extends ActionExecuterAbstractBase {

    /** the logger */
    private final Log logger = LogFactory.getLog(LaunchWorkflowFromEmailAction.class);

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
    private AuthenticationService authenticationService;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
        this.workflowService = serviceRegistry.getWorkflowService();
        this.personService = serviceRegistry.getPersonService();
        this.fileFolderService = serviceRegistry.getFileFolderService();
        this.searchService = serviceRegistry.getSearchService();
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

    public void setARCHIVED_EMAIL_FOLDER_PATH(String aRCHIVED_EMAIL_FOLDER_PATH) {
        ARCHIVED_EMAIL_FOLDER_PATH = aRCHIVED_EMAIL_FOLDER_PATH;
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
            	
                // Check if the node has the email aspect
                if (nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_EMAILED)) {
                    // The workflow package
                    NodeRef workflowPackage = workflowService.createPackage(null);
                    // Set initiator as the creator
                    NodeRef initiator = personService.getPerson((String) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_CREATOR));
                    Serializable initiatorHomeFolder = nodeService.getProperty(initiator, ContentModel.PROP_HOMEFOLDER);
                    //Get the parent (MyPension Emails) folder where the email enters alfresco.
                    NodeRef parentFolder = nodeService.getPrimaryParent(actionedUponNodeRef).getParentRef();
                    // Get the attachments from the email node
                    List<AssociationRef> assocs = nodeService.getTargetAssocs(actionedUponNodeRef, ContentModel.ASSOC_ATTACHMENTS);
                    
                    // get the name of the folder one level up (2 alfresco levels)
                    String office =  (String) nodeService.getProperty(nodeService.getPrimaryParent(nodeService.getPrimaryParent(actionedUponNodeRef).getParentRef()).getParentRef(),ContentModel.PROP_NAME);
                    logger.debug("Office name : " + office + " length : " + office.length() + office.getClass());
                    
                    
                    
                    // Get the target folder noderef where move the attachments
                    NodeRef targetFolderNoderef = null;
                    List<String> pathElements = Arrays.asList(StringUtils.split(ATTACHMENTS_TARGET_FOLDER_PATH, '/'));

                    FileInfo fileInfo = null;
                    try {
                    	
                    	String emailSource = (String) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_ORIGINATOR);
                    	
                        fileInfo = fileFolderService.resolveNamePath(NodeServiceHelper.getCompanyHome(searchService), pathElements);
                        // Get the target folder noderef
                        targetFolderNoderef = fileInfo.getNodeRef();
                        logger.debug("Attachments folder : "+targetFolderNoderef);
                        
                        // check if there is an html version of the email stored as one of the attachments. (Usually if email was sent as html)
                        String actionedUponNodeName = (String) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_NAME);
                        Boolean hasHTMLEmailVersion = false;
                        HTMLcheck:
                        for (AssociationRef assoc : assocs) {
                        	NodeRef attachedNodeRef = assoc.getTargetRef();
                        	String  attachmentNodeName = (String) nodeService.getProperty(attachedNodeRef, ContentModel.PROP_NAME);
                        	// check that the attachment has the same name without the "(Part 1)" text and that it ends .html
                    		if( attachmentNodeName.toLowerCase().contains(actionedUponNodeName.toLowerCase()) && attachmentNodeName.endsWith(".html") ){
                    			hasHTMLEmailVersion = true;
                    			break HTMLcheck;
                    		}
                    	
                        }
                        
                        
                        // Add all attachments to the workflow package regardless of type of incoming email
                        for (AssociationRef assoc : assocs) {
                            NodeRef attachNodeRef = assoc.getTargetRef();
                            nodeService.addChild(workflowPackage, attachNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, assoc.getTypeQName());
                            // move the node to the folder "Uploads/Holding Folder"
	                        nodeService.moveNode(attachNodeRef, targetFolderNoderef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) nodeService.getProperty(actionedUponNodeRef,ContentModel.PROP_NAME)));
           
                        }
                        
                        // mypension.com send the emails as plain text or alfresco interprets them as plain text which is just one file
                        if(!hasHTMLEmailVersion){
                                                
                        	if (emailSource.equals("mypension@puntersouthall.com") || emailSource.equals("noreply@mypension.com") || emailSource.equals("dsapp14@mypension.com") ){
	                        	
	                        		//change the mime type of incoming mypension email to html so it displays correctly
		                        	ContentData cd = (ContentData) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_CONTENT);
		                        	ContentData newCD = ContentData.setMimetype(cd, "text/html");
		                        	nodeService.setProperty(actionedUponNodeRef, ContentModel.PROP_CONTENT, newCD);
	                        }
	                        //no html file so add the emailed in file to workflow package too and dont archive it
                            nodeService.addChild(workflowPackage, actionedUponNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "workflowcontents"));
                            // move the node to the folder "Uploads/Holding Folder"
	                        nodeService.moveNode(actionedUponNodeRef, targetFolderNoderef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) nodeService.getProperty(actionedUponNodeRef,ContentModel.PROP_NAME)));
		           
		                        
	                        
                    	}
                        String WorkflowGroupAssignee;
                        if (logger.isDebugEnabled()){
                        logger.debug("Office name : " + office + " length : " + office.length() + office.getClass());
                        }
                        if(office.equals("Birmingham")){
                        	WorkflowGroupAssignee = PROP_BIRM_INDEXING_TEAM;
                        	logger.debug("Birmingham found");
                        }
                        else if(office.equals("Bristol")){
                        	WorkflowGroupAssignee = PROP_BRIS_INDEXING_TEAM;
                        }       
                        else if(office.equals("Edinburgh")){
                        	WorkflowGroupAssignee = PROP_WOKO_INDEXING_TEAM;
                        }       
                        else if(office.equals("London")){
                        	WorkflowGroupAssignee = PROP_BIRM_INDEXING_TEAM;
                        }       
                        else if(office.equals("Newcastle")){
                        	WorkflowGroupAssignee = PROP_BRIS_INDEXING_TEAM;
                        }       
                        else if(office.equals("Wokingham")){
                        	WorkflowGroupAssignee = PROP_WOKO_INDEXING_TEAM;
                        }   
                        else if(office.equals("Chelmsford")){
                        	WorkflowGroupAssignee = PROP_CHEL_INDEXING_TEAM;
                        } 
                        else if(office.equals("Perth")){
                        	WorkflowGroupAssignee = PROP_PERT_INDEXING_TEAM;
                        }  
                        else{
                        	 logger.warn("No office found for the incoming file");
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
                        
                        // temporary hack to aid birmingham scanner emma with indexing until proper email indexing is added for all users in phase 2
                        if(emailSource.toLowerCase().equals("emma.underhill@puntersouthall.com") || emailSource.toLowerCase().equals("joe.bujok@puntersouthall.com")){
                        	workflowProps.put(PunterSouthallWorkflowModel.PROP_SOURCE_SYSTEM, emailSource);
                        }
                        else{
                        	 workflowProps.put(PunterSouthallWorkflowModel.PROP_SOURCE_SYSTEM, PROP_SOURCE_SYSTEM);
                        }
                       
                        workflowProps.put(PunterSouthallWorkflowModel.PROP_ADMIN_SCHEME_GROUP, WorkflowGroupAssignee);
                        
                        // Create workflow
                        WorkflowDefinition workflowDefinition = workflowService.getDefinitionByName("activiti$adminMemberWorkflowProcess");
                        // Start the workflow
                        WorkflowPath workflowPath = workflowService.startWorkflow(workflowDefinition.getId(), workflowProps);

                        if (workflowPath.getId() != null) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("LaunchWorkflowFromEmailAction.executeImpl: Workflow started successfully.");
                            }
                            // Archive the email if there is an HTML version of incoming file
                            // Get the target folder noderef where to move the emails
                            if (hasHTMLEmailVersion){
	                            
	                 
	
	                            pathElements = Arrays.asList(StringUtils.split(ARCHIVED_EMAIL_FOLDER_PATH, '/'));
	                            fileInfo = null;
	                            try {
	                                fileInfo = fileFolderService.resolveNamePath(parentFolder, pathElements);
	                                if (logger.isDebugEnabled()){
	                                logger.debug("fileinfo : " + fileInfo);
	                                }
	                                // Get the target folder noderef
	                                targetFolderNoderef = fileInfo.getNodeRef();
	                                if (logger.isDebugEnabled()){
	                                logger.debug("fileinfo noderef : " + targetFolderNoderef);
	                                }
	                                // move the node to the folder "Archived Email Link Files"
	    	                        nodeService.moveNode(actionedUponNodeRef, targetFolderNoderef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) nodeService.getProperty(actionedUponNodeRef,ContentModel.PROP_NAME)));

	                            } catch (InvalidStoreRefException e) {
	                                
	                            	PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(), e.getMessage().toString());
	                            } catch (FileNotFoundException e) {
	                                
	                            	PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(), e.getMessage().toString());
	                            }
                            }
                        }
                    } catch (InvalidStoreRefException e) {
                        //logger.error(e.getMessage());
                    	PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(), e.getMessage().toString());
                    } catch (FileNotFoundException e) {
                        //logger.error(e.getMessage());
                    	PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(), e.getMessage().toString());
                    }
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
