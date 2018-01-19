package com.puntersouthall.workflow.delegates;

import com.puntersouthall.webservice.alfresco.IPunterSouthallService;
import com.puntersouthall.webservice.alfresco.PunterSouthallService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class GetDeadlineDelegate implements JavaDelegate {

	private Expression activity;
	private Expression dateReceived;
	private Expression schemeName;
	private Expression subActivity;
	private Expression teamName;
	

  public void execute(DelegateExecution execution)  {
  
	//ObjectFactory objectFactory = new ObjectFactory();	  
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	String dateReceivedString = dateFormat.format((Date) dateReceived.getValue(execution));
    // First Name and Surname can potentially be objects and not string if not entered in workflow4
	
	/*GetDeadlineType ctnType = new GetDeadlineType();
	ctnType.setActivity((JAXBElement<String>) activity.getValue(execution));
	ctnType.setDateReceived(objectFactory.createGetDeadlineGetDeadlineTypeString(dateReceivedString));
	ctnType.setSchemeName((JAXBElement<String>) schemeName.getValue(execution));
	ctnType.setSubActivity((JAXBElement<String>) subActivity.getValue(execution));
	ctnType.setTeamName((JAXBElement<String>) teamName.getValue(execution));*/
	
	String ctnType = "{ \"activity\" : \"" + (String) activity.getValue(execution)
			+ "\", \"dateReceived\" : \"" // 29-06-2016" "
					+ dateReceivedString
			+ "\", \"schemeName\" : \"" + (String) schemeName.getValue(execution) 
			+ "\", \"subActivity\" : \"" + (String) subActivity.getValue(execution) 
			+ "\", \"teamName\" : \"" + (String) teamName.getValue(execution)
			+ "\" }";
	
	
    PunterSouthallService psService = new PunterSouthallService();
	IPunterSouthallService iPSService = psService.getBasicHttpBindingIPunterSouthallService();
	
	
	
    String deadline = iPSService.getDeadline(ctnType); 	
	
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

  
      Date deadlineDate = null;
	try {
		deadlineDate = formatter.parse(deadline);
	} catch (ParseException e) {
		deadline = null;
	}

	
	if (deadline == null) 
	{
		// 0 will be returned if task not created properly
		execution.setVariable("pswf_success", false);		
	}
	else
	{
		execution.setVariable("pswf_deadline", deadlineDate);
		execution.setVariable("pswf_internalDeadline", deadlineDate);
		execution.setVariable("pswf_success", true);
	}
  }
}