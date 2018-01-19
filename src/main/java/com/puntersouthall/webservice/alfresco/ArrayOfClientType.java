
package com.puntersouthall.webservice.alfresco;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ArrayOfClientType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfClientType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ClientType" type="{http://puntersouthall.com/webservice/alfresco/}ClientType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfClientType", propOrder = {
    "clientType"
})
public class ArrayOfClientType {

    @XmlElement(name = "ClientType", nillable = true)
    protected List<ClientType> clientType;

    /**
     * Gets the value of the clientType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the clientType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClientType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ClientType }
     * 
     * 
     */
    public List<ClientType> getClientType() {
        if (clientType == null) {
            clientType = new ArrayList<ClientType>();
        }
        return this.clientType;
    }

}
