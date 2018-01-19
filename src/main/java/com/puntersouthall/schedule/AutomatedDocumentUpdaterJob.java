package com.puntersouthall.schedule;

import com.puntersouthall.model.PunterSouthallCustomModel;
import com.puntersouthall.model.PunterSouthallWorkflowModel;
import com.puntersouthall.webservice.alfresco.ArrayOfCentralMemberRecordChangeType;
import com.puntersouthall.webservice.alfresco.CentralMemberRecordChangeType;
import com.puntersouthall.webservice.alfresco.IPunterSouthallService;
import com.puntersouthall.webservice.alfresco.PunterSouthallService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springmodules.workflow.jbpm31.JbpmFactoryLocator;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.Serializable;
import java.util.*;

public class AutomatedDocumentUpdaterJob implements StatefulJob {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
			
		final Logger logger = Logger.getLogger(AutomatedDocumentUpdaterJob.class);
		
		AuthenticationUtil.runAsSystem(new RunAsWork<String>() { 
			public String doWork() {
				logger.debug("AutomatedDocumentUpdaterJob has started....");
				ServiceRegistry serviceRegistry = getServiceRegistry();
				LockService lockService = serviceRegistry.getLockService();
				NodeService nodeService = serviceRegistry.getNodeService();
				SearchService searchService = serviceRegistry.getSearchService();
				WorkflowService workflowService = serviceRegistry.getWorkflowService();
				StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");		
				
				Boolean endOfDayUpdate = false;
				Date now = new Date();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(now);
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				if (hour >= 20){
					endOfDayUpdate = true;
				}
				
				PunterSouthallService psService = new PunterSouthallService();
				IPunterSouthallService iPSService = psService.getBasicHttpBindingIPunterSouthallService();
				ArrayOfCentralMemberRecordChangeType changeRecords = iPSService.getCentralMemberRecordsChanges(endOfDayUpdate);
							
				for (int x = 0; x < changeRecords.getCentralMemberRecordChangeType().size(); x++) {			
					CentralMemberRecordChangeType changeRecord = changeRecords.getCentralMemberRecordChangeType().get(x);
					Integer changesID = changeRecord.getChangesID();
					Integer centralMemberRecordID = changeRecord.getCentralMemberRecordID();
					ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "@psdoc\\:centralID:" + centralMemberRecordID.toString());				
					
					String firstName = changeRecord.getMemberFirstName().getValue();
					String surname = changeRecord.getMemberSurname().getValue();
					String nino = changeRecord.getMemberNINumber().getValue();					
					GregorianCalendar memberDOB = getGregorianCalendarForXMLGregorianCalendar(changeRecord.getMemberDateOfBirth().getValue()); 
									
					try {
					    if (rs.length() != 0) {			
					    	// Documents					    		
					    	
					    	for (int y = 0; y < rs.length(); y++) {				    		
					    		NodeRef documentNodeRef = rs.getNodeRef(y);
					    		// Get current document properties
					    		Map<QName, Serializable> documentProperties = nodeService.getProperties(documentNodeRef);
					    		String currentFirstName = (String)documentProperties.get(PunterSouthallCustomModel.PROP_FIRST_NAME);
					    		String currentSurname = (String)documentProperties.get(PunterSouthallCustomModel.PROP_SURNAME);
					    		String currentNINo = (String)documentProperties.get(PunterSouthallCustomModel.PROP_NINO);
					    		Date currentMemberDOB = (Date)documentProperties.get(PunterSouthallCustomModel.PROP_DOB);
					    		GregorianCalendar currentMemberDOBGregorian = new GregorianCalendar();
					    		currentMemberDOBGregorian.setTime(currentMemberDOB);
					    		// If any of the current properties doesn't equal the new properties, then update the document
					    		if (currentFirstName != firstName || currentSurname != surname || currentNINo != nino || currentMemberDOBGregorian != memberDOB)
					    		{
					    			documentProperties.put(PunterSouthallCustomModel.PROP_FIRST_NAME, firstName);
					    			documentProperties.put(PunterSouthallCustomModel.PROP_SURNAME, surname);
					    			documentProperties.put(PunterSouthallCustomModel.PROP_NINO, nino);
					    			documentProperties.put(PunterSouthallCustomModel.PROP_DOB, memberDOB);
					    			if (lockService.getLockStatus(documentNodeRef).equals(LockStatus.LOCKED)) {
					    				lockService.unlock(documentNodeRef);
					    				//nodeService.setProperties(documentNodeRef, documentProperties);
					    				nodeService.setProperty(documentNodeRef, PunterSouthallCustomModel.PROP_FIRST_NAME, firstName);
					    				nodeService.setProperty(documentNodeRef, PunterSouthallCustomModel.PROP_SURNAME, surname);
					    				nodeService.setProperty(documentNodeRef, PunterSouthallCustomModel.PROP_NINO, nino);
					    				nodeService.setProperty(documentNodeRef, PunterSouthallCustomModel.PROP_DOB, memberDOB);
					    				lockService.lock(documentNodeRef, LockType.READ_ONLY_LOCK);					    				
					    			}
					    			else {
					    				nodeService.setProperties(documentNodeRef, documentProperties);					    			
					    			}

					    		}
					    	}							    	
					    }
					    // Workflows
						Map<QName, Object> properties = new HashMap<QName, Object>();
						properties.put(PunterSouthallWorkflowModel.PROP_CENTRAL_ID, centralMemberRecordID);
						
						Map<QName, Serializable> updatedWorkflowProperties = new HashMap<QName, Serializable>();
						updatedWorkflowProperties.put(PunterSouthallWorkflowModel.PROP_FIRST_NAME, firstName);
						updatedWorkflowProperties.put(PunterSouthallWorkflowModel.PROP_SURNAME, surname);
						updatedWorkflowProperties.put(PunterSouthallWorkflowModel.PROP_NINO, nino);
						updatedWorkflowProperties.put(PunterSouthallWorkflowModel.PROP_DOB, memberDOB);
						
						WorkflowTaskQuery workflowTaskQuery = new WorkflowTaskQuery();
						workflowTaskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
						workflowTaskQuery.setProcessCustomProps(properties);
						
						List<WorkflowTask> workflowTasks = workflowService.queryTasks(workflowTaskQuery, true);

						for (WorkflowTask workflowTask : workflowTasks) {								
							String taskID = workflowTask.getId();
							Map<QName, Serializable> workflowProperties = workflowTask.getProperties();
							
							String currentWorkflowFirstName = (String)workflowProperties.get(PunterSouthallWorkflowModel.PROP_FIRST_NAME);
				    		String currentWorkflowSurname = (String)workflowProperties.get(PunterSouthallWorkflowModel.PROP_SURNAME);
				    		String currentWorkflowNINo = (String)workflowProperties.get(PunterSouthallWorkflowModel.PROP_NINO);
				    		Date currentWorkflowMemberDOB = (Date)workflowProperties.get(PunterSouthallWorkflowModel.PROP_DOB);
				    		GregorianCalendar currentWorkflowMemberDOBGregorian = new GregorianCalendar();
				    		currentWorkflowMemberDOBGregorian.setTime(currentWorkflowMemberDOB);
				    		// If any of the current properties doesn't equal the new properties, then update the workflow
				    		if (currentWorkflowFirstName != firstName || currentWorkflowSurname != surname || currentWorkflowNINo != nino || currentWorkflowMemberDOBGregorian != memberDOB)
				    			workflowService.updateTask(taskID, updatedWorkflowProperties, null, null);
						}
						
						if (rs.length() != 0)
							logger.info("Document details updated for central member ID : " + centralMemberRecordID.toString());
						else 
							logger.info("No documents to update for central member ID : " + centralMemberRecordID.toString());						
						
						Boolean updatedOk = iPSService.updateCentralMemberRecordsChangeRecord(changesID, endOfDayUpdate);
												
						if (!updatedOk)
							logger.error("ChangesToDocumentRecord update failed for central member ID : " + centralMemberRecordID.toString());						
					
					    
					}
					catch (Exception ex){
						logger.error("Document details update failed for central member ID : " + centralMemberRecordID.toString());
						logger.error(ex.getMessage());
					}
					finally {												
						rs.close();				    
					}													
				}
				logger.debug("AutomatedDocumentUpdaterJob has completed.");
				return "ok";
			}						
		});
	}	
	
	private ServiceRegistry getServiceRegistry(){
		BeanFactoryLocator factoryLocator = new JbpmFactoryLocator();
		BeanFactoryReference factoryReference = factoryLocator.useBeanFactory(null);
		BeanFactory factory = factoryReference.getFactory();
		return (ServiceRegistry) factory.getBean(ServiceRegistry.SERVICE_REGISTRY);
	}
	
	private GregorianCalendar getGregorianCalendarForXMLGregorianCalendar(XMLGregorianCalendar xmlGregorianCalendar){
		return new GregorianCalendar(
				xmlGregorianCalendar.getYear(), 
				xmlGregorianCalendar.getMonth() - 1, 
				xmlGregorianCalendar.getDay()); 
	}	
}
