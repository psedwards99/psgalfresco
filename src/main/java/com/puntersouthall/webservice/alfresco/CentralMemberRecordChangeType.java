
package com.puntersouthall.webservice.alfresco;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for CentralMemberRecordChangeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CentralMemberRecordChangeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CentralMemberRecordID" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ChangesID" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="MemberDateOfBirth" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="MemberFirstName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MemberNINumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MemberSurname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CentralMemberRecordChangeType", propOrder = {
    "centralMemberRecordID",
    "changesID",
    "memberDateOfBirth",
    "memberFirstName",
    "memberNINumber",
    "memberSurname"
})
public class CentralMemberRecordChangeType {

    @XmlElement(name = "CentralMemberRecordID")
    protected Integer centralMemberRecordID;
    @XmlElement(name = "ChangesID")
    protected Integer changesID;
    @XmlElementRef(name = "MemberDateOfBirth", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<XMLGregorianCalendar> memberDateOfBirth;
    @XmlElementRef(name = "MemberFirstName", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> memberFirstName;
    @XmlElementRef(name = "MemberNINumber", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> memberNINumber;
    @XmlElementRef(name = "MemberSurname", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> memberSurname;

    /**
     * Gets the value of the centralMemberRecordID property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCentralMemberRecordID() {
        return centralMemberRecordID;
    }

    /**
     * Sets the value of the centralMemberRecordID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCentralMemberRecordID(Integer value) {
        this.centralMemberRecordID = value;
    }

    /**
     * Gets the value of the changesID property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getChangesID() {
        return changesID;
    }

    /**
     * Sets the value of the changesID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setChangesID(Integer value) {
        this.changesID = value;
    }

    /**
     * Gets the value of the memberDateOfBirth property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public JAXBElement<XMLGregorianCalendar> getMemberDateOfBirth() {
        return memberDateOfBirth;
    }

    /**
     * Sets the value of the memberDateOfBirth property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public void setMemberDateOfBirth(JAXBElement<XMLGregorianCalendar> value) {
        this.memberDateOfBirth = value;
    }

    /**
     * Gets the value of the memberFirstName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getMemberFirstName() {
        return memberFirstName;
    }

    /**
     * Sets the value of the memberFirstName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setMemberFirstName(JAXBElement<String> value) {
        this.memberFirstName = value;
    }

    /**
     * Gets the value of the memberNINumber property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getMemberNINumber() {
        return memberNINumber;
    }

    /**
     * Sets the value of the memberNINumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setMemberNINumber(JAXBElement<String> value) {
        this.memberNINumber = value;
    }

    /**
     * Gets the value of the memberSurname property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getMemberSurname() {
        return memberSurname;
    }

    /**
     * Sets the value of the memberSurname property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setMemberSurname(JAXBElement<String> value) {
        this.memberSurname = value;
    }

}
