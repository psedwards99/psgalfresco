package com.puntersouthall.actions;

import com.puntersouthall.helper.NodeServiceHelper;
import com.puntersouthall.helper.PSErrorLoggingHelper;
import com.puntersouthall.model.PunterSouthallCustomModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.List;

public class UserUploadedDocumentAction extends ActionExecuterAbstractBase {

    /** the logger */
    private final Log logger = LogFactory.getLog(UserUploadedDocumentAction.class);

    /** path properties */
    private String MOVE_DOCUMENT_FOLDER_PATH;
    private String START_WORKFLOW_FOLDER_PATH;
    private String FILING_ERRORS_FOLDER_PATH;

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
    private OwnableService ownableService;
    
	
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
        this.ownableService = serviceRegistry.getOwnableService();
    }
    
    public void setMOVE_DOCUMENT_FOLDER_PATH(String mOVE_DOCUMENT_FOLDER_PATH) {
    	MOVE_DOCUMENT_FOLDER_PATH = mOVE_DOCUMENT_FOLDER_PATH;
    }
    
    public void setSTART_WORKFLOW_FOLDER_PATH(String sTART_WORKFLOW_FOLDER_PATH) {
    	START_WORKFLOW_FOLDER_PATH = sTART_WORKFLOW_FOLDER_PATH;
    }
    public void setFILING_ERRORS_FOLDER_PATH(String fILING_ERRORS_FOLDER_PATH) {
    	FILING_ERRORS_FOLDER_PATH = fILING_ERRORS_FOLDER_PATH;
    }


    @Override
    protected void executeImpl(Action action, final NodeRef actionedUponNodeRef) {

    	
        // Run under the system admin context so that we can launch the workflows ok within Alfresco
        AuthenticationUtil.runAsSystem(new RunAsWork<String>() {
            public String doWork() {
            	
            	// get logged in user
            	String loggedInUser = authenticationService.getCurrentUserName();
            	
            	//set owner
            	ownableService.setOwner(actionedUponNodeRef, "admin");
            	
                // Check if the node has the workflow aspect
            	String pathToUse = "";
                if (nodeService.hasAspect(actionedUponNodeRef, PunterSouthallCustomModel.ASPECT_WORKFLOW)) {
            		pathToUse = START_WORKFLOW_FOLDER_PATH;
            		if (logger.isDebugEnabled()){
                    logger.debug("Document has workflow aspect - move to Start Workflow folder .  Noderef: " + actionedUponNodeRef);   
            		}
                }
                else{
                	pathToUse = MOVE_DOCUMENT_FOLDER_PATH;
                	if (logger.isDebugEnabled()){
                    logger.debug("Document does not have workflow aspect - move to Move Documents folder .  Noderef: " + actionedUponNodeRef);       
                	}
                }
           
                // Get the target folder noderef where move the attachments
                try{
	                List<String> pathElements = Arrays.asList(StringUtils.split(pathToUse, '/'));
	                
	                NodeRef targetFolderNoderef = null;
	                
	                FileInfo fileInfo = null;	                    
	                fileInfo = fileFolderService.resolveNamePath(NodeServiceHelper.getCompanyHome(searchService), pathElements);
	                // Get the target folder noderef
	                targetFolderNoderef = fileInfo.getNodeRef();
	                if (logger.isDebugEnabled()){
	                logger.debug("Target folder : "+ nodeService.getProperty(targetFolderNoderef, ContentModel.PROP_NAME));
	                }
	                
	                
	                nodeService.moveNode(actionedUponNodeRef, targetFolderNoderef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) nodeService.getProperty(actionedUponNodeRef,ContentModel.PROP_NAME)));
     
	                
	               
                } catch (InvalidStoreRefException e) {
                    PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(),"Initiating doc is set to FALSE");
                } catch (FileNotFoundException e) {
                    PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(),"Initiating doc is set to FALSE");
                }
				return "Ok";
            }
        });
    }

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// TODO Auto-generated method stub
		
	}
	
}
