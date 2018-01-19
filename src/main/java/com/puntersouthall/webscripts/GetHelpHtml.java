package com.puntersouthall.webscripts;

import com.puntersouthall.model.PunterSouthallCustomModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetHelpHtml extends AbstractWebScript
{
	private SearchService searchService;
	private NodeService nodeService;
	private ContentService contentService;
	
	 private static Integer LIMIT = 10;

	/**
     * Inject the Alfresco service registry
     * 
     * @param serviceRegistry Alfresco service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.searchService = serviceRegistry.getSearchService();
        this.nodeService = serviceRegistry.getNodeService();
        this.contentService = serviceRegistry.getContentService();
    }
    
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
    	final String activityId = req.getParameter("activity");
    	final String subActivityId = req.getParameter("subActivity");
    	final String query = "TYPE:\"psdoc:tasklist\" AND @psdoc\\:activity:\"" + activityId + "\" AND @psdoc\\:subActivity:\"" + subActivityId + "\"";
    	AuthenticationUtil.setRunAsUserSystem();
    	List<NodeRef> nodes = search( query, 10);
    	JSONObject resObj = new JSONObject();
    	List<JSONObject> nodesJson = new ArrayList();
    	try 
    	{
	    	for(NodeRef nodeRef : nodes){
	    		if(activityId.equals(nodeService.getProperty(nodeRef, PunterSouthallCustomModel.PROP_ACTIVITY)) & subActivityId.equals(nodeService.getProperty(nodeRef, PunterSouthallCustomModel.PROP_SUB_ACTIVITY))){
	    			JSONObject nodeObj = new JSONObject();
					nodeObj.put("name", nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
					ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
					String version = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
					nodeObj.put("version", version);
		    	    String content = reader.getContentString();
		    		nodeObj.put("content", content);
		    		nodesJson.add(nodeObj);
	    			
	    		}
	    		else{
	    			//null do nothing if activity and sub activity are not an exact match
	    		}
	    	}
    		resObj.put("helphtmls", nodesJson);
    		String jsonString = resObj.toString();
	    	res.getWriter().write(jsonString);
		} catch (JSONException e1) {
			throw new WebScriptException("Unable to serialize JSON");
		}
    }    
    
    private List<NodeRef> search(String query, Integer limit)
    {
        ResultSet rs = null;
        List<NodeRef> nodes = new ArrayList<NodeRef>();

        try
        {
            // search parameters
            SearchParameters sp = new SearchParameters();
            sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setMaxPermissionChecks(limit != null ? limit : LIMIT);
            sp.setMaxItems(limit != null ? limit : LIMIT);
            sp.setLimit(limit != null ? limit : LIMIT);

            sp.setQuery(query);

            rs = searchService.query(sp);

            if (rs.length() > 0)
            {
                nodes = rs.getNodeRefs();
            }
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
        }

        return nodes;
    }
}
