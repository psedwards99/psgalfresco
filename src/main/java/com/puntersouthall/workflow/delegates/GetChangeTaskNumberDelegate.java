package com.puntersouthall.workflow.delegates;

import com.puntersouthall.webservice.alfresco.ChangeTaskNumberType;
import com.puntersouthall.webservice.alfresco.IPunterSouthallService;
import com.puntersouthall.webservice.alfresco.ObjectFactory;
import com.puntersouthall.webservice.alfresco.PunterSouthallService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GetChangeTaskNumberDelegate implements JavaDelegate {

  private Expression schemeName;
  private Expression activity;
  private Expression subActivity;
  private Expression firstName;
  private Expression surname;
  private Expression doer;
  private Expression deadline;
  private Expression numberOfTasks;
  private Expression taskID;

  public void execute(DelegateExecution execution) {
  
	ObjectFactory objectFactory = new ObjectFactory();	  
	  
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	String deadlineString = dateFormat.format((Date) deadline.getValue(execution));

	// First Name and Surname can potentially be objects and not string if not entered in workflow4
	String firstNameString = (firstName.getValue(execution) instanceof String) ? (String) firstName.getValue(execution) : "";
	String surnameString = (surname.getValue(execution) instanceof String) ? (String) surname.getValue(execution) : "";
	  
	ChangeTaskNumberType ctnType = new ChangeTaskNumberType();
	ctnType.setSchemeName(objectFactory.createChangeTaskNumberTypeSchemeName((String) schemeName.getValue(execution)));
	ctnType.setActivity(objectFactory.createChangeTaskNumberTypeActivity((String) activity.getValue(execution)));
	ctnType.setSubActivity(objectFactory.createChangeTaskNumberTypeSubActivity((String) subActivity.getValue(execution)));
	ctnType.setFirstName(objectFactory.createChangeTaskNumberTypeFirstName(firstNameString));
	ctnType.setSurname(objectFactory.createChangeTaskNumberTypeSurname(surnameString));
	ctnType.setDoer(objectFactory.createChangeTaskNumberTypeDoer((String) doer.getValue(execution)));
	ctnType.setDeadline(objectFactory.createChangeTaskNumberTypeDeadline(deadlineString));
	ctnType.setNumberOfTasks((Integer) numberOfTasks.getValue(execution));
	ctnType.setTaskID((Integer) taskID.getValue(execution));
	  
    PunterSouthallService psService = new PunterSouthallService();
	IPunterSouthallService iPSService = psService.getBasicHttpBindingIPunterSouthallService();
        
	Boolean success = iPSService.getChangeTaskNumber(ctnType);	
	execution.setVariable("pswf_success", success);
  }
}