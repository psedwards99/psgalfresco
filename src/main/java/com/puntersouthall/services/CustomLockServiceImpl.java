package com.puntersouthall.services;

import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;

public class CustomLockServiceImpl extends BaseProcessorExtension implements CustomLockService {
	
	private final Logger logger = Logger.getLogger(CustomLockServiceImpl.class);
	private ServiceRegistry serviceRegistry;
	private LockService lockService;

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
		lockService = serviceRegistry.getLockService();
		
	}
	
/*	public void setLockService(LockService lockService){
		this.lockService = lockService;
	}*/
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean lockNode(NodeRef nodeDocument) {

		// Lock the docuement if there is not lock
		if (lockService.getLockStatus(nodeDocument).equals(LockStatus.NO_LOCK)) {
			lockService.lock(nodeDocument, LockType.READ_ONLY_LOCK);

			logger.info("File has been locked - " + nodeDocument);
			return true;
		}

		logger.info("File has NOT been locked - " + nodeDocument);	
		return false;
	}


}
