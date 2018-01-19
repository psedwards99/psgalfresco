package com.puntersouthall.policies;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies.OnContentPropertyUpdatePolicy;
import org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.transform.PoiContentTransformer;
import org.alfresco.repo.domain.locks.LockDetails.LockType;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import antlr.Utils;

import com.google.gdata.data.Content;
import com.puntersouthall.helper.NodeServiceHelper;
import com.puntersouthall.helper.PSErrorLoggingHelper;
import com.puntersouthall.model.PunterSouthallCustomModel;
import com.puntersouthall.model.PunterSouthallFormsModel;
import com.puntersouthall.webservice.alfresco.ArrayOfMemberType;
import com.puntersouthall.webservice.alfresco.ArrayOfSchemeType;
import com.puntersouthall.webservice.alfresco.IPunterSouthallService;
import com.puntersouthall.webservice.alfresco.MemberType;
import com.puntersouthall.webservice.alfresco.PunterSouthallService;
import com.puntersouthall.webservice.alfresco.SchemeType;
import com.sun.tools.xjc.model.CReferencePropertyInfo;

/**
 * @author Nicholas Cottingham & Joe Bujok
 *
 */
public class UpdatedPayrollNewStarterFormPropertiesBehaviour implements NodeServicePolicies.OnUpdatePropertiesPolicy,  OnContentUpdatePolicy
{

	/** the logger */
	private final Log logger = LogFactory.getLog(UpdatedPayrollNewStarterFormPropertiesBehaviour.class);
	
	/** output location **/
	private String outputLocation;
	
	public void setOutputLocation(String outputLocation) {
		this.outputLocation = outputLocation;
	}
	
	/** Alfresco services **/
	private NodeService nodeService;
	private ServiceRegistry serviceRegistry;
	//private FileFolderService fileFolderService;
	//private SearchService searchService;
	//private AuthenticationService authenticationService;
	private ContentService contentService;
	private PermissionService permissionService;
	private LockService lockService;
	
	/**
	* policy component to create behaviours
	*/
	private PolicyComponent policyComponent;
	
	/**
	* updateProperties behaviour
	*/
	private Behaviour updateProperties;
	/**
	* updateProperties behaviour
	*/
	private Behaviour onContentUpdate;	
	

	
	/**
	* setter method for policy component
	* 
	* @param policyComponent
	*/
	public void setPolicyComponent(PolicyComponent policyComponent)
	{
	this.policyComponent = policyComponent;
	}
	
	/**
	* setter method for service registry
	* 
	* @param serviceRegistry
	*/
	public void setServiceRegistry(ServiceRegistry serviceRegistry)
	{
	// setting the services
		this.serviceRegistry = serviceRegistry;
		this.nodeService = serviceRegistry.getNodeService();
		//this.fileFolderService = serviceRegistry.getFileFolderService();
		//this.searchService = serviceRegistry.getSearchService();
		//this.authenticationService = serviceRegistry.getAuthenticationService();
		this.contentService = serviceRegistry.getContentService();
		this.permissionService = serviceRegistry.getPermissionService();
		this.lockService = serviceRegistry.getLockService();
	}
	
	
	/**
	* method to initialize the behaviours
	*/
	public void init()
	{
	
	// Initialize behaviours
	this.updateProperties = new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT);
	this.onContentUpdate = new JavaBehaviour(this, "onContentUpdate", NotificationFrequency.EVERY_EVENT);
	
	// Bind behaviours to node policies	
	this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), 
	        PunterSouthallFormsModel.TYPE_NEW_PAYROLL_STARTER_FORM, this.updateProperties);
	this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onContentUpdate"), 
	        PunterSouthallFormsModel.TYPE_NEW_PAYROLL_STARTER_FORM, this.onContentUpdate);
	
	}
	
	
	
	@Override
	public void onUpdateProperties(NodeRef actionedUponNodeRef, Map<QName, Serializable> oldProps, Map<QName, Serializable> newProps)
	{

		String newFormStatus = (String) newProps.get(PunterSouthallFormsModel.PROP_FORM_STATUS);
		String oldFormStatus = (String) oldProps.get(PunterSouthallFormsModel.PROP_FORM_STATUS);
		if(newFormStatus == null) newFormStatus = "";
		if(oldFormStatus == null) oldFormStatus = "";
		
		
		Date dateCreated = (Date) newProps.get(ContentModel.PROP_CREATED);
		Date currentTime = new Date();
		long minutesDiff = (currentTime.getTime() - dateCreated.getTime()) / 1000 / 60;

		//should be called when a payroll form is first created.
		if(oldProps.isEmpty() && (minutesDiff < 2)){
			HashMap<QName, Serializable> formCreatorProps =  new  HashMap<QName, Serializable>();
			formCreatorProps.put(PunterSouthallFormsModel.PROP_DOER, newProps.get(ContentModel.PROP_CREATOR));
			formCreatorProps.put(PunterSouthallFormsModel.PROP_DOER_TIMESTAMP,ISO8601DateFormat.format(currentTime));
			formCreatorProps.put(PunterSouthallFormsModel.PROP_FORM_STATUS, "Draft");
			nodeService.addProperties(actionedUponNodeRef, formCreatorProps);
		}
		
		
		//only when status has changed to Approved by admin...
		if (newFormStatus.equals("Approved by Admin") && !(newFormStatus.equals(oldFormStatus))){
			//change file permission
	        try{
	        	 HashMap<QName, Serializable> checkerProps = new  HashMap<QName, Serializable>();
	        	 
	        	 Date currentDate = new Date();
	        	 String ISOTimeStamp = ISO8601DateFormat.format(currentDate);
	        	 String doerUserName = (String) oldProps.get(PunterSouthallFormsModel.PROP_DOER);
	        	 String checkerUserName = this.serviceRegistry.getAuthenticationService().getCurrentUserName();
	        	 if(!doerUserName.equals(checkerUserName)){
		        	 checkerProps.put(PunterSouthallFormsModel.PROP_CHECKER_STAGE_1,checkerUserName);
		        	 checkerProps.put(PunterSouthallFormsModel.PROP_CHECKER_STAGE_1_TIMESTAMP, ISOTimeStamp);
		        	 nodeService.addAspect(actionedUponNodeRef, PunterSouthallFormsModel.ASPECT_CHECK_STAGE_1, checkerProps);
	        		 
	        	 }
	        	 else{
	        		 //reset the form status
	        		 nodeService.setProperty(actionedUponNodeRef, PunterSouthallFormsModel.PROP_FORM_STATUS, oldProps.get(PunterSouthallFormsModel.PROP_FORM_STATUS));
	        		 throw new ContentIOException("Checker and Doer are the same person, this is not allowed.");
	        		 
	        		 
	        	 }
	        	 removeWriteAdminPermission(actionedUponNodeRef);

	        }
	        catch(Exception ex) {
	        	logger.error("An error has occured : " + ex.getMessage());

	        }
		}
	

		if (newFormStatus.equals("Approved by Payroll") && !(newFormStatus.equals(oldFormStatus)) ){

			//export file and change permissions
	        try{
	        	
	        	HashMap<QName, Serializable> checkerStageTwoProps = new  HashMap<QName, Serializable>();
	        	 
	        	 Date currentDate = new Date();
	        	 String ISOTimeStamp = ISO8601DateFormat.format(currentDate);
	        	 String doerUserName = (String) oldProps.get(PunterSouthallFormsModel.PROP_DOER);
	        	 String checkerUserName = (String) oldProps.get(PunterSouthallFormsModel.PROP_CHECKER_STAGE_1);
	        	 String checkerStageTwoUserName = this.serviceRegistry.getAuthenticationService().getCurrentUserName();
	        	 if(!doerUserName.equals(checkerUserName) && !doerUserName.equals(checkerStageTwoUserName) && !checkerUserName.equals(checkerStageTwoUserName)){
	        		 checkerStageTwoProps.put(PunterSouthallFormsModel.PROP_CHECKER_STAGE_2,checkerStageTwoUserName);
	        		 checkerStageTwoProps.put(PunterSouthallFormsModel.PROP_CHECKER_STAGE_2_TIMESTAMP, ISOTimeStamp);
		        	 nodeService.addAspect(actionedUponNodeRef, PunterSouthallFormsModel.ASPECT_CHECK_STAGE_2, checkerStageTwoProps);
		    		//start export

	        		String fileTimeStamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
	    		
	    			Integer memberID = (Integer) nodeService.getProperty(actionedUponNodeRef, PunterSouthallCustomModel.PROP_MEMBER_RECORD_ID);
					String outputFolderPath = outputLocation;
					File outputFile = new File(outputFolderPath);
				    outputFile.mkdirs();
		    	    String outputFilePath = outputFolderPath + "Payroll new starter form - " + memberID + fileTimeStamp + ".xlsm";
		    	    createFile(contentService, actionedUponNodeRef, outputFilePath);
		    	    
		    	    lockNewPensionerFrom(actionedUponNodeRef);
	        	 }
	        	 else{
	        		//reset the form status
	        		 nodeService.setProperty(actionedUponNodeRef, PunterSouthallFormsModel.PROP_FORM_STATUS, oldProps.get(PunterSouthallFormsModel.PROP_FORM_STATUS));
	        		 throw new ContentIOException("Doer, checker or Payroll checker are the same person, this is not allowed."); 
	        		 
	        	 }
 	
	        	
	        } catch (InvalidStoreRefException e) {
	            //PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(),"Invalid store ref exception");
	        }
		}


	}
	
	
	
	private Cell getSingleCellFromNamedRange(String SingleCellName, Workbook wb){
	
		Name namedCell = wb.getName(SingleCellName);	 
		AreaReference aref = new AreaReference(namedCell.getRefersToFormula());
		CellReference cref;
		 if(aref.isSingleCell()){
			 cref = aref.getFirstCell();
			 Sheet s = wb.getSheet(cref.getSheetName());
        	 Row r = s.getRow(cref.getRow());
        	 Cell c = r.getCell(cref.getCol());
        	 return c;

		 }
		 else return null;
	}
	
	private Object getCellValue(Cell cell){
		
		switch (cell.getCellType()) {
	        case Cell.CELL_TYPE_STRING:
	            return cell.getStringCellValue();
	
	        case Cell.CELL_TYPE_BOOLEAN:
	           return cell.getBooleanCellValue();
	
	        case Cell.CELL_TYPE_NUMERIC:
	            return cell.getNumericCellValue();
	            
	        case Cell.CELL_TYPE_BLANK:
	            return "";
	            
	        case Cell.CELL_TYPE_FORMULA:
	            return cell.getCellFormula();
	            
	        default:
	        	return null;
		}
	}
		 
		
   
	private void removeWriteAdminPermission(NodeRef nodeRef)
    {
		// set admin group name
		String payrollGroupName = "GROUP_ALF-PayrollTeam";
        // set the user as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        // check whether the group has the permissions or not
        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);
        for (AccessPermission permission : permissions)
        {
            if (permission.getAuthority().contains("Admin-FullAccess")) 
            {           
                
            	 permissionService.setInheritParentPermissions(nodeRef, false);
            	 permissionService.setPermission(nodeRef, permission.getAuthority(), PermissionService.CONTRIBUTOR, true);
            	        	
                 permissionService.setPermission(nodeRef, payrollGroupName, PermissionService.WRITE, true);
                 permissionService.setPermission(nodeRef, payrollGroupName, PermissionService.CANCEL_CHECK_OUT, true);
                 permissionService.setPermission(nodeRef, payrollGroupName, PermissionService.CHECK_IN, true);
                 permissionService.setPermission(nodeRef, payrollGroupName, PermissionService.CHECK_OUT, true);
            }
            if (permission.getAuthority().contains("Admin-ReadOnly")) 
            {
            	permissionService.setPermission(nodeRef, permission.getAuthority(), PermissionService.CONTRIBUTOR, true);
            }
        }

    }  
	
	private void lockNewPensionerFrom(NodeRef nodeRef)
    {
		lockService.lock(nodeRef, org.alfresco.service.cmr.lock.LockType.READ_ONLY_LOCK);

    } 
        

	
	private void createFile(ContentService contentService, NodeRef documentNodeRef, final String outputFilePath) {
		try {
			ContentReader reader = contentService.getReader(documentNodeRef, ContentModel.PROP_CONTENT);
		    InputStream originalInputStream = reader.getContentInputStream();
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    final int BUF_SIZE = 1 << 8; //1KiB buffer
		    byte[] buffer = new byte[BUF_SIZE];
		    int bytesRead = -1;
		    while((bytesRead = originalInputStream.read(buffer)) > -1) {
		    	outputStream.write(buffer, 0, bytesRead);
		    }
		    originalInputStream.close();
		    final byte[] binaryData = outputStream.toByteArray();	
		    
		    AuthenticationUtil.runAsSystem(new RunAsWork<String>() { 
				public String doWork() {
					try{
					    FileOutputStream out = new FileOutputStream(outputFilePath);
					    out.write(binaryData);		    
					    out.close();
					}
					catch (Exception ex){
						logger.error("Document not exported");
						logger.error(ex.getMessage());
					}
					finally {
					}	
					return "ok";
				}});
		    logger.info("File exported : " + outputFilePath);
		}
		catch (Exception ex){
			logger.error("Document not exported");
			logger.error(ex.getMessage());
		}
		finally {
		}	
	}

	@Override
	public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
		
		//update form metadata if content has changed
		logger.debug("Attempting to process excel file conent on property change.");
		//try to get a content reader for stream#
		try {
			ContentReader contentReader =  serviceRegistry.getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT);
			InputStream inputStream = contentReader.getContentInputStream();
			Workbook wb;
			//try opening excel file
			try {
				wb = new XSSFWorkbook(inputStream);
				Double payYear = (Double) (getCellValue(getSingleCellFromNamedRange("PayYear", wb)));
				String payPeriod = (String) (getCellValue(getSingleCellFromNamedRange("PayPeriod", wb)));

				HashMap<QName, Serializable> periodProps = new  HashMap<QName, Serializable>();
				periodProps.put(PunterSouthallCustomModel.PROP_PERIOD_YEAR, payYear);
				periodProps.put(PunterSouthallCustomModel.PROP_PERIOD_MONTH, payPeriod);
				nodeService.addAspect(nodeRef, PunterSouthallCustomModel.ASPECT_PERIOD, periodProps);
				logger.debug("Sucessfully updated metadata to match excel form content.");
				
			} catch (IOException e1) {
				
				logger.error("Unable to process the content, perhaps not an excel file. " + e1.getMessage());
			}
		} catch (Exception e) {
			// this can happen sometimes on transaction commit frequency. 
			logger.debug("could not get a content reader");
		}
		
	}


}
