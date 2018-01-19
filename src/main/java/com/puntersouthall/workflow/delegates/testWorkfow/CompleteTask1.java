package com.puntersouthall.workflow.delegates.testWorkfow;

import com.puntersouthall.utils.WorkflowUtils;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.script.ActivitiScriptBase;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springmodules.workflow.jbpm31.JbpmFactoryLocator;

public class CompleteTask1 extends ActivitiScriptBase implements TaskListener{

    private static final long serialVersionUID = 1L;
    
    private final String WORKFLOW_UTILS_BEAN_KEY = "custom.WorkflowUtils";

    @Override
    public void notify(DelegateTask task) {
        
        // Get the teamName variable
        String teamName = (String) task.getVariable("pswf_adminSchemeGroup");
        
        // Get the workflow utils service
        WorkflowUtils workflowUtils = getWorkflowUtils();
        
        // The new group assignee
        ActivitiScriptNode groupNode = workflowUtils.getGroupByName(teamName);
        
        // Set the group
        task.setVariable("bpm_groupAssignee", groupNode);
        
    }
    
    private WorkflowUtils getWorkflowUtils(){
        BeanFactoryLocator factoryLocator = new JbpmFactoryLocator();
        BeanFactoryReference factoryReference = factoryLocator.useBeanFactory(null);
        BeanFactory factory = factoryReference.getFactory();
        return (WorkflowUtils) factory.getBean(WORKFLOW_UTILS_BEAN_KEY);
    }

}
