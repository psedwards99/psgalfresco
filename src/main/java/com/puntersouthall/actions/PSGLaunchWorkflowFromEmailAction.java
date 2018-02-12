package com.puntersouthall.actions;

import com.puntersouthall.helper.NodeServiceHelper;
import com.puntersouthall.helper.PSErrorLoggingHelper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;


public class PSGLaunchWorkflowFromEmailAction extends ActionExecuterAbstractBase {

    /**
     * the logger
     */
    private final Log logger = LogFactory.getLog(LaunchWorkflowFromEmailAction.class);

    /**
     * Alfresco services
     */
    private ServiceRegistry serviceRegistry;
    private NodeService nodeService;
    private PersonService personService;
    private FileFolderService fileFolderService;
    private SearchService searchService;
    private AuthenticationService authenticationService;

    /**
     * workflow properties
     */
    private String PROP_SOURCE_SYSTEM;
    private String PROP_ADMIN_SCHEME_GROUP;
    private String ATTACHMENTS_TARGET_FOLDER_PATH;
    private String ARCHIVED_EMAIL_FOLDER_PATH;

    private String REST_URI;
    private String USERNAME;
    private String PASSWORD;
    private String REPOSITORY_NAME_USED_BY_ACTIVITI;
    private String PROCESS_NAME = "Email Received";
    private String PROCESS_DEFINITION_KEY;
    private String ATTACHMENT_FORM_FIELD;

    /*----------------------------------------------------------------------------------------------------------------*/
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
        this.personService = serviceRegistry.getPersonService();
        this.fileFolderService = serviceRegistry.getFileFolderService();
        this.searchService = serviceRegistry.getSearchService();
        this.authenticationService = serviceRegistry.getAuthenticationService();
    }

    public void setPROP_SOURCE_SYSTEM(String pROP_SOURCE_SYSTEM) {
        PROP_SOURCE_SYSTEM = pROP_SOURCE_SYSTEM;
    }

    public void setPROP_ADMIN_SCHEME_GROUP(String pROP_ADMIN_SCHEME_GROUP) {
        PROP_ADMIN_SCHEME_GROUP = pROP_ADMIN_SCHEME_GROUP;
    }

    public void setATTACHMENTS_TARGET_FOLDER_PATH(String aTTACHMENTS_TARGET_FOLDER_PATH) {
        ATTACHMENTS_TARGET_FOLDER_PATH = aTTACHMENTS_TARGET_FOLDER_PATH;
    }

    public void setARCHIVED_EMAIL_FOLDER_PATH(String aRCHIVED_EMAIL_FOLDER_PATH) {
        ARCHIVED_EMAIL_FOLDER_PATH = aRCHIVED_EMAIL_FOLDER_PATH;
    }
    public void setREST_URI(String restURI) {
        REST_URI = restURI;
    }

    public void setPROCESS_DEFINITION_KEY(String processDefKey) {
        PROCESS_DEFINITION_KEY = processDefKey;
    }
    public void setREPOSITORY_NAME_USED_BY_ACTIVITI(String repositoryName) {
        REPOSITORY_NAME_USED_BY_ACTIVITI = repositoryName;
    }

    public void setUSERNAME(String username) {
        USERNAME = username;
    }
    public void setPASSWORD(String password) {
        PASSWORD = password;
    }

    //The name of the field on the start form that accepts file uploads
    public void setATTACHMENT_FORM_FIELD(String formFieldName) {
        ATTACHMENT_FORM_FIELD = formFieldName;
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        // TODO Auto-generated method stub

    }

    /*----------------------------------------------------------------------------------------------------------------*/
    @Override
    protected void executeImpl(Action action, final NodeRef theEmailNodeRef) {


        // Run under the system admin context so that we can launch the workflows ok within Alfresco
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<String>() {
            public String doWork() {

                // get logged in user
                String loggedInUser = authenticationService.getCurrentUserName();

                // Check if the node has the email aspect
                if (nodeService.hasAspect(theEmailNodeRef, ContentModel.ASPECT_EMAILED)) {

                    // Set initiator as the creator
                    NodeRef initiator = personService.getPerson((String) nodeService.getProperty(theEmailNodeRef, ContentModel.PROP_CREATOR));
                    Serializable initiatorHomeFolder = nodeService.getProperty(initiator, ContentModel.PROP_HOMEFOLDER);

                    //Get the parent (MyPension Emails) folder where the email enters alfresco.
                    NodeRef parentFolder = nodeService.getPrimaryParent(theEmailNodeRef).getParentRef();

                    // Get the attachments from the email node
                    List<AssociationRef> assocs = nodeService.getTargetAssocs(theEmailNodeRef, ContentModel.ASSOC_ATTACHMENTS);


                    // Get the target folder noderef where move the attachments
                    NodeRef targetFolderNoderef = null;
                    List<String> pathElements = Arrays.asList(StringUtils.split(ATTACHMENTS_TARGET_FOLDER_PATH, '/'));

                    FileInfo fileInfo = null;
                    try {

                        String emailSource = (String) nodeService.getProperty(theEmailNodeRef, ContentModel.PROP_ORIGINATOR);

                        fileInfo = fileFolderService.resolveNamePath(NodeServiceHelper.getCompanyHome(searchService), pathElements);
                        // Get the target folder noderef
                        targetFolderNoderef = fileInfo.getNodeRef();
                        logger.debug("Attachments folder : " + targetFolderNoderef);

                        // check if there is an html version of the email stored as one of the attachments. (Usually if email was sent as html)
                        String theEmailName = (String) nodeService.getProperty(theEmailNodeRef, ContentModel.PROP_NAME);
                        Boolean hasHTMLEmailVersion = false;

                        HTMLcheck:
                        for (AssociationRef assoc : assocs) {
                            NodeRef attachedNodeRef = assoc.getTargetRef();
                            String attachmentNodeName = (String) nodeService.getProperty(attachedNodeRef, ContentModel.PROP_NAME);
                            // check that the attachment has the same name without the "(Part 1)" text and that it ends .html
                            if (attachmentNodeName.toLowerCase().contains(theEmailName.toLowerCase()) && attachmentNodeName.endsWith(".html")) {
                                hasHTMLEmailVersion = true;
                                break HTMLcheck;
                            }
                        }


                        // Upload all attachments to activiti
                        // The uplaod process returns an activiti content ID that we use in the workflow start process
                        String contentIDList = "";
                        for (AssociationRef assoc : assocs) {

                            NodeRef theAttachmentNodeRef = assoc.getTargetRef();
                            String contentID = uploadContentToActiviti(theAttachmentNodeRef);
                            if (!contentID.equals("")) {
                                // Add to list of content to be attached to started process
                                if (contentIDList.equals("")) {
                                    contentIDList = contentID;
                                } else {
                                    contentIDList = contentIDList + "," + contentID;
                                }
                            }
                            // move the node to the folder "Uploads/Holding Folder"
                            nodeService.moveNode(theAttachmentNodeRef, targetFolderNoderef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) nodeService.getProperty(theEmailNodeRef, ContentModel.PROP_NAME)));
                        }

                        // mypension.com send the emails as plain text or alfresco interprets them as plain text which is just one file
                        if (!hasHTMLEmailVersion) {
                            if(emailSource==null) emailSource="";
                            if (emailSource.equals("mypension@puntersouthall.com") || emailSource.equals("noreply@mypension.com") || emailSource.equals("dsapp14@mypension.com")|| emailSource.equals("")) {

                                //change the mime type of incoming mypension email to html so it displays correctly
                                ContentData cd = (ContentData) nodeService.getProperty(theEmailNodeRef, ContentModel.PROP_CONTENT);
                                ContentData newCD = ContentData.setMimetype(cd, "text/html");
                                nodeService.setProperty(theEmailNodeRef, ContentModel.PROP_CONTENT, newCD);
                            }
                            //no html file so add the emailed in file to workflow package too and dont archive it
                            String contentID = uploadContentToActiviti(theEmailNodeRef);
                            if (!contentID.equals("")) {
                                // Add to list of content to be attached to started process
                                if (contentIDList.equals("")) {
                                    contentIDList = contentID;
                                } else {
                                    contentIDList = contentIDList + "," + contentID;
                                }
                            }
                            // move the node to the folder "Uploads/Holding Folder"
                            nodeService.moveNode(theEmailNodeRef, targetFolderNoderef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) nodeService.getProperty(theEmailNodeRef, ContentModel.PROP_NAME)));
                        }

                        // Create workflow

                        JSONObject startProcessBody = new JSONObject();
                        startProcessBody.put("processDefinitionKey",PROCESS_DEFINITION_KEY);
                        startProcessBody.put("name",PROCESS_NAME);
                        JSONObject startProcessContentID = new JSONObject();
                        startProcessContentID.put("name","attached document");
                        startProcessContentID.put(ATTACHMENT_FORM_FIELD,contentIDList);

                        startProcessBody.put("values",startProcessContentID);

                        logger.info("About to start workflow. JSON Body : "+startProcessBody.toString());
                        JSONObject contentObject = doJsonPost("api/enterprise/process-instances", startProcessBody);

                        // Do something to check if the WF started correctly
                        logger.debug("Return from start : " + contentObject.toString());

                    } catch (InvalidStoreRefException e) {
                        //logger.error(e.getMessage());
                        PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(theEmailNodeRef).toString(), e.getMessage().toString());
                    } catch (FileNotFoundException e) {
                        //logger.error(e.getMessage());
                        PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(theEmailNodeRef).toString(), e.getMessage().toString());
                    } catch (JSONException je) {
                        PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(theEmailNodeRef).toString(), je.getMessage().toString());
                    }
                }

                return "OK";
            }

        });
    }
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Creates a request body as JSON for linking an Alfresco document to Activiti
     * then calls doJsonPost to send the request
     * @param theContent
     * @return The ID of the uploaded content
     * @throws JSONException
     */
    private String uploadContentToActiviti(NodeRef theContent) throws JSONException {

        FileInfo fileInfo = fileFolderService.getFileInfo(theContent);
        if ((!fileInfo.isFolder()) && (!fileInfo.isLink())) {
            String sourceId = theContent.getId();
            JSONObject body = new JSONObject();
            body.put("source", REPOSITORY_NAME_USED_BY_ACTIVITI);
            body.put("sourceId", sourceId);
            body.put("name", fileInfo.getName());
            body.put("mimeType", JSONObject.NULL);
            body.put("simpleType", JSONObject.NULL);
            body.put("link", false);

            JSONObject contentObject = doJsonPost("api/enterprise/content", body);
            // The call to post returns JSON that contains the ID of the uploaded file
            // THis is the ID that we need to use in the start process call
            if (contentObject !=null) {
                int contentId = contentObject.getInt("id");
                logger.debug("Content ID returned after upload : " + contentId);
                return String.valueOf(contentId);
            }
            else {
                return "";
            }

        } else {
            return "";
        }
    }
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * POSTs the request to Activiti
     * @param urlPath
     * @param body
     * @return JSONObject containing the response from Activiti
     */
    private JSONObject doJsonPost(String urlPath, JSONObject body) {

        JSONObject jsonResponse = null;
        try {
            URL url = new URL(REST_URI +"/"+urlPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Basic YWRtaW5AYXBwLmFjdGl2aXRpLmNvbTphZG1pbg==");
            conn.setRequestProperty("Content-Type", "application/json");

            String requestBody = body.toString();
            logger.debug("JSON Body : "+requestBody);

            // Send the request
            OutputStream os = conn.getOutputStream();
            os.write(requestBody.getBytes());
            os.flush();

            // Get the response code
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            // Read the response
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String outputLine;
            String response="";
            logger.debug("Response from Activiti .... ");
            while ((outputLine = br.readLine()) != null) {
                logger.debug(outputLine);
                response = response+outputLine;
            }

            conn.disconnect();
            jsonResponse = new JSONObject(response);


        } catch (MalformedURLException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }
        return jsonResponse;

    }

}
