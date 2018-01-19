package com.puntersouthall.workflow.delegates;

import com.puntersouthall.webservice.alfresco.IPunterSouthallService;
import com.puntersouthall.webservice.alfresco.PunterSouthallService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

public class DiaryNoteApproveDeletionDelegate implements JavaDelegate {

	private Expression approvedBy;
	private Expression diaryNoteIndex;
  
	public void execute(DelegateExecution execution) {
  
		String approvedByValue = (String) approvedBy.getValue(execution);
		Integer diaryNoteIndexValue = (Integer) diaryNoteIndex.getValue(execution);
		  
	    PunterSouthallService psService = new PunterSouthallService();
		IPunterSouthallService iPSService = psService.getBasicHttpBindingIPunterSouthallService();
	        
		Boolean success = iPSService.diaryNoteApproveDeletion(diaryNoteIndexValue, approvedByValue);	
		execution.setVariable("pswf_success", success);
	}
}