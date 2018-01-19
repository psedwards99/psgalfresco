package com.puntersouthall.workflow.delegates;

import com.puntersouthall.webservice.alfresco.IPunterSouthallService;
import com.puntersouthall.webservice.alfresco.PunterSouthallService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

public class DiaryNoteCancelDeletionDelegate implements JavaDelegate {

	private Expression diaryNoteIndex;
  
	public void execute(DelegateExecution execution) {
  
		Integer diaryNoteIndexValue = (Integer) diaryNoteIndex.getValue(execution);
		  
	    PunterSouthallService psService = new PunterSouthallService();
		IPunterSouthallService iPSService = psService.getBasicHttpBindingIPunterSouthallService();
	        
		Boolean success = iPSService.diaryNoteCancelDeletion(diaryNoteIndexValue);	
		execution.setVariable("pswf_success", success);
	}
}