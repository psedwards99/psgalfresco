package com.puntersouthall.schedule;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

public class PSGDocumentUpdaterScheduledJob extends AbstractScheduledLockedJob implements StatefulJob {
	@Override
	public void executeJob(JobExecutionContext context) throws JobExecutionException {
		JobDataMap jobData = context.getJobDetail().getJobDataMap();

		// Extract the Job executer to use
		Object executerObj = jobData.get("jobExecuter");
		if (executerObj == null || !(executerObj instanceof PSGDocumentUpdater)) {
			throw new AlfrescoRuntimeException(
					"PSGDocumentUpdaterScheduledJob data must contain valid 'Executer' reference");
		}

		final PSGDocumentUpdater jobExecuter = (PSGDocumentUpdater) executerObj;

		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
			public Object doWork() throws Exception {
				jobExecuter.execute();
				return null;
			}
		}, AuthenticationUtil.getSystemUserName());
	}
}

