package com.puntersouthall.helper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public final class NodeServiceHelper {
		
	//returns noderef for Company home
    public static NodeRef getCompanyHome(SearchService searchService) {
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home\"");
        NodeRef companyHomeNodeRef = null;
        try {
            if (rs.length() == 0) {
                throw new AlfrescoRuntimeException("Didn't find Company Home");
            }
            companyHomeNodeRef = rs.getNodeRef(0);
        } finally {
            rs.close();
        }
        return companyHomeNodeRef;
    }
	
	 //creates a child node if one doesnt exist, otherwise returns the existing one.
    public static NodeRef createChildNode(NodeService nodeService, NodeRef parent, String name, QName contentType)
    {
    	NodeRef node = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, name);
    	if (node != null){
    		return node;
    	}
        // Create a map to contain the values of the properties of the node
            
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, name);  

        // use the node service to create a new node
        node = nodeService.createNode(
                            parent, 
                            ContentModel.ASSOC_CONTAINS, 
                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                            contentType, 
                            props).getChildRef();
                            
        // Return a node reference to the newly created node
        return node;
    } 
    
    //creates a child node if one doesnt exist, otherwise returns the existing one.
    public static NodeRef createChildNode(NodeService nodeService, ContentService contentService, NodeRef parent, String name, QName contentType, String text)
    {
    	NodeRef node = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, name);
    	if (node != null){
    		return node;
    	}

        // Create a map to contain the values of the properties of the node
            
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, name);  

        // use the node service to create a new node
        node = nodeService.createNode(
                            parent, 
                            ContentModel.ASSOC_CONTAINS, 
                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                            contentType, 
                            props).getChildRef();
                            
        // Use the content service to set the content onto the newly created node
        ContentWriter writer = contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(text);
        
        // Return a node reference to the newly created node
        return node;
    }
    
    //file documents in relevant error folder in repository when method action is not possible
   	public static void moveToErrorFolder(NodeService nodeService, SearchService searchService, NodeRef node, String errorFolderName){
   		NodeRef companyHome = NodeServiceHelper.getCompanyHome(searchService);
   		NodeRef filingErrorsNodeRef = NodeServiceHelper.createChildNode(nodeService, companyHome, errorFolderName, ContentModel.TYPE_FOLDER);
   		nodeService.moveNode(node, filingErrorsNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) nodeService.getProperty(node,ContentModel.PROP_NAME)));
   		
   	}  
}
