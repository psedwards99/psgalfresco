
package com.puntersouthall.webservice.alfresco;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;


/**
 * <p>Java class for SchemeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SchemeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SchemeID" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="SchemeName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SystemSchemeID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SchemeType", propOrder = {
    "schemeID",
    "schemeName",
    "systemSchemeID"
})
public class SchemeType {

    @XmlElement(name = "SchemeID")
    protected Integer schemeID;
    @XmlElementRef(name = "SchemeName", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> schemeName;
    @XmlElementRef(name = "SystemSchemeID", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> systemSchemeID;

    /**
     * Gets the value of the schemeID property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSchemeID() {
        return schemeID;
    }

    /**
     * Sets the value of the schemeID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSchemeID(Integer value) {
        this.schemeID = value;
    }

    /**
     * Gets the value of the schemeName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSchemeName() {
        return schemeName;
    }

    /**
     * Sets the value of the schemeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSchemeName(JAXBElement<String> value) {
        this.schemeName = value;
    }

    /**
     * Gets the value of the systemSchemeID property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSystemSchemeID() {
        return systemSchemeID;
    }

    /**
     * Sets the value of the systemSchemeID property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSystemSchemeID(JAXBElement<String> value) {
        this.systemSchemeID = value;
    }

}
