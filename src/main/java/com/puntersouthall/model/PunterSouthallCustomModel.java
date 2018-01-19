package com.puntersouthall.model;

import org.alfresco.service.namespace.QName;

public interface PunterSouthallCustomModel {

	/**
	 * NamespaceService
	 */
	static final String NS_MODEL_URI = "custom.model.jb";
	static final String NS_MODEL_PREFIX = "psdoc";
	
	/**
	 * Types
	 */

	static final QName TYPE_DOCUMENT = QName.createQName(NS_MODEL_URI, "document");
	static final QName TYPE_ADMIN_MEMBER_DOCUMENT = QName.createQName(NS_MODEL_URI, "adminMemberDocument");
	static final QName TYPE_ADMIN_SCHEME_DOCUMENT = QName.createQName(NS_MODEL_URI, "adminSchemeDocument");
	static final QName TYPE_PAYROLL_MEMBER_DOCUMENT = QName.createQName(NS_MODEL_URI, "payrollMemberDocument");
	static final QName TYPE_PAYROLL_SCHEME_DOCUMENT = QName.createQName(NS_MODEL_URI, "payrollSchemeDocument");
	static final QName TYPE_CASHIERING_MEMBER_DOCUMENT = QName.createQName(NS_MODEL_URI, "cashieringMemberDocument");
	static final QName TYPE_CASHIERING_SCHEME_DOCUMENT = QName.createQName(NS_MODEL_URI, "cashieringSchemeDocument");
	static final QName TYPE_ADMIN_SCHEME_CONTROL_DOCUMENT = QName.createQName(NS_MODEL_URI, "adminSchemeControlDocument");
	
	/**
	 * Aspects
	 */
	static final QName ASPECT_MY_PENSION = QName.createQName(NS_MODEL_URI, "myPensionAspect");
	static final QName ASPECT_WORKFLOW = QName.createQName(NS_MODEL_URI, "workflowAspect");
	static final QName ASPECT_ADMIN_SCHEME_CONTROL_DOCUMENT = QName.createQName(NS_MODEL_URI, "adminSchemeControlDocumentAspect");
	static final QName ASPECT_PERIOD = QName.createQName(NS_MODEL_URI, "periodAspect");
	/**
	 * Properties
	 */
	static final QName PROP_CENTRAL_ID = QName.createQName(NS_MODEL_URI, "centralID");		
	static final QName PROP_DOC_DATE = QName.createQName(NS_MODEL_URI, "docDate");	
	static final QName PROP_DOB = QName.createQName(NS_MODEL_URI, "dob");
	static final QName PROP_FIRST_NAME = QName.createQName(NS_MODEL_URI, "firstName");
	static final QName PROP_NINO = QName.createQName(NS_MODEL_URI, "nino");
	static final QName PROP_SCHEME_ID = QName.createQName(NS_MODEL_URI, "schemeID");	
	static final QName PROP_SURNAME = QName.createQName(NS_MODEL_URI, "surname");	
	static final QName PROP_TYPE = QName.createQName(NS_MODEL_URI, "type");
	static final QName PROP_TITLE = QName.createQName(NS_MODEL_URI, "title");	
	static final QName PROP_WEB_DOC_TYPE = QName.createQName(NS_MODEL_URI, "webDocType");
	static final QName PROP_CLIENT_ID = QName.createQName(NS_MODEL_URI, "clientID");
	static final QName PROP_CLIENT_NAME = QName.createQName(NS_MODEL_URI, "clientName");
	static final QName PROP_SCHEME_NAME = QName.createQName(NS_MODEL_URI, "schemeName");
	static final QName PROP_MEMBER_RECORD_ID = QName.createQName(NS_MODEL_URI, "memberRecordID");
	static final QName PROP_ACTIVITY = QName.createQName(NS_MODEL_URI, "activity");
	static final QName PROP_INITIATING_DOC = QName.createQName(NS_MODEL_URI, "initiatingDoc");
	static final QName PROP_SUB_ACTIVITY = QName.createQName(NS_MODEL_URI, "subActivity");
	static final QName PROP_SCHEME_CONTROL_ADMIN_SCHEME = QName.createQName(NS_MODEL_URI, "schemeControlAdminScheme");
	static final QName PROP_PERIOD_MONTH = QName.createQName(NS_MODEL_URI, "periodMonth");
	static final QName PROP_PERIOD_YEAR = QName.createQName(NS_MODEL_URI, "periodYear");
	/**
	 * Constraints
	 * 
	 */
	//static final QName CONS_PAYMENT_PERIOD = QName.createQName(NS_MODEL_URI, "paymentPeriod");
	
	/**
	 * Associations
	 */
//	static final QName ASSOC_TITLE = QName.createQName(NS_MODEL_URI, "title");
}
