package com.puntersouthall.policies;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.puntersouthall.helper.NodeServiceHelper;
import com.puntersouthall.helper.PSErrorLoggingHelper;

/**
 * @author Nicholas Cottingham
 *
 */
public class UpdatedDocumentMemberPropertiesBehaviour implements NodeServicePolicies.OnUpdatePropertiesPolicy
{

	/** the logger */
	private final Log logger = LogFactory.getLog(UpdatedDocumentMemberPropertiesBehaviour.class);
		
	/** Alfresco services */
	private NodeService nodeService;
	private FileFolderService fileFolderService;
	private SearchService searchService;
	private AuthenticationService authenticationService;
	    
	/**
	* policy component to create behaviours
	*/
	private PolicyComponent policyComponent;
	
	/**
	* updateProperties behaviour
	*/
	private Behaviour updateProperties;
	
	
	/** path properties */
	private String MOVE_DOCUMENT_FOLDER_PATH;
	
	/**
	* setter method for move documents folder
	* 
	* @param policyComponent
	*/
	public void setMOVE_DOCUMENT_FOLDER_PATH(String mOVE_DOCUMENT_FOLDER_PATH) {
		MOVE_DOCUMENT_FOLDER_PATH = mOVE_DOCUMENT_FOLDER_PATH;
	}
	
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
		this.nodeService = serviceRegistry.getNodeService();
		this.fileFolderService = serviceRegistry.getFileFolderService();
		this.searchService = serviceRegistry.getSearchService();
		this.authenticationService = serviceRegistry.getAuthenticationService();
	}
	
	
	/**
	* method to initialize the behaviours
	*/
	public void init()
	{
	
	// Initialize behaviours
	this.updateProperties = new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT);
	
	// Bind behaviours to node policies
	this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
	        QName.createQName("custom.model.jb", "memberDetailsAspect"), this.updateProperties);
	}
	
	
	
	@Override
	public void onUpdateProperties(NodeRef actionedUponNodeRef, Map<QName, Serializable> oldProps, Map<QName, Serializable> newProps)
	{
	
		// get logged in user
    	String loggedInUser = authenticationService.getCurrentUserName();
		
		// QName for psdoc:centralID
	    QName psMemberRecordIDQName = QName.createQName("custom.model.jb", "memberRecordID");    
		Integer newMemberRecordID = (Integer)newProps.get(psMemberRecordIDQName);
		Integer oldMemberRecordID = (Integer) oldProps.get(psMemberRecordIDQName);	
	
		//set path for 'Move Document' folder
		String pathToUse = "";
		pathToUse = MOVE_DOCUMENT_FOLDER_PATH;
		
		if (oldMemberRecordID != null && newMemberRecordID != null){
			if (oldMemberRecordID.equals("") || oldMemberRecordID.equals(newMemberRecordID)){
				//do nothing
			}
			else {
				//doc needs to be re-filed
		        try{
		            List<String> pathElements = Arrays.asList(StringUtils.split(pathToUse, '/'));
		            
		            NodeRef targetFolderNoderef = null;
		            
		            FileInfo fileInfo = null;	                    
		            fileInfo = fileFolderService.resolveNamePath(NodeServiceHelper.getCompanyHome(searchService), pathElements);
		            // Get the target folder noderef
		            targetFolderNoderef = fileInfo.getNodeRef();
		            if (logger.isDebugEnabled()){
		            logger.debug("Target folder : "+ nodeService.getProperty(targetFolderNoderef, ContentModel.PROP_NAME));
		            }
		            
		            nodeService.moveNode(actionedUponNodeRef, targetFolderNoderef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) nodeService.getProperty(actionedUponNodeRef,ContentModel.PROP_NAME)));
            
		            
		           
		        } catch (InvalidStoreRefException e) {
		            PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(),"Invalid store ref exception");
		        } catch (FileNotFoundException e) {
		            PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(),"File not found exception");
		        }
			}
		}
	}
	
		
}

