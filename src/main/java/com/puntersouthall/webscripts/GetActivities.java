package com.puntersouthall.webscripts;

import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfstring;
import com.puntersouthall.webservice.alfresco.IPunterSouthallService;
import com.puntersouthall.webservice.alfresco.PunterSouthallService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.List;

public class GetActivities extends AbstractWebScript
{
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
    	try
    	{
    		PunterSouthallService psService = new PunterSouthallService();
			IPunterSouthallService iPSService = psService.getBasicHttpBindingIPunterSouthallService();
			ArrayOfstring activitiesArray = iPSService.getActivities();
			List<String> activitiesList = activitiesArray.getString();
			
	    	// build a json object
	    	JSONObject obj = new JSONObject();
	    	
	    	// put some data on it
	    	obj.put("activities", activitiesList.toArray());
	    	
	    	// build a JSON string and send it back
	    	String jsonString = obj.toString();
	    	res.getWriter().write(jsonString);
    	}
    	catch(JSONException e)
    	{
    		throw new WebScriptException("Unable to serialize JSON");
    	}
    }    
}
