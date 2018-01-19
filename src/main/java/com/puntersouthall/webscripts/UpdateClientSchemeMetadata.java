package com.puntersouthall.webscripts;

import com.puntersouthall.model.PunterSouthallCustomModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.*;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;

public class UpdateClientSchemeMetadata extends AbstractWebScript{

	static Logger  log = Logger.getLogger(UpdateClientSchemeMetadata.class);
	
	private SearchService searchService;
	private NodeService nodeService;
	
	 
	protected String UPDATE_TYPE;
	protected String NEW_NAME;
	protected int ID;
	protected int MAXPERMISSIONCHECKS;
	protected int MAXPERMISSIONCHECKTIMEMILLIS;
	protected boolean RUN_UPDATE;
	

	 /**
     * Inject the Alfresco service registry
     * 
     * @param serviceRegistry Alfresco service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.searchService = serviceRegistry.getSearchService();
        this.nodeService = serviceRegistry.getNodeService();

    }
	
	
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		// set system as user
		AuthenticationUtil.setRunAsUserSystem();

		//set defaults and get parameters 
		UPDATE_TYPE ="";
		ID = -1;
		NEW_NAME = "";
		MAXPERMISSIONCHECKS = 1000;
		MAXPERMISSIONCHECKTIMEMILLIS = 10;
		RUN_UPDATE = false;
		String ErrorMessage = "None";
		getParams(req);
		
		
		//ASPECT:"psdoc:clientAspect" AND @psdoc\:clientID:1001 AND NOT @psdoc\:clientName:"3i Group plcs"

		long startTime = System.nanoTime();
		ResultSet rs = null;
		try{
			SearchParameters sp = new SearchParameters();
			sp.setLanguage(SearchService.LANGUAGE_LUCENE);
			sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
			sp.setMaxItems(-1);
			sp.setLimit(0);
			sp.setMaxPermissionChecks(MAXPERMISSIONCHECKS);
			sp.setMaxPermissionCheckTimeMillis(MAXPERMISSIONCHECKTIMEMILLIS);
			sp.setLimitBy(LimitBy.UNLIMITED);

			int NumberOfUpdates = 0;
			
			if(UPDATE_TYPE.equals("Client")){
				String queryString = "ASPECT:\"psdoc:clientAspect\" AND @psdoc\\:clientID:"
						+ ID + " AND NOT @psdoc\\:clientName:\"" + NEW_NAME + "\"" ;
				sp.setQuery(queryString);
				rs = searchService.query(sp);
				log.debug("Query string being executed : " + queryString);	
				
				for(ResultSetRow row : rs)
	            {
					NodeRef nr = row.getNodeRef();
					String CurrentName = (String) nodeService.getProperty(nr, PunterSouthallCustomModel.PROP_CLIENT_NAME);
					if (!CurrentName.equals(NEW_NAME)){
						if(RUN_UPDATE){
							nodeService.setProperty(nr, PunterSouthallCustomModel.PROP_CLIENT_NAME, NEW_NAME);
						}
						NumberOfUpdates++;
					};
	            }
				if(RUN_UPDATE){
					SearchParameters folderSP = new SearchParameters();
					folderSP.setLanguage(SearchService.LANGUAGE_LUCENE);
					folderSP.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
					folderSP.setMaxItems(-1);
					folderSP.setLimit(0);
					folderSP.setMaxPermissionChecks(1000);
					folderSP.setMaxPermissionCheckTimeMillis(10);
					folderSP.setLimitBy(LimitBy.NUMBER_OF_PERMISSION_EVALUATIONS);
					folderSP.setQuery("PATH:\"/app:company_home/cm:Clients/*\" AND TYPE:\"cm:folder\" AND @cm\\:name:" + ID);
					rs = searchService.query(folderSP);
					if(rs.length()==1){
						nodeService.setProperty(rs.getNodeRef(0), ContentModel.PROP_NAME, ID + " - " + NEW_NAME); 
					}
					else{
						ErrorMessage =  "Could not update Clients Folder name";
					}
					
				}
				
				
			}
			
			if(UPDATE_TYPE.equals("Scheme")){
				String queryString = "ASPECT:\"psdoc:schemeAspect\" AND @psdoc\\:schemeID:"
						+ ID + " AND NOT @psdoc\\:schemeName:\"" + NEW_NAME + "\"" ;
				sp.setQuery(queryString);
				rs = searchService.query(sp);
				log.debug("Query string being executed : " + queryString);	
				
				for(ResultSetRow row : rs)
	            {
					NodeRef nr = row.getNodeRef();
					String CurrentName = (String) nodeService.getProperty(nr, PunterSouthallCustomModel.PROP_SCHEME_NAME);
					if (!CurrentName.equals(NEW_NAME)){
						if(RUN_UPDATE){
							nodeService.setProperty(nr, PunterSouthallCustomModel.PROP_SCHEME_NAME, NEW_NAME);
						}
						NumberOfUpdates++;
					};
	            }
				ErrorMessage =  "Scheme folder names need to be updated manually...";
			}
			

			
			
			long endTime = System.nanoTime();
			long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
			
			res.getWriter().write("Number or results found : "  + rs.length()+ "\n" + 
					"Parameters used : \n" + 
					"MAXPERMISSIONCHECKS = " + MAXPERMISSIONCHECKS + "\n" + 
					"MAXPERMISSIONCHECKTIMEMILLIS = " + MAXPERMISSIONCHECKTIMEMILLIS + "\n" + 
					"UPDATE_TYPE = " + UPDATE_TYPE + "\n" 	+ "\n" 	+ "\n" 	+
					"ID = " + ID + "\n" 	+ 
					"NEW_NAME = " + NEW_NAME + "\n" 	+
					"Number of files updated = " + NumberOfUpdates + "\n" 	+
					"SEARCHONLY (properties not updated) = " + !RUN_UPDATE + "\n" 	+
					"Time taken : " + duration/1000000000 +" seconds" + "\n" 	+ "\n" 	+
					"Errors : " + ErrorMessage
					);
			
			
		}
		finally{
			rs.close();
			
		}
		
	}
	
	protected void getParams(WebScriptRequest req){
		String param;
		UPDATE_TYPE = req.getParameter("UpdateType");
		NEW_NAME = req.getParameter("NewName");
		ID =  Integer.parseInt(req.getParameter("ID"));
		param = req.getParameter("MaxPermissionChecks");
		if(param != null && !param.isEmpty()){
			MAXPERMISSIONCHECKS = Integer.parseInt(req.getParameter("MaxPermissionChecks"));
		}
		param = req.getParameter("MaxPermissionCheckTimeMillis");
		if(param != null && !param.isEmpty()){
			MAXPERMISSIONCHECKTIMEMILLIS = Integer.parseInt(req.getParameter("MaxPermissionCheckTimeMillis"));
		}
		if(req.getParameter("RunUpdate").equals("1")){
			RUN_UPDATE = true;
		}
		
	}

}
