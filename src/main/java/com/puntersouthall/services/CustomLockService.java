package com.puntersouthall.services;

import org.alfresco.service.cmr.repository.NodeRef;

public interface CustomLockService {

	/**
	 * This method lock a given nodeRef in Alfresco
	 * 
	 * @param nodeDocument
	 * @return true if the lock is made, false if it fails
	 */
	public boolean lockNode(NodeRef nodeDocument);
}
