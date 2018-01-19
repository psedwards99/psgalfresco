package com.puntersouthall.helper;

import org.apache.commons.logging.Log;

//custom PS log4j functions
public class PSErrorLoggingHelper {
	//writes out error to log file
    public static void psErrorLogger (Log logger, String loggedInUser, String nodeRefFileInfo, String message)
    {
    	//message to be displayed in log file
    	String errorMessage = "USERNAME: " +  loggedInUser + " ; NODEREF INFO: " + nodeRefFileInfo + " ; MESSAGE: " + message;
    	//write to log file
		logger.error(errorMessage); 		
    }      
}
