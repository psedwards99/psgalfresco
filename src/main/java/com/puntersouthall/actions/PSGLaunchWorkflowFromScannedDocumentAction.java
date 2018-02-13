package com.puntersouthall.actions;

import com.puntersouthall.helper.NodeServiceHelper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class PSGLaunchWorkflowFromScannedDocumentAction extends ActionExecuterAbstractBase {

    /** the logger */
    private final Log logger = LogFactory.getLog(PSGLaunchWorkflowFromScannedDocumentAction.class);

    /** workflow properties */
    private String HOLDING_FOLDER_PATH;

    private String REST_URI;
    private String USERNAME;
    private String PASSWORD;
    private String REPOSITORY_NAME_USED_BY_ACTIVITI;
    private String PROCESS_NAME;
    private String PROCESS_DEFINITION_KEY;
    private String ATTACHMENT_FORM_FIELD;

    /** Alfresco services */
    private ServiceRegistry serviceRegistry;
    private NodeService nodeService;


    private FileFolderService fileFolderService;
    private SearchService searchService;

    
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
        this.fileFolderService = serviceRegistry.getFileFolderService();
        this.searchService = serviceRegistry.getSearchService();
    }

    public void setHOLDING_FOLDER_PATH(String holding_folder_path) {

        HOLDING_FOLDER_PATH = holding_folder_path;
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

    @Override
    protected void executeImpl(Action action, final NodeRef documentNodeRef) {

    	
        // Run under the system admin context
        AuthenticationUtil.runAsSystem(new RunAsWork<String>() {
            public String doWork() {     

	                    // get the name of the folder the document is in (ths is the Office name)
	                    String office =  (String) nodeService.getProperty(nodeService.getPrimaryParent(documentNodeRef).getParentRef(),ContentModel.PROP_NAME);
	                    if (logger.isDebugEnabled()){
	                    	logger.debug("Office name : " + office );
	                    }
	                    PROCESS_NAME = "Scanning - " + office.trim();

	                    // Get the target folder noderef where the document will be moved to
	                    NodeRef targetFolderNoderef = null;
	                    List<String> pathElements = Arrays.asList(StringUtils.split(HOLDING_FOLDER_PATH, '/'));
	
	                    FileInfo fileInfo = null;
	                    try {
	                    	
	                        fileInfo = fileFolderService.resolveNamePath(NodeServiceHelper.getCompanyHome(searchService), pathElements);
	                        // Get the target folder noderef
	                        targetFolderNoderef = fileInfo.getNodeRef();
	                        if (logger.isDebugEnabled()){
	                            logger.debug("Attachments folder : "+targetFolderNoderef);
	                        }


                            // Document must be linked to Activiti before it can be added to a workflow.
                            String contentID = uploadContentToActiviti(documentNodeRef);

	                        // move the node to the folder "Uploads/Holding Folder"
	                        nodeService.moveNode(documentNodeRef, targetFolderNoderef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) nodeService.getProperty(documentNodeRef,ContentModel.PROP_NAME)));


	                        // Source system must be in the form "scanningWokingham"
                            // It is used in the workflow script on Task Configure Post Team
                            String sourceSystem="scanning"+office.trim();


                            // Create workflow

                            JSONObject startProcessBody = new JSONObject();
                            startProcessBody.put("processDefinitionKey",PROCESS_DEFINITION_KEY);
                            startProcessBody.put("name",PROCESS_NAME);

                            JSONObject startFormBody = new JSONObject();
                            startFormBody.put("name","Attached Document");
                            startFormBody.put(ATTACHMENT_FORM_FIELD,contentID);
                            startFormBody.put("sourcesystem",sourceSystem);

                            startProcessBody.put("values",startFormBody);

                            logger.info("About to start workflow. JSON Body : "+startProcessBody.toString());
                            JSONObject contentObject = doJsonPost("api/enterprise/process-instances", startProcessBody);

                            // Do something to check if the WF started correctly
                            logger.debug("Return from start : " + contentObject.toString());

	                    } catch (InvalidStoreRefException e) {
                            logger.error(e.getMessage());
                            e.printStackTrace();
	                    } catch (FileNotFoundException e) {
                            logger.error(e.getMessage());
                            e.printStackTrace();
	                    }
                        catch (JSONException je) {
                            logger.error(je.getMessage());
                            je.printStackTrace();
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

            String authString=USERNAME+":"+PASSWORD;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String encodedAuthString = new String(authEncBytes);

            conn.setRequestProperty("Authorization", "Basic " + encodedAuthString);
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
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// TODO Auto-generated method stub
		
	}
	
}
