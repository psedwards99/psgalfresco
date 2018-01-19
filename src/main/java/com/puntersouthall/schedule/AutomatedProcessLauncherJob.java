package com.puntersouthall.schedule;

import com.puntersouthall.model.PunterSouthallWorkflowModel;
import com.puntersouthall.webservice.alfresco.ArrayOfAutomatedTaskType;
import com.puntersouthall.webservice.alfresco.AutomatedTaskType;
import com.puntersouthall.webservice.alfresco.IPunterSouthallService;
import com.puntersouthall.webservice.alfresco.PunterSouthallService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class AutomatedProcessLauncherJob implements StatefulJob {
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		final Logger logger = Logger.getLogger(AutomatedProcessLauncherJob.class);
		
		// Run under the system admin context so that we can launch the workflows ok within Alfresco
		AuthenticationUtil.runAsSystem(new RunAsWork<String>() { 
			public String doWork() {
				logger.debug("AutomatedProcessLauncherJob has started....");
				// Get registries and services
				ServiceRegistry serviceRegistry = getServiceRegistry();
				WorkflowService workflowService = serviceRegistry.getWorkflowService();
				PersonService personService = serviceRegistry.getPersonService();
				NodeService nodeService = serviceRegistry.getNodeService();
				
				
				// Set initiator to Admin so get admin "person"
				NodeRef adminPerson = personService.getPerson("admin");
				Serializable adminHomeFolder = nodeService.getProperty(adminPerson, ContentModel.PROP_HOMEFOLDER);				
				
				// Create QNames for all properties
				QName qnameInitiator = QName.createQName(null, "initiator");
				QName qnameInitiatorHome = QName.createQName(null, "initiatorhome");
				
				PunterSouthallService psService = new PunterSouthallService();
				IPunterSouthallService iPSService = psService.getBasicHttpBindingIPunterSouthallService();
				ArrayOfAutomatedTaskType automatedTasks = iPSService.getAutomatedLaunchTasksForToday();				
				
				for (int x = 0; x < automatedTasks.getAutomatedTaskType().size(); x++) {
					// Set properties
					NodeRef workflowPackage = workflowService.createPackage(null);
					AutomatedTaskType automatedTask = automatedTasks.getAutomatedTaskType().get(x); 					
					GregorianCalendar dateReceived = getGregorianCalendarForXMLGregorianCalendar(automatedTask.getDateReceived());						
					String wfTaskType = "Admin-SchemeWork";
					String wfSourceSystem = "Automated Task";
										
			       	Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>(16);
			       	workflowProps.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
			       	workflowProps.put(qnameInitiator, adminPerson);		       	
			       	workflowProps.put(qnameInitiatorHome, adminHomeFolder);
		       	
			       	workflowProps.put(PunterSouthallWorkflowModel.PROP_ADMIN_SCHEME_GROUP, "GROUP_" + automatedTask.getTeamName().getValue());
			       	workflowProps.put(PunterSouthallWorkflowModel.PROP_TEAM_NAME, automatedTask.getTeamName().getValue());
			       	workflowProps.put(PunterSouthallWorkflowModel.PROP_CLIENT_NAME, automatedTask.getClientName().getValue());
			       	workflowProps.put(PunterSouthallWorkflowModel.PROP_CLIENT_ID, automatedTask.getClientID());
			       	workflowProps.put(PunterSouthallWorkflowModel.PROP_ADMIN_SCHEME_NAME, automatedTask.getSchemeName().getValue());
			       	workflowProps.put(PunterSouthallWorkflowModel.PROP_ADMIN_SCHEME_ID, automatedTask.getSchemeNameID());
			       	workflowProps.put(PunterSouthallWorkflowModel.PROP_ACTIVITY, automatedTask.getActivity().getValue());
			       	workflowProps.put(PunterSouthallWorkflowModel.PROP_SUB_ACTIVITY, automatedTask.getSubActivity().getValue());
			       	workflowProps.put(PunterSouthallWorkflowModel.PROP_DATE_RECEIVED, dateReceived);
			       	
			       	if (automatedTask.getDeadline().getValue() != null) {
			       		GregorianCalendar deadline = getGregorianCalendarForXMLGregorianCalendar(automatedTask.getDeadline().getValue()); 				       		
			       		workflowProps.put(PunterSouthallWorkflowModel.PROP_DEADLINE, deadline);
			       		workflowProps.put(PunterSouthallWorkflowModel.PROP_INTERNAL_DEADLINE, deadline);
			       		wfSourceSystem = "Automated Task - Project";
			       	}
			       	
			       	if (automatedTask.getCentralMemberRecordID().getValue() != null) {
			       		wfTaskType = "Admin-MemberWork";
				       	workflowProps.put(PunterSouthallWorkflowModel.PROP_CENTRAL_ID, automatedTask.getCentralMemberRecordID().getValue());
				       	workflowProps.put(PunterSouthallWorkflowModel.PROP_RELEVANT_MEMBER_ID, automatedTask.getDatabaseMemberID().getValue());
				       	workflowProps.put(PunterSouthallWorkflowModel.PROP_SURNAME, automatedTask.getMemberSurname().getValue());
				       	workflowProps.put(PunterSouthallWorkflowModel.PROP_FIRST_NAME, automatedTask.getMemberFirstName().getValue());
				       	workflowProps.put(PunterSouthallWorkflowModel.PROP_NINO, automatedTask.getMemberNINumber().getValue());
						GregorianCalendar memberDateOfBirth = getGregorianCalendarForXMLGregorianCalendar(automatedTask.getMemberDateOfBirth().getValue()); 			       	
				       	workflowProps.put(PunterSouthallWorkflowModel.PROP_DOB, memberDateOfBirth);
			       	}
			       	//workflowProps.put(WorkflowModel.PROP_COMMENT,automatedTask.getComment().getValue() );	
			       	String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH.mm").format(Calendar.getInstance().getTime());
			       	workflowProps.put(WorkflowModel.PROP_COMMENT,"Automated Task Description\r\n" + timeStamp +"\r\n" + automatedTask.getComment().getValue());		 
			       	workflowProps.put(PunterSouthallWorkflowModel.PROP_WORKFLOW_TASK_TYPE, wfTaskType);
			       	workflowProps.put(PunterSouthallWorkflowModel.PROP_SOURCE_SYSTEM, wfSourceSystem);
			       	
			       	// Create workflow
			       	WorkflowDefinition workflowDefinition = workflowService.getDefinitionByName("activiti$adminMemberWorkflowProcess");
			       	WorkflowPath createdWorkflow = workflowService.startWorkflow(workflowDefinition.getId(), workflowProps);
			       	
			       	Integer automatedLaunchListID = automatedTask.getAutomatedLaunchListID();
			       	if (createdWorkflow.getId() != null)
			       	{
			       		// Tell the database that the task was launched ok
			       		iPSService.updateAutomatedTaskLaunched(automatedLaunchListID);
			       		logger.info("Workflow launched - Automated Launched List ID : " + automatedLaunchListID.toString());
			       	}
			       	else
			       	{
			       		logger.error("Workflow not launched - Automated Launched List ID : " + automatedLaunchListID.toString());
			       	}		       				       	
				}
		       	return "ok";				
			}		
		});
	}	

	private GregorianCalendar getGregorianCalendarForXMLGregorianCalendar(XMLGregorianCalendar xmlGregorianCalendar){
		return new GregorianCalendar(
				xmlGregorianCalendar.getYear(), 
				xmlGregorianCalendar.getMonth() - 1, 
				xmlGregorianCalendar.getDay(),
				xmlGregorianCalendar.getHour(),
				xmlGregorianCalendar.getMinute(),
				xmlGregorianCalendar.getSecond()); 
	}	
	
	private ServiceRegistry getServiceRegistry(){
		BeanFactoryLocator factoryLocator = new JbpmFactoryLocator();
		BeanFactoryReference factoryReference = factoryLocator.useBeanFactory(null);
		BeanFactory factory = factoryReference.getFactory();
		return (ServiceRegistry) factory.getBean(ServiceRegistry.SERVICE_REGISTRY);
	}
	
}
