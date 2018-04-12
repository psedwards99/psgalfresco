package com.puntersouthall.schedule;

import com.puntersouthall.model.PunterSouthallCustomModel;
import com.puntersouthall.webservice.alfresco.ArrayOfCentralMemberRecordChangeType;
import com.puntersouthall.webservice.alfresco.CentralMemberRecordChangeType;
import com.puntersouthall.webservice.alfresco.IPunterSouthallService;
import com.puntersouthall.webservice.alfresco.PunterSouthallService;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.VmShutdownListener.VmShutdownException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class calls a webservice which returns a list of updated members from the CMDB
 * For each updated member, the class updates any existing document metadata and
 * calls APS to update any running workflows relating to the updated member
 * 
 * @author pedwards
 *
 */
public class PSGDocumentUpdater {
	private static Log logger = LogFactory.getLog(PSGDocumentUpdater.class);
	private NodeRef logFileNodeRef;
	private ServiceRegistry serviceRegistry;
	//private SearchService searchService;
	//private NodeService nodeService;
	//private ContentService contentService;
	private JobLockService jobLockService;

    private String REST_URI;
    private String USERNAME;
    private String PASSWORD;

	private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "PSGDocumentUpdater");
	private static final long LOCK_TTL = 30000L;

	//************************************************************************************************
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
		//this.searchService = serviceRegistry.getSearchService();
		//this.nodeService = serviceRegistry.getNodeService();
		//this.contentService = serviceRegistry.getContentService();
		this.jobLockService = serviceRegistry.getJobLockService();
	}
	
	public ServiceRegistry getServiceRegistry() {
		return this.serviceRegistry;
	}


    public void setUSERNAME(String username) {

        USERNAME = username;
    }
    public void setPASSWORD(String password) {

        PASSWORD = password;
    }
    public void setREST_URI(String restURI) {

        REST_URI = restURI;
    }
	//************************************************************************************************
	/**
	 * Acquire the job lock
	 */
	private String acquireLock(JobLockRefreshCallback lockCallback)
	{
		// Get lock
		String lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL);

		// Register the refresh callback which will keep the lock alive
		jobLockService.refreshLock(lockToken, LOCK_QNAME, LOCK_TTL, lockCallback);

		if (logger.isDebugEnabled())
		{
			logger.debug("lock acquired: " + LOCK_QNAME + ": " + lockToken);
		}

		return lockToken;
	}
	//************************************************************************************************
	/**
	 * Release the lock after the job completes
	 */
	private void releaseLock(LockCallback lockCallback, String lockToken)
	{
		if (lockCallback != null)
		{
			lockCallback.running.set(false);
		}

		if (lockToken != null)
		{
			jobLockService.releaseLock(lockToken, LOCK_QNAME);
			if (logger.isDebugEnabled())
			{
				logger.debug("Lock released: " + LOCK_QNAME + ": " + lockToken);
			}
		}
	}
	//************************************************************************************************
	private class LockCallback implements JobLockRefreshCallback
	{
		final AtomicBoolean running = new AtomicBoolean(true);

		@Override
		public boolean isActive()
		{
			return running.get();
		}

		@Override
		public void lockReleased()
		{
			running.set(false);
			if (logger.isDebugEnabled())
			{
				logger.debug("Lock release notification: " + LOCK_QNAME);
			}
		}
	}

	//************************************************************************************************
	/**
	 * Executer implementation
	 */
	public void execute() {

        LockCallback lockCallback = new LockCallback();
        String lockToken = null;
        try {
            lockToken = acquireLock(lockCallback);


            logger.debug("PSGDocumentUpdater has started....");
            LockService lockService = serviceRegistry.getLockService();
            CheckOutCheckInService checkOutCheckInService = serviceRegistry.getCheckOutCheckInService();
            NodeService nodeService = serviceRegistry.getNodeService();
            SearchService searchService = serviceRegistry.getSearchService();
            StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

            Boolean endOfDayUpdate = false;
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour >= 20) {
                endOfDayUpdate = true;
            }

            PunterSouthallService psService = new PunterSouthallService();
            IPunterSouthallService iPSService = psService.getBasicHttpBindingIPunterSouthallService();
            ArrayOfCentralMemberRecordChangeType changeRecords = iPSService.getCentralMemberRecordsChanges(endOfDayUpdate);

            for (int x = 0; x < changeRecords.getCentralMemberRecordChangeType().size(); x++) {
                CentralMemberRecordChangeType changeRecord = changeRecords.getCentralMemberRecordChangeType().get(x);
                Integer changesID = changeRecord.getChangesID();
                Integer centralMemberRecordID = changeRecord.getCentralMemberRecordID();
                ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "@psdoc\\:centralID:" + centralMemberRecordID.toString());

                String firstName = changeRecord.getMemberFirstName().getValue();
                String surname = changeRecord.getMemberSurname().getValue();
                String nino = changeRecord.getMemberNINumber().getValue();
                GregorianCalendar memberDOB = getGregorianCalendarForXMLGregorianCalendar(changeRecord.getMemberDateOfBirth().getValue());

                try {
                    if (rs.length() != 0) {
                        // Documents

                        for (int y = 0; y < rs.length(); y++) {
                            NodeRef documentNodeRef = rs.getNodeRef(y);
                            // Get current document properties
                            Map<QName, Serializable> documentProperties = nodeService.getProperties(documentNodeRef);
                            String currentFirstName = (String) documentProperties.get(PunterSouthallCustomModel.PROP_FIRST_NAME);
                            String currentSurname = (String) documentProperties.get(PunterSouthallCustomModel.PROP_SURNAME);
                            String currentNINo = (String) documentProperties.get(PunterSouthallCustomModel.PROP_NINO);
                            Date currentMemberDOB = (Date) documentProperties.get(PunterSouthallCustomModel.PROP_DOB);
                            GregorianCalendar currentMemberDOBGregorian = new GregorianCalendar();
                            currentMemberDOBGregorian.setTime(currentMemberDOB);
                            // If any of the current properties doesn't equal the new properties, then update the document
                            if (currentFirstName != firstName || currentSurname != surname || currentNINo != nino || currentMemberDOBGregorian != memberDOB) {
                                documentProperties.put(PunterSouthallCustomModel.PROP_FIRST_NAME, firstName);
                                documentProperties.put(PunterSouthallCustomModel.PROP_SURNAME, surname);
                                documentProperties.put(PunterSouthallCustomModel.PROP_NINO, nino);
                                documentProperties.put(PunterSouthallCustomModel.PROP_DOB, memberDOB);

                                // Might be something wrong with locking check

                                if (checkOutCheckInService.isCheckedOut(documentNodeRef)) {
                                    checkOutCheckInService.cancelCheckout(documentNodeRef);
                                    nodeService.setProperties(documentNodeRef, documentProperties);




                                }
                                else if (lockService.getLockStatus(documentNodeRef).equals(LockStatus.LOCKED)) {
                                    lockService.unlock(documentNodeRef);
                                    //nodeService.setProperties(documentNodeRef, documentProperties);
                                    nodeService.setProperty(documentNodeRef, PunterSouthallCustomModel.PROP_FIRST_NAME, firstName);
                                    nodeService.setProperty(documentNodeRef, PunterSouthallCustomModel.PROP_SURNAME, surname);
                                    nodeService.setProperty(documentNodeRef, PunterSouthallCustomModel.PROP_NINO, nino);
                                    nodeService.setProperty(documentNodeRef, PunterSouthallCustomModel.PROP_DOB, memberDOB);
                                    // Re-lock the document
                                    lockService.lock(documentNodeRef, LockType.READ_ONLY_LOCK);
                                } else {
                                    nodeService.setProperties(documentNodeRef, documentProperties);
                                }

                            }
                        }
                    }
                    // All documents have  been updated
                    // Now check for running workflows in APS


                    String numberOfUpdatedWF = updateAPSWorkflows( centralMemberRecordID.toString(), firstName, surname, nino, memberDOB);


                    if (rs.length() != 0)
                        logger.info("Document details updated for central member ID : " + centralMemberRecordID.toString());
                    else
                        logger.info("No documents to update for central member ID : " + centralMemberRecordID.toString());

                    Boolean updatedOk = iPSService.updateCentralMemberRecordsChangeRecord(changesID, endOfDayUpdate);

                    if (!updatedOk)
                        logger.error("ChangesToDocumentRecord update failed for central member ID : " + centralMemberRecordID.toString());


                } catch (Exception ex) {
                    logger.error("Document details update failed for central member ID : " + centralMemberRecordID.toString());
                    logger.error(ex.getMessage());
                } finally {
                    rs.close();
                }
            }
            logger.debug("PSGDocumentUpdaterJob has completed.");

        } catch (LockAcquisitionException e) {
            // Job being done by another process
            if (logger.isDebugEnabled()) {
                logger.error("Document Import already underway.");
                logger.error(e.getMessage());
            }
        } catch (VmShutdownException e) {
            // Aborted
            if (logger.isDebugEnabled()) {
                logger.error("Document Import aborted.");
                logger.error(e.getMessage());
            }
        } finally {
            releaseLock(lockCallback, lockToken);
            logger.debug("Document Import Job Lock released");
        }
    }
	
	//************************************************************************************************
    private GregorianCalendar getGregorianCalendarForXMLGregorianCalendar(XMLGregorianCalendar xmlGregorianCalendar){
        return new GregorianCalendar(
                xmlGregorianCalendar.getYear(),
                xmlGregorianCalendar.getMonth() - 1,
                xmlGregorianCalendar.getDay());
    }

    /**
     * Creates a request body as a JSON object
     * then calls doJsonPost to send the request
     * @param cmdbId
     * @param firstName
     * @param surname
     * @param nino
     * @param memberDob
     * @return
     * @throws JSONException
     *
     * Example post
     * POST http://localhost:8080/activiti-app/api/enterprise/psg/member
     * {
     * "memberCentralID":"123",
     * "memberForename":"Darren1",
     * "memberSurname":"Devine1",
     * "memberNINumber":"JK99",
     * "memberDOB":"1970-01-01T00:00:00-00:00"
     * }
     */
    private String updateAPSWorkflows(String cmdbId, String firstName, String surname, String nino, GregorianCalendar memberDob) throws JSONException {

            JSONObject body = new JSONObject();
            body.put("memberCentralID",cmdbId );
            body.put("memberForename", firstName);
            body.put("memberSurname", surname);
            body.put("memberNINumber", nino);
            body.put("memberDOB", memberDob);

            JSONObject result = doJsonPost("api/enterprise/psg/member", body);
            return result.toString();
    }

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

}
