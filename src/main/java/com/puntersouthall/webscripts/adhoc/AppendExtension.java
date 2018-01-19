package com.puntersouthall.webscripts.adhoc;

import com.puntersouthall.helper.NodeServiceHelper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.UnableToReleaseLockException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.*;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import javax.transaction.*;
import java.io.IOException;

public class AppendExtension extends AbstractWebScript{

	private SearchService searchService;
	private NodeService nodeService;
	private LockService lockService;
	private MimetypeService mimeTypeService;
	private FileFolderService fileFolderService;
	private ServiceRegistry serviceRegistry;
	
	protected int MAXPERMISSIONCHECKS;
	protected int MAXPERMISSIONCHECKTIMEMILLIS;
	protected boolean ADDEXTENSIONS;
	protected boolean SEARCHONLY;
	protected String MIMETYPE;
	protected WebScriptResponse GLOBALRESPONCE;

	 /**
     * Inject the Alfresco service registry
     * 
     * @param serviceRegistry Alfresco service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.serviceRegistry = serviceRegistry;
        this.searchService = serviceRegistry.getSearchService();
        this.nodeService = serviceRegistry.getNodeService();
        this.lockService = serviceRegistry.getLockService();
        this.mimeTypeService = serviceRegistry.getMimetypeService();
        this.fileFolderService = serviceRegistry.getFileFolderService();
        
    }
	
	@SuppressWarnings("deprecation")
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		// TODO Auto-generated method stub
		
		final Logger logger = Logger.getLogger(AppendExtension.class);

		GLOBALRESPONCE = res;
		//set defaults and get parameters 
		MAXPERMISSIONCHECKS = 10000;
		MAXPERMISSIONCHECKTIMEMILLIS = 1;
		//ADDEXTENSIONS = false;
		SEARCHONLY = true;
		MIMETYPE = "";
		getParams(req);
		AuthenticationUtil.runAsSystem(new RunAsWork<String>() { 
			public String doWork() {
		NodeRef companyHome = NodeServiceHelper.getCompanyHome(searchService);
		NodeRef workingfolder = NodeServiceHelper.createChildNode(nodeService, companyHome, "Temporary Workings", ContentModel.TYPE_FOLDER);
		NodeRef AppendExtensionWorkingFolder = NodeServiceHelper.createChildNode(nodeService, workingfolder, "Append Extension Webscript", ContentModel.TYPE_FOLDER);
		
		long startTime = System.nanoTime();
		ResultSet rs = null;
		String fileExtension = mimeTypeService.getExtension(MIMETYPE);
		
		//bin is returned if no mimetype map is found
		if (fileExtension!= "bin"){
			try{
				SearchParameters sp = new SearchParameters();
				sp.setLanguage(SearchService.LANGUAGE_LUCENE);
				sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
				sp.setMaxItems(-1);
				sp.setLimit(0);
				sp.setMaxPermissionChecks(MAXPERMISSIONCHECKS);
				sp.setMaxPermissionCheckTimeMillis(MAXPERMISSIONCHECKTIMEMILLIS);
				sp.setLimitBy(LimitBy.UNLIMITED);
				if(fileExtension.equals("tiff")){
					sp.setQuery("(TYPE:\"psdoc:document\" OR TYPE:\"psdoc:tasklist\" OR TYPE:\"psdoc:adminSchemeControlDocument\" ) AND NOT (@cm\\:name:\"*.tif\" OR @cm\\:name:\"*.tiff\") AND @cm\\:content.mimetype:\"image/tiff\"");
				}
				else{
					sp.setQuery("(TYPE:\"psdoc:document\" OR TYPE:\"psdoc:tasklist\" OR TYPE:\"psdoc:adminSchemeControlDocument\" ) AND NOT @cm\\:name:\"*." + fileExtension + "\" AND @cm\\:content.mimetype:\"" + MIMETYPE + "\"");
				}
				if(logger.isDebugEnabled()){
					logger.debug("Search query being used: " + sp.getQuery());
				}
				
				

				rs = searchService.query(sp);
				if(!SEARCHONLY){
					for(ResultSetRow row : rs)
		            {
						if(logger.isDebugEnabled()){
							logger.debug("Record number : " + row.getIndex() + " starting");
						}
						UserTransaction trx = serviceRegistry.getTransactionService().getNonPropagatingUserTransaction(false);
						try{
							trx.begin();
							NodeRef currentNodeRef = row.getNodeRef();
							String filename = (String) nodeService.getProperty(currentNodeRef, ContentModel.PROP_NAME);
			    			if (lockService.getLockStatus(currentNodeRef).equals(LockStatus.LOCKED)) {
			    				try{
			    					GLOBALRESPONCE.getWriter().write(fileFolderService.getFileInfo(currentNodeRef).toString() + " - Locked file" + "\n");
			    					lockService.unlock(row.getNodeRef());
			    					// added try catch in case when file is renamed there is already a file with that name, we move the file to trigger our rename behavour on file move.
			    					try{
			    						nodeService.setProperty(currentNodeRef, ContentModel.PROP_NAME, filename + "." + fileExtension);
			    					}
			    					catch(DuplicateChildNodeNameException ex){
			    						try {
			    							logger.debug("Duplicate name caught, moving file to temporary folder..." + currentNodeRef.toString());
			    							NodeRef currentNodeParentFolder = nodeService.getPrimaryParent(currentNodeRef).getParentRef();
											fileFolderService.move(currentNodeRef, AppendExtensionWorkingFolder, null);
											nodeService.setProperty(currentNodeRef, ContentModel.PROP_NAME, filename + "." + fileExtension);
											fileFolderService.move(currentNodeRef, currentNodeParentFolder, null);
											
										} catch (FileExistsException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (FileNotFoundException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
			    					}
			    					finally{
			    						lockService.lock(currentNodeRef, LockType.READ_ONLY_LOCK);
			    					}

			    				}
			    				catch(UnableToReleaseLockException ex){
			    					logger.error("Unable to unlock node ref : " + currentNodeRef.toString() + " Full exception : " + ex);
			    				};

			    			}
			    			else{
			    				GLOBALRESPONCE.getWriter().write(fileFolderService.getFileInfo(currentNodeRef).toString() + " - unlocked file" + "\n");
			    				try{
			    					nodeService.setProperty(currentNodeRef, ContentModel.PROP_NAME, filename + "." + fileExtension);
			    				}
			    				catch(DuplicateChildNodeNameException ex){
			    					NodeRef currentNodeParentFolder = nodeService.getPrimaryParent(currentNodeRef).getParentRef();
									try {
										fileFolderService.move(currentNodeRef, AppendExtensionWorkingFolder, null);
										nodeService.setProperty(currentNodeRef, ContentModel.PROP_NAME, filename + "." + fileExtension);
										fileFolderService.move(currentNodeRef, currentNodeParentFolder, null);
									} catch (FileExistsException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (FileNotFoundException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
		
			    				}
			    				
			    			}
	    
			    			trx.commit();
							if(logger.isDebugEnabled()){
								logger.debug("Record number : " + row.getIndex() + " committed");
							}
			            }
						catch (RollbackException exception) {
							// TODO Auto-generated catch block
							exception.printStackTrace();
							rollbacktrans(trx);
						} catch (HeuristicMixedException exception) {
							// TODO Auto-generated catch block
							exception.printStackTrace();
							rollbacktrans(trx);
						} catch (HeuristicRollbackException exception) {
							// TODO Auto-generated catch block
							exception.printStackTrace();
							rollbacktrans(trx);
						}
						catch (SecurityException exception) {
							// TODO Auto-generated catch block
							exception.printStackTrace();
							rollbacktrans(trx);
						} catch (IllegalStateException exception) {
							// TODO Auto-generated catch block
							exception.printStackTrace();
							rollbacktrans(trx);
						} catch (SystemException exception) {
							// TODO Auto-generated catch block
							exception.printStackTrace();
							rollbacktrans(trx);
						} catch (NotSupportedException exception) {
							// TODO Auto-generated catch block
							exception.printStackTrace();
							rollbacktrans(trx);
						}
						
		            }
				}
				else{
					for(ResultSetRow row : rs){
						NodeRef currentNodeRef = row.getNodeRef();
						GLOBALRESPONCE.getWriter().write("Node found - " + fileFolderService.getFileInfo(currentNodeRef).toString());
						if(logger.isDebugEnabled()){
							logger.debug("Node found - " + fileFolderService.getFileInfo(currentNodeRef).toString());
						}
					}
				}

				long endTime = System.nanoTime();
				long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
				
				GLOBALRESPONCE.getWriter().write("Number or results found : "  + rs.length()+ "\n" + 
						"Parameters used : \n" + 
						"MIMETYPE = " + MIMETYPE+ "\n" + 
						"File extension mapped from MIMETYPE provided = " + fileExtension + "\n" + 
						"MAXPERMISSIONCHECKS = " + MAXPERMISSIONCHECKS + "\n" + 
						"MAXPERMISSIONCHECKTIMEMILLIS = " + MAXPERMISSIONCHECKTIMEMILLIS + "\n" + 
						"SEARCHONLY (properties not updated) = " + SEARCHONLY + "\n" 	+ "\n" 	+ "\n" 	+
						"Time taken : " + duration/1000000000 +" seconds"
						
						);
				
				
			}
			catch(IOException ioex){
				
			}
			finally{
				rs.close();
				
			}
			
		}
		else{
			try {
				GLOBALRESPONCE.getWriter().write("No Mimetype map was found for : " + MIMETYPE);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "ok";
		

			}});
		
		
	}
	
	private boolean rollbacktrans(UserTransaction t ){
		try {
			t.rollback();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
		
	}
	
	public void getParams(WebScriptRequest req){
		String param;
		param = req.getParameter("MaxPermissionChecks");
		if(param != null && !param.isEmpty()){
			MAXPERMISSIONCHECKS = Integer.parseInt(req.getParameter("MaxPermissionChecks"));
		}
/*		param = req.getParameter("MaxPermissionCheckTimeMillis");
		if(param != null && !param.isEmpty()){
			MAXPERMISSIONCHECKTIMEMILLIS = Integer.parseInt(req.getParameter("MaxPermissionCheckTimeMillis"));
		}*/
		if(req.getParameter("SearchOnly").equals("0")){
			SEARCHONLY = false;
		}
		param = req.getParameter("MimeType");
		if(param != null && !param.isEmpty()){
			MIMETYPE = param;
		}
		
	}
	

}
