package com.puntersouthall.model;

import org.alfresco.service.namespace.QName;

public interface PunterSouthallWorkflowModel {

	/**
	 * NamespaceService
	 */
	
	static final String NS_MODEL_URI = "http://www.puntersouthall.com/model/workflow/1.0";
	static final String NS_MODEL_PREFIX = "pswf";
	
	/**
	 * Types
	 */
	
	/**
	 * Aspects
	 */
	
	/**
	 * Properties
	 */
	
	static final QName PROP_ACTIVITY = QName.createQName(NS_MODEL_URI, "activity");	
	static final QName PROP_ADMIN_SCHEME_ID = QName.createQName(NS_MODEL_URI, "adminSchemeID");
	static final QName PROP_ADMIN_SCHEME_NAME = QName.createQName(NS_MODEL_URI, "adminSchemeName");
	static final QName PROP_ADMIN_SCHEME_GROUP = QName.createQName(NS_MODEL_URI, "adminSchemeGroup");
	static final QName PROP_CENTRAL_ID = QName.createQName(NS_MODEL_URI, "centralID");
	static final QName PROP_CLIENT_ID = QName.createQName(NS_MODEL_URI, "clientID");
	static final QName PROP_CLIENT_NAME = QName.createQName(NS_MODEL_URI, "clientName");
	static final QName PROP_COMMENT = QName.createQName(NS_MODEL_URI, "comment");
	static final QName PROP_DATE_RECEIVED = QName.createQName(NS_MODEL_URI, "dateReceived");
	static final QName PROP_DEADLINE = QName.createQName(NS_MODEL_URI, "deadline");
	static final QName PROP_DOB = QName.createQName(NS_MODEL_URI, "dob");
	static final QName PROP_FIRST_NAME = QName.createQName(NS_MODEL_URI, "firstName");
	static final QName PROP_INTERNAL_DEADLINE = QName.createQName(NS_MODEL_URI, "internalDeadline");
	static final QName PROP_NINO = QName.createQName(NS_MODEL_URI, "nino");
	static final QName PROP_RELEVANT_MEMBER_ID = QName.createQName(NS_MODEL_URI, "relevantMemberID");
	static final QName PROP_SOURCE_SYSTEM = QName.createQName(NS_MODEL_URI, "sourceSystem");
	static final QName PROP_SUB_ACTIVITY = QName.createQName(NS_MODEL_URI, "subActivity");
	static final QName PROP_SURNAME = QName.createQName(NS_MODEL_URI, "surname");
	static final QName PROP_TEAM_NAME = QName.createQName(NS_MODEL_URI, "teamName");
	static final QName PROP_WORKFLOW_TASK_TYPE = QName.createQName(NS_MODEL_URI, "workflowTaskType");
	
	// Diary Note
	static final QName PROP_DIARY_TEAM_NAME = QName.createQName(NS_MODEL_URI, "diaryTeamName");
	static final QName PROP_DIARY_ACTIVITY = QName.createQName(NS_MODEL_URI, "diaryActivity");
	static final QName PROP_DIARY_SUB_ACTIVITY = QName.createQName(NS_MODEL_URI, "diarySubActivity");
	static final QName PROP_DIARY_DATE_CREATED = QName.createQName(NS_MODEL_URI, "diaryDateCreated");
	static final QName PROP_DIARY_DATE_SET_TO_LAUNCH = QName.createQName(NS_MODEL_URI, "diaryDateSetToLaunch");
    static final QName PROP_DIARY_EXPECTED_DEADLINE = QName.createQName(NS_MODEL_URI, "diaryExpectedDeadline");
    static final QName PROP_DIARY_DIARY_NOTE = QName.createQName(NS_MODEL_URI, "diaryDiaryNote");
    static final QName PROP_DIARY_DELETION_REASON = QName.createQName(NS_MODEL_URI, "diaryDeletionReason");
    static final QName PROP_DIARY_DELETION_APPROVAL = QName.createQName(NS_MODEL_URI, "diaryDeletionApproval");
    static final QName PROP_DIARY_DELETION_REQUEST_MADE_BY = QName.createQName(NS_MODEL_URI, "diaryDeletionRequestMadeBy");
    static final QName PROP_DIARY_DECISION_MADE_BY = QName.createQName(NS_MODEL_URI, "diaryDecisionMadeBy");
    static final QName PROP_DIARY_CENTRAL_ID = QName.createQName(NS_MODEL_URI, "diaryCentralID");
    static final QName PROP_DIARY_FIRSTNAME = QName.createQName(NS_MODEL_URI, "diaryFirstName");
    static final QName PROP_DIARY_SURNAME = QName.createQName(NS_MODEL_URI, "diarySurname");
    static final QName PROP_DIARY_NINO = QName.createQName(NS_MODEL_URI, "diaryNino");
    static final QName PROP_DIARY_DOB = QName.createQName(NS_MODEL_URI, "diaryDob");
    static final QName PROP_DIARY_RELEVANT_MEMBER_ID = QName.createQName(NS_MODEL_URI, "diaryRelevantMemberID");
    static final QName PROP_DIARY_ADMIN_SCHEME_NAME = QName.createQName(NS_MODEL_URI, "diaryAdminSchemeName");
    static final QName PROP_DIARY_NOTE_INDEX = QName.createQName(NS_MODEL_URI, "diaryNoteIndex");

	
	/**
	 * Constraints
	 */
	
	/**
	 * Associations
	 */
}
