package com.puntersouthall.webscripts;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.*;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;

public class LockNodes extends AbstractWebScript{

	private SearchService searchService;
	private NodeService nodeService;
	private LockService lockService;
	
	protected int MAXPERMISSIONCHECKS;
	protected int MAXPERMISSIONCHECKTIMEMILLIS;
	protected boolean LOCKFILES;
	protected boolean SEARCHONLY;

	 /**
     * Inject the Alfresco service registry
     * 
     * @param serviceRegistry Alfresco service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.searchService = serviceRegistry.getSearchService();
        this.nodeService = serviceRegistry.getNodeService();
        this.lockService = serviceRegistry.getLockService();
    }
	
	@SuppressWarnings("deprecation")
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		// TODO Auto-generated method stub
		AuthenticationUtil.setRunAsUserSystem();

		//set defaults and get parameters 
		MAXPERMISSIONCHECKS = 10000;
		MAXPERMISSIONCHECKTIMEMILLIS = 180000;
		LOCKFILES = false;
		SEARCHONLY = true;
		getParams(req);
		
		
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

			if(LOCKFILES){
				sp.setQuery("PATH:\"/app:company_home/cm:Clients//*\" AND (TYPE:\"psdoc\\:adminMemberDocument\" OR TYPE:\"psdoc\\:adminSchemeDocument\") "
						+ "AND (@psdoc\\:status:\"Checked\" OR @psdoc\\:status:\"Authorised\" )"
						+ " AND NOT @cm\\:lockLifetime:\"PERSISTENT\" AND NOT @cm\\:lockType:\"READ_ONLY_LOCK\"");
				rs = searchService.query(sp);
				if(!SEARCHONLY){
					for(ResultSetRow row : rs)
		            {
		                NodeRef currentNodeRef = row.getNodeRef();
		                lockService.lock(currentNodeRef,LockType.READ_ONLY_LOCK);
		            }
					
				}

				
			}
			if(!LOCKFILES){
				sp.setQuery("PATH:\"/app:company_home/cm:Clients//*\" AND (TYPE:\"psdoc\\:adminMemberDocument\" OR TYPE:\"psdoc\\:adminSchemeDocument\") "
						+ "AND (@psdoc\\:status:\"Checked\" OR @psdoc\\:status:\"Authorised\" )"
						+ "AND @cm\\:lockLifetime:\"PERSISTENT\" AND @cm\\:lockType:\"READ_ONLY_LOCK\"");
				rs = searchService.query(sp);
				if(!SEARCHONLY){
					for(ResultSetRow row : rs)
		            {
		                NodeRef currentNodeRef = row.getNodeRef();
		                lockService.unlock(currentNodeRef);
		            }
				}
			}
			
			
			long endTime = System.nanoTime();
			long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
			
			res.getWriter().write("Number or results found : "  + rs.length()+ "\n" + 
					"Parameters used : \n" + 
					"MAXPERMISSIONCHECKS = " + MAXPERMISSIONCHECKS + "\n" + 
					"MAXPERMISSIONCHECKTIMEMILLIS = " + MAXPERMISSIONCHECKTIMEMILLIS + "\n" + 
					"LOCKFILES = " + LOCKFILES + "\n" 	+ "\n" 	+ "\n" 	+
					"SEARCHONLY (properties not updated) = " + SEARCHONLY + "\n" 	+ "\n" 	+ "\n" 	+
					"Time taken : " + duration/1000000000 +" seconds"
					
					);
			
			
		}
		finally{
			rs.close();
			
		}
		

    	
    	
	}
	
	public void getParams(WebScriptRequest req){
		String param;
		param = req.getParameter("MaxPermissionChecks");
		if(param != null && !param.isEmpty()){
			MAXPERMISSIONCHECKS = Integer.parseInt(req.getParameter("MaxPermissionChecks"));
		}
		param = req.getParameter("MaxPermissionCheckTimeMillis");
		if(param != null && !param.isEmpty()){
			MAXPERMISSIONCHECKTIMEMILLIS = Integer.parseInt(req.getParameter("MaxPermissionCheckTimeMillis"));
		}
		
		if(req.getParameter("LockFiles").equals("1")){
			LOCKFILES = true;
		}
		if(req.getParameter("SearchOnly").equals("0")){
			SEARCHONLY = false;
		}
		
	}
	

}
