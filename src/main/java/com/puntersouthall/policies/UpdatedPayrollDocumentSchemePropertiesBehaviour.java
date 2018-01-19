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
public class UpdatedPayrollDocumentSchemePropertiesBehaviour implements NodeServicePolicies.OnUpdatePropertiesPolicy
{

	/** the logger */
	private final Log logger = LogFactory.getLog(UpdatedPayrollDocumentSchemePropertiesBehaviour.class);
		
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
	        QName.createQName("custom.model.jb", "payrollSchemeDocument"), this.updateProperties);
	
	}
	
	
	
	@Override
	public void onUpdateProperties(NodeRef actionedUponNodeRef, Map<QName, Serializable> oldProps, Map<QName, Serializable> newProps)
	{
	
		// get logged in user
    	String loggedInUser = authenticationService.getCurrentUserName();
		
		// QName for psdoc:centralID
	    QName psSchemeIDQName = QName.createQName("custom.model.jb", "schemeID");    
		Integer newSchemeID = (Integer) newProps.get(psSchemeIDQName);
		Integer oldSchemeID = (Integer) oldProps.get(psSchemeIDQName);	
		
		// QName for psdoc:subActivity
	    QName psSubActivityQName = QName.createQName("custom.model.jb", "subActivity");    
	    String newSubActivity = (String) newProps.get(psSubActivityQName);
	    String oldSubActivity = (String) oldProps.get(psSubActivityQName);	
		
		//set path for 'Move Document' folder
		String pathToUse = "";
		pathToUse = MOVE_DOCUMENT_FOLDER_PATH;
		
		
		if (oldSchemeID != null && newSchemeID != null){
			////if scheme has changed re-file document
			if (oldSchemeID.equals("") || oldSchemeID.equals(newSchemeID)){
				//if scheme has not changed, check if activity has changed and re-file document if so
				if (oldSubActivity != null && newSubActivity != null){
					if (oldSubActivity.equals("") || oldSubActivity.equals(newSubActivity)){
						//do nothing if both Scheme and activity have not changed
					}
					else {
						//doc needs to be re-filed as activity has changed
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
			else {
				//doc needs to be re-filed as Scheme ID has changed
		        try{
		            List<String> pathElements = Arrays.asList(StringUtils.split(pathToUse, '/'));
		            
		            NodeRef targetFolderNoderef = null;
		            
		            FileInfo fileInfo = null;	                    
		            fileInfo = fileFolderService.resolveNamePath(NodeServiceHelper.getCompanyHome(searchService), pathElements);
		            // Get the target folder noderef
		            targetFolderNoderef = fileInfo.getNodeRef();
		            logger.debug("Target folder : "+ nodeService.getProperty(targetFolderNoderef, ContentModel.PROP_NAME));		            
		            
		            nodeService.moveNode(actionedUponNodeRef, targetFolderNoderef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) nodeService.getProperty(actionedUponNodeRef,ContentModel.PROP_NAME)));            
		            		           
		        } catch (InvalidStoreRefException e) {
		            //logger.error(e.getMessage());
		            PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(),"Invalid store ref exception");
		        } catch (FileNotFoundException e) {
		            //logger.error(e.getMessage());
		            PSErrorLoggingHelper.psErrorLogger(logger, loggedInUser, fileFolderService.getFileInfo(actionedUponNodeRef).toString(),"File not found exception");
		        }
			}
		}
	}
	
		
}
