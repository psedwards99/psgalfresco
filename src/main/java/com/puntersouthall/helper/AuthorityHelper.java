package com.puntersouthall.helper;

import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.apache.log4j.Logger;

public class AuthorityHelper {

    //check if authority exists and create it if it doesn't
    public static void createAuthority(Logger log, AuthorityService authorityService, String authorityName ){

    	Boolean authorityExists = authorityService.authorityExists("GROUP_" + authorityName);
        
        if(authorityExists.equals(true)){
        	log.debug("Authority already exists: " + authorityName);
        }
        else{
        	authorityService.createAuthority(AuthorityType.GROUP, authorityName);
        	log.debug("Authority created: " + authorityName);
        }
        

    }
	
}
