package com.puntersouthall.workflow.delegates;

import com.puntersouthall.webservice.alfresco.IPunterSouthallService;
import com.puntersouthall.webservice.alfresco.PunterSouthallService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

public class GetLogOffTaskDelegate implements JavaDelegate { 

  private Expression taskNumber;

  public void execute(DelegateExecution execution) {
    
    PunterSouthallService psService = new PunterSouthallService();
	IPunterSouthallService iPSService = psService.getBasicHttpBindingIPunterSouthallService();
        
    Boolean success = iPSService.getLogOffTask((Integer) taskNumber.getValue(execution));
    execution.setVariable("pswf_success", success);
  }
}