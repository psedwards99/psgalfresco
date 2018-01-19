package com.puntersouthall.workflow.delegates.testWorkfow;

import java.util.Set;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.puntersouthall.actions.LaunchWorkflowFromUserDocumentAction;
import com.puntersouthall.actions.LaunchWorkflowFromUserDocumentAction.MultipleAdminTeamsException;
import com.puntersouthall.helper.NodeServiceHelper;
import com.puntersouthall.helper.PSErrorLoggingHelper;
import com.puntersouthall.utils.WorkflowUtils;

public class SetBPMGroupAssigneeTask implements JavaDelegate  {
    
	private final Log logger = LogFactory.getLog(SetBPMGroupAssigneeTask.class);
	
    private WorkflowUtils workflowUtils;
    private AuthorityService authorityService;
    private AuthenticationService authenticationService;
    
    
	
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.authorityService = serviceRegistry.getAuthorityService();
        this.authenticationService = serviceRegistry.getAuthenticationService();
    }
    
    public void setWorkflowUtils(WorkflowUtils workflowUtils) {
        this.workflowUtils = workflowUtils;
    }

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {
            public Void doWork()
            {
                // Get the variable 'pswf_adminSchemeGroup'
                String groupName = (String) execution.getVariable("pswf_adminSchemeGroup");
            	
                //get admin team group from user  
                
                //need to check for null first as workdesk errors on isempty, share can handle both however but only passes true on isempty.
            	if ( groupName == null || groupName.isEmpty()  ){
            		// get logged in user
            		String loggedInUser = authenticationService.getCurrentUserName();
                    
                    Set<String> loggedInUserGroups = authorityService.getAuthoritiesForUser(loggedInUser);	
                    
                    Integer counter = 0;
                    for (String s : loggedInUserGroups) {
                    	if(s.length() > 18){
                    		if(s.substring(0, 19).equals("GROUP_ALF-AdminTeam")){
                    			counter++;
                    			if (counter > 1){
                    				PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, "", "user is member of more than 1 admin team.");
    							}
                    			groupName = s;
                    			if (logger.isDebugEnabled()){
                    			logger.debug("AdminTeam group found for user :" + loggedInUser + " and group is:" + groupName);
                    			}
                    		}
                    	}
                    }
            		
            	}

                // Get the node group
                ActivitiScriptNode groupNode = workflowUtils.getGroupByName(groupName);
                // Set the bpm_groupAssignee variable using 'pswf_adminSchemeGroup'
                execution.setVariable("bpm_groupAssignee", groupNode);
                
                return null;
            }
        });
        
    }
}
