package com.puntersouthall.model;

import org.alfresco.service.namespace.QName;

public interface PunterSouthallFormsModel {
	
	
	/**
	 * NamespaceService
	 */
	static final String NS_MODEL_URI = "PSAL.Forms.Model/1.0";
	static final String NS_MODEL_PREFIX = "psform";
	
	/**
	 * Types
	 */
	static final QName TYPE_FORM = QName.createQName(NS_MODEL_URI, "form");
	static final QName TYPE_NEW_PAYROLL_STARTER_FORM = QName.createQName(NS_MODEL_URI, "newPayrollStarterForm");

	/**
	 * Aspects
	 */
	static final QName ASPECT_GENERAL_FORM = QName.createQName(NS_MODEL_URI, "generalFormAspect");
	static final QName ASPECT_CHECK_STAGE_1 = QName.createQName(NS_MODEL_URI, "checkStage1");
	static final QName ASPECT_CHECK_STAGE_2 = QName.createQName(NS_MODEL_URI, "checkStage2");
/**
	 * Properties
	 */
	static final QName PROP_FORM_ID = QName.createQName(NS_MODEL_URI, "formID");		
	static final QName PROP_FORM_STATUS = QName.createQName(NS_MODEL_URI, "formStatus");	
	static final QName PROP_DOER = QName.createQName(NS_MODEL_URI, "formDoer");	
	static final QName PROP_DOER_TIMESTAMP = QName.createQName(NS_MODEL_URI, "formDoerTimeStamp");	
	static final QName PROP_CHECKER_STAGE_1 = QName.createQName(NS_MODEL_URI, "checkerStage1");	
	static final QName PROP_CHECKER_STAGE_1_TIMESTAMP = QName.createQName(NS_MODEL_URI, "checkerStage1TimeStamp");	
	static final QName PROP_CHECKER_STAGE_2 = QName.createQName(NS_MODEL_URI, "checkerStage2");	
	static final QName PROP_CHECKER_STAGE_2_TIMESTAMP = QName.createQName(NS_MODEL_URI, "checkerStage2TimeStamp");		

	/**
	 * Constraints
	 */
	//static final QName CONS_PAYMENT_PERIOD = QName.createQName(NS_MODEL_URI, "paymentPeriod");
	
	/**
	 * Associations
	 */
//	static final QName ASSOC_TITLE = QName.createQName(NS_MODEL_URI, "title");
}
