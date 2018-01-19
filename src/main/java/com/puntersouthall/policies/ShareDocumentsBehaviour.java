package com.puntersouthall.policies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author vkajamugan
 *
 */
public class ShareDocumentsBehaviour implements NodeServicePolicies.OnUpdatePropertiesPolicy,
        NodeServicePolicies.OnRemoveAspectPolicy
{

    /**
     * permission service to set permissions
     */
    private PermissionService permissionService;

    /**
     * policy component to create behaviours
     */
    private PolicyComponent policyComponent;

    /**
     * updateProperties behaviour
     */
    private Behaviour updateProperties;

    /**
     * removeAspect behaviour
     */
    private Behaviour removeAspect;

    /**
     * Cashiering team group name
     */
    private String groupName;

    /**
     * psdoc:type property value
     */
    private String propertyName;
    
    /**
     * Signatories group name
     */
    private String signatoriesGroupName;

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
        // setting the permission service from service registry
        this.permissionService = serviceRegistry.getPermissionService();
    }

    /**
     * setter method for groupName
     * 
     * @param groupName
     */
    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    /**
     * setter method for propertyName
     * 
     * @param propertyName
     */
    public void setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;
    }

    /**
     * setter method for signatoriesGroupName
     * 
     * @param signatoriesGroupName
     */
    public void setSignatoriesGroupName(String signatoriesGroupName)
    {
        this.signatoriesGroupName = signatoriesGroupName;
    }
    
    /**
     * method to initialize the behaviours
     */
    public void init()
    {

        // Initialize behaviours
        this.updateProperties = new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.EVERY_EVENT);
        this.removeAspect = new JavaBehaviour(this, "onRemoveAspect", NotificationFrequency.EVERY_EVENT);

        // Bind behaviours to node policies
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                QName.createQName("custom.model.jb", "generalAspect"), this.updateProperties);
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveAspect"),
                QName.createQName("custom.model.jb", "generalAspect"), this.removeAspect);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr
     * .repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> oldProps, Map<QName, Serializable> newProps)
    {
        // QName for psdoc:type
        QName psDocQName = QName.createQName("custom.model.jb", "type");
        
        ArrayList<String> newPropList = new ArrayList<String>();
    	newPropList = (ArrayList<String>) newProps.get(psDocQName);

    	ArrayList<String> oldPropList = new ArrayList<String>();
    	oldPropList = (ArrayList<String>) oldProps.get(psDocQName);
    	
        // Checking the psdoc:type got changed or not
        if ((oldProps != null) && (newProps != null))
        {
            if ((oldProps.get(psDocQName)) != (newProps.get(psDocQName)))
            {
                if (newProps.get(psDocQName) != null)
                {
                    // new property & old property are not equal and new property is expected property name
                	
                	
                	
                    if (newPropList.contains(propertyName))
                    {
                        // setting the permission
                        setWritePermission(nodeRef);
                    }
                    else if (oldProps.get(psDocQName) != null)
                    {
                        // new property is not expected property name, but old property is & got changed
                        if (oldPropList.contains(propertyName))
                        {
                            // removing the permission
                            removeWritePermission(nodeRef);
                        }
                    }
                }
                else if (oldProps.get(psDocQName) != null)
                {
                    // New property is null & old property is expected property name
                    if (oldPropList.contains(propertyName))
                    {
                        // removing the permission
                        removeWritePermission(nodeRef);
                    }
                }
            }
        }
        else if (newProps != null)
        {
            if (newProps.get(psDocQName) != null)
            {
                // Old property is null & new property is expected property name
                if (newPropList.contains(propertyName))
                {
                    // setting the permission
                    setWritePermission(nodeRef);
                }
            }
        }
        else if (oldProps != null)
        {
            if (oldProps.get(psDocQName) != null)
            {
                // New property is null & old property is expected property name
                if (oldPropList.contains(propertyName))
                {
                    // removing the permission
                    removeWritePermission(nodeRef);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.repo.node.NodeServicePolicies.OnRemoveAspectPolicy#onRemoveAspect(org.alfresco.service.cmr.repository
     * .NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void onRemoveAspect(NodeRef nodeRef, QName arg1)
    {
        // removing the permission
        removeWritePermission(nodeRef);

    }

    /**
     * setting the read & write permission to cashiering team
     * 
     * @param nodeRef
     */
    private void setWritePermission(NodeRef nodeRef)
    {
        // set the user as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        // check whether the group has the permissions or not
        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);
        boolean isWritePermissionSet = false;
        boolean isReadPermissionSet = false;
        for (AccessPermission permission : permissions)
        {
            if ((permission.getAuthority().equals(groupName))
                    && (permission.getPermission().equals(PermissionService.WRITE)))
            {
                isWritePermissionSet = true;
            }
            else if ((permission.getAuthority().equals(groupName))
                    && (permission.getPermission().equals(PermissionService.READ)))
            {
                isReadPermissionSet = true;
            }
        }
        // Group doesn't have the read permission
        if (!isReadPermissionSet)
        {
            // set the read permission
            permissionService.setPermission(nodeRef, groupName, PermissionService.READ, true);
            
            
        }
        // Group doesn't have the write permission
        if (!isWritePermissionSet)
        {
            // set the write permission
            permissionService.setPermission(nodeRef, groupName, PermissionService.WRITE, true);
            permissionService.setPermission(nodeRef, groupName, PermissionService.CANCEL_CHECK_OUT, true);
            permissionService.setPermission(nodeRef, groupName, PermissionService.CHECK_IN, true);
            permissionService.setPermission(nodeRef, groupName, PermissionService.CHECK_OUT, true);
                    
            permissionService.setPermission(nodeRef, signatoriesGroupName, PermissionService.READ, true);
        }
    }

    /**
     * removing the write permission for cashiering team
     * 
     * @param nodeRef
     */
    private void removeWritePermission(NodeRef nodeRef)
    {
        // set the user as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        // check whether the group has the permissions or not
        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);
        boolean isWritePermissionSet = false;
        for (AccessPermission permission : permissions)
        {
            if ((permission.getAuthority().equals(groupName))
                    && (permission.getPermission().equals(PermissionService.WRITE)))
            {
                isWritePermissionSet = true;
            }
        }
        // Group has write permission set
        if (isWritePermissionSet)
        {
            // remove the write permission
            permissionService.deletePermission(nodeRef, groupName, PermissionService.WRITE);
            permissionService.deletePermission(nodeRef, groupName, PermissionService.CANCEL_CHECK_OUT);
            permissionService.deletePermission(nodeRef, groupName, PermissionService.CHECK_IN);
            permissionService.deletePermission(nodeRef, groupName, PermissionService.CHECK_OUT);
            
            permissionService.deletePermission(nodeRef, signatoriesGroupName, PermissionService.READ);
        }
    }
}
