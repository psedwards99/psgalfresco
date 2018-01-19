package com.puntersouthall.workflow.delegates;

import com.puntersouthall.webservice.alfresco.CreateTaskNumberType;
import com.puntersouthall.webservice.alfresco.IPunterSouthallService;
import com.puntersouthall.webservice.alfresco.ObjectFactory;
import com.puntersouthall.webservice.alfresco.PunterSouthallService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GetCreateTaskNumberDelegate implements JavaDelegate {

  private Expression schemeName;
  private Expression activity;
  private Expression subActivity;
  private Expression firstName;
  private Expression surname;
  private Expression doer;
  private Expression deadline;
  private Expression numberOfTasks;

  public void execute(DelegateExecution execution)  {
  
	ObjectFactory objectFactory = new ObjectFactory();	  
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	String deadlineString = dateFormat.format((Date) deadline.getValue(execution));
    // First Name and Surname can potentially be objects and not string if not entered in workflow4
	String firstNameString = (firstName.getValue(execution) instanceof String) ? (String) firstName.getValue(execution) : "";
	String surnameString = (surname.getValue(execution) instanceof String) ? (String) surname.getValue(execution) : "";
	
	CreateTaskNumberType ctnType = new CreateTaskNumberType();
	ctnType.setSchemeName(objectFactory.createCreateTaskNumberTypeSchemeName((String) schemeName.getValue(execution)));
	ctnType.setActivity(objectFactory.createCreateTaskNumberTypeActivity((String) activity.getValue(execution)));
	ctnType.setSubActivity(objectFactory.createCreateTaskNumberTypeSubActivity((String) subActivity.getValue(execution)));
	ctnType.setFirstName(objectFactory.createCreateTaskNumberTypeFirstName(firstNameString));
	ctnType.setSurname(objectFactory.createCreateTaskNumberTypeSurname(surnameString));
	ctnType.setDoer(objectFactory.createCreateTaskNumberTypeDoer((String) doer.getValue(execution)));
	ctnType.setDeadline(objectFactory.createCreateTaskNumberTypeDeadline(deadlineString));
	ctnType.setNumberOfTasks((Integer) numberOfTasks.getValue(execution));
	
    PunterSouthallService psService = new PunterSouthallService();
	IPunterSouthallService iPSService = psService.getBasicHttpBindingIPunterSouthallService();
        
    Integer taskNumber = iPSService.getCreateTaskNumber(ctnType);	
	if (taskNumber == 0) 
	{
		// 0 will be returned if task not created properly
		execution.setVariable("pswf_success", false);		
	}
	else
	{
		execution.setVariable("pswf_taskNumber", taskNumber);
		execution.setVariable("pswf_success", true);
	}
  }
}