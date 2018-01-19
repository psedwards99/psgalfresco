package com.puntersouthall.workflow.delegates.testWorkflow;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;

public class InitWorkflowProperties implements JavaDelegate {

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {
            public Void doWork()
            {
                // Set the initial value of the variable 'pswf_adminSchemeGroup'
                execution.setVariable("pswf_adminSchemeGroup", "GROUP_ALF-SchemeAggregateIndustriesPensionPlan");

                return null;
            }
        });

    }

}