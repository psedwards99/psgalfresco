package com.puntersouthall.utils;

import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WorkflowUtils {

	private final Log logger = LogFactory.getLog(WorkflowUtils.class);
    
    /** Alfresco services */
    private ServiceRegistry serviceRegistry;
    private AuthorityService authorityService;
    private NodeService nodeService;
    
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry; 
        this.authorityService = serviceRegistry.getAuthorityService();
        this.nodeService = serviceRegistry.getNodeService();
    }
    
    public ActivitiScriptNode getGroupByName(String groupName) {
        
        // Get the group noderef
        NodeRef group = authorityService.getAuthorityNodeRef(groupName);
        
        if(logger.isDebugEnabled())
        {
            //logger.debug("com.puntersouthall.utils.WorkflowUtils.getGroupByName: group " + groupName + " exists = " + nodeService.exists(group));
            logger.debug("com.puntersouthall.utils.WorkflowUtils.getGroupByName: group = " + groupName + " exists = " + nodeService.exists(group));
        }
        
        return new ActivitiScriptNode(group, serviceRegistry);
    }
    
}
