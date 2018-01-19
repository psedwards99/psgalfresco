package com.puntersouthall.webscripts;

import com.puntersouthall.model.PunterSouthallWorkflowModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.*;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DiaryNoteRequestDeletionPost extends AbstractWebScript {

    private final Logger logger = Logger.getLogger(DiaryNoteRequestDeletionPost.class);

    /** Alfresco services */
    private ServiceRegistry serviceRegistry;
    private WorkflowService workflowService;
    private PersonService personService;
    private NodeService nodeService;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.workflowService = serviceRegistry.getWorkflowService();
        this.personService = serviceRegistry.getPersonService();
        this.nodeService = serviceRegistry.getNodeService();
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) {
        try {
            JSONObject json = getJSONObjectPostMethod(req);

            if (logger.isDebugEnabled()) {
                logger.debug("pswf_diaryTeamName              =" + json.get("pswf_diaryTeamName"));
                logger.debug("pswf_diaryActivity              =" + json.get("pswf_diaryActivity"));
                logger.debug("pswf_diarySubActivity           =" + json.get("pswf_diarySubActivity"));
                logger.debug("pswf_diaryDateCreated           =" + json.get("pswf_diaryDateCreated"));
                logger.debug("pswf_diayDateSetToLaunch        =" + json.get("pswf_diayDateSetToLaunch"));
                logger.debug("pswf_diaryExpectedDeadline      =" + json.get("pswf_diaryExpectedDeadline"));
                logger.debug("pswf_diaryDiaryNote             =" + json.get("pswf_diaryDiaryNote"));
                logger.debug("pswf_diaryDeletionReason        =" + json.get("pswf_diaryDeletionReason"));
                logger.debug("pswf_diaryDeletionRequestMadeBy =" + json.get("pswf_diaryDeletionRequestMadeBy"));
                logger.debug("pswf_diaryDecisionMadeBy        =" + json.get("pswf_diaryDecisionMadeBy"));
                logger.debug("pswf_diaryCentralID             =" + json.get("pswf_diaryCentralID"));
                logger.debug("pswf_diaryFirstName             =" + json.get("pswf_diaryFirstName"));
                logger.debug("pswf_diarySurname               =" + json.get("pswf_diarySurname"));
                logger.debug("pswf_diaryNino                  =" + json.get("pswf_diaryNino"));
                logger.debug("pswf_diaryDob                   =" + json.get("pswf_diaryDob"));
                logger.debug("pswf_diaryRelevantMemberID      =" + json.get("pswf_diaryRelevantMemberID"));
                logger.debug("pswf_diaryAdminSchemeName       =" + json.get("pswf_diaryAdminSchemeName"));
            }
            
            requestDiaryNoteEntryDeletion(json);
            
        } catch (JSONException e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (IOException e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private JSONObject getJSONObjectPostMethod(WebScriptRequest req) throws IOException, JSONException {
        HttpServletRequest r = WebScriptServletRuntime.getHttpServletRequest(req);

        InputStream jsonInputStream = r.getInputStream();

        return jsonInputStream == null ? null : new JSONObject(IOUtils.toString(jsonInputStream, "UTF-8"));

    }
    
    private void requestDiaryNoteEntryDeletion(final JSONObject json) {
        // Run under the system admin context so that we can launch the workflows ok within Alfresco
        AuthenticationUtil.runAsSystem(new RunAsWork<String>() 
        {
            public String doWork() throws JSONException {

                // The workflow package
                NodeRef workflowPackage = workflowService.createPackage(null);

                // Set initiator
                NodeRef initiator = personService.getPerson(json.get("pswf_diaryDeletionRequestMadeBy").toString());
                Serializable initiatorHomeFolder = nodeService.getProperty(initiator, ContentModel.PROP_HOMEFOLDER);

                // Set the parameters for the workflow
                Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>();
                // Create QNames for all properties
                QName qnameInitiator = QName.createQName(null, "initiator");
                QName qnameInitiatorHome = QName.createQName(null, "initiatorhome");
                workflowProps.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
                workflowProps.put(qnameInitiator, initiator);
                workflowProps.put(qnameInitiatorHome, initiatorHomeFolder);
                // Set the workflow properties
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_TEAM_NAME, json.get("pswf_diaryTeamName").toString());
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_ACTIVITY, json.get("pswf_diaryActivity").toString());
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_SUB_ACTIVITY, json.get("pswf_diarySubActivity").toString());
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_DATE_CREATED, convertToAlfrescoDate(json.get("pswf_diaryDateCreated").toString()));
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_DATE_SET_TO_LAUNCH, convertToAlfrescoDate(json.get("pswf_diayDateSetToLaunch").toString()));
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_EXPECTED_DEADLINE, convertToAlfrescoDate(json.get("pswf_diaryExpectedDeadline").toString()));
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_DIARY_NOTE, json.get("pswf_diaryDiaryNote").toString());
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_DELETION_REASON, json.get("pswf_diaryDeletionReason").toString());
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_DELETION_REQUEST_MADE_BY, json.get("pswf_diaryDeletionRequestMadeBy").toString());
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_DECISION_MADE_BY, json.get("pswf_diaryDecisionMadeBy").toString());
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_CENTRAL_ID, Integer.parseInt(json.get("pswf_diaryCentralID").toString()));
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_FIRSTNAME, json.get("pswf_diaryFirstName").toString());
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_SURNAME, json.get("pswf_diarySurname").toString());
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_NINO, json.get("pswf_diaryNino").toString());
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_DOB, convertToAlfrescoDate(json.get("pswf_diaryDob").toString()));
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_RELEVANT_MEMBER_ID, json.get("pswf_diaryRelevantMemberID").toString());
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_ADMIN_SCHEME_NAME, json.get("pswf_diaryAdminSchemeName").toString());
                workflowProps.put(PunterSouthallWorkflowModel.PROP_DIARY_NOTE_INDEX, Integer.parseInt(json.get("pswf_diaryNoteIndex").toString()));
                
                // Create workflow
                WorkflowDefinition workflowDefinition = workflowService.getDefinitionByName("activiti$DiaryNoteDeletionRequest");
                // Start the workflow
                WorkflowPath workflowPath = workflowService.startWorkflow(workflowDefinition.getId(), workflowProps);

                if (workflowPath.getId() != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("DiaryNoteRequestDeletionPost.requestDiaryNoteEntryDeletion: Workflow started successfully.");
                    }

                }
                return "OK";
            }
        });
    }
    
    private Date convertToAlfrescoDate(String date) {
        Date _date = null;
        if (!StringUtils.isEmpty(date)) {
            if (date.length() == "dd/MM/yyyy".length()) 
            {
                try {
                    _date = new SimpleDateFormat("dd/MM/yyyy").parse(date);
                } catch (ParseException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return _date;
    }
}
