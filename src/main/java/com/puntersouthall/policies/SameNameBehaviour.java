package com.puntersouthall.policies;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SameNameBehaviour implements BeforeCreateNodePolicy {
    private final Log logger = LogFactory.getLog(SameNameBehaviour.class);

    /** Enable/Disable the policy */
    protected boolean policySameNameEnalbed = false;

    /** The policy component */
    private PolicyComponent policyComponent;

    /** Alfresco services */
    private ServiceRegistry serviceRegistry;
    private NodeService nodeService;
    private NamespaceService namespaceService;

    public void setPolicySameNameEnalbed(boolean policySameNameEnalbed) {
        this.policySameNameEnalbed = policySameNameEnalbed;
    }
    
    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.nodeService = serviceRegistry.getNodeService();
    }

    /** Init the policy */
    public void init() {
        if (this.policySameNameEnalbed) {
            policyComponent.bindClassBehaviour(BeforeCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this,
                    "beforeCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
            logger.info("com.puntersouthall.policies.SameNameBehaviour.init(): Initialiased policy \"beforeCreateNode\" for type cm:content.");
        } else {
            logger.info("com.puntersouthall.policies.SameNameBehaviour.init(): Policy \"beforeCreateNode\" for type cm:content is disabled.");
        }
    }

    @Override
    public void beforeCreateNode(NodeRef parentRef, QName assocTypeQName, QName assocQName, QName nodeTypeQName) {

        // Set the file name
        String fileName = assocQName.getLocalName();
        if (logger.isDebugEnabled()) {
            logger.debug("SameNameBehaviour.beforeCreateNode(): filename = " + fileName);
        }

        // Check if a node with the same name exist
        NodeRef existingFile = nodeService.getChildByName(parentRef, assocTypeQName, fileName);

        int counter = 1;
        String tmpFileName = fileName;

        // Building the new file name
        while (existingFile != null) {

            int dotIndex = fileName.lastIndexOf(".");
            dotIndex = fileName.lastIndexOf(".");

            if (dotIndex == 0) {
                // File didn't have a proper 'name' instead it had just a suffix and started with a ".", create "1.txt"
                tmpFileName = counter + fileName;
            } else if (dotIndex > 0) {
                // Filename contained ".", create "filename-1.txt"
                tmpFileName = fileName.substring(0, dotIndex) + "-" + counter + fileName.substring(dotIndex);
            } else {
                // Filename didn't contain a dot at all, create "filename-1"
                tmpFileName = fileName + "-" + counter;
            }

            existingFile = nodeService.getChildByName(parentRef, assocTypeQName, tmpFileName);
            counter++;
        }
        
        // Define the hash map where add the properties for the node
        final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        // add the file name
        properties.put(ContentModel.PROP_NAME, tmpFileName);

        // Create the node
        nodeService.createNode(parentRef, assocTypeQName, QName.createQName("cm:" + tmpFileName, namespaceService), ContentModel.TYPE_CONTENT, properties);

    }

}
