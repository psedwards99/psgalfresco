
package com.puntersouthall.webservice.alfresco;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for MemberType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MemberType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="BenefitStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="BenefitSubStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CentralMemberRecordID" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ClientID" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ClientName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DatabaseMemberID" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="DateJoinedScheme" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="DateOfBirth" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="FirstName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NINumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PenScopeSchemeID" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="SchemeID" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="SchemeName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SchemeType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Surname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MemberType", propOrder = {
    "benefitStatus",
    "benefitSubStatus",
    "centralMemberRecordID",
    "clientID",
    "clientName",
    "databaseMemberID",
    "dateJoinedScheme",
    "dateOfBirth",
    "firstName",
    "niNumber",
    "penScopeSchemeID",
    "schemeID",
    "schemeName",
    "schemeType",
    "surname"
})
public class MemberType {

    @XmlElementRef(name = "BenefitStatus", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> benefitStatus;
    @XmlElementRef(name = "BenefitSubStatus", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> benefitSubStatus;
    @XmlElementRef(name = "CentralMemberRecordID", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> centralMemberRecordID;
    @XmlElementRef(name = "ClientID", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> clientID;
    @XmlElementRef(name = "ClientName", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> clientName;
    @XmlElementRef(name = "DatabaseMemberID", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> databaseMemberID;
    @XmlElementRef(name = "DateJoinedScheme", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<XMLGregorianCalendar> dateJoinedScheme;
    @XmlElementRef(name = "DateOfBirth", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<XMLGregorianCalendar> dateOfBirth;
    @XmlElementRef(name = "FirstName", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> firstName;
    @XmlElementRef(name = "NINumber", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> niNumber;
    @XmlElementRef(name = "PenScopeSchemeID", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> penScopeSchemeID;
    @XmlElementRef(name = "SchemeID", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> schemeID;
    @XmlElementRef(name = "SchemeName", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> schemeName;
    @XmlElementRef(name = "SchemeType", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> schemeType;
    @XmlElementRef(name = "Surname", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> surname;

    /**
     * Gets the value of the benefitStatus property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getBenefitStatus() {
        return benefitStatus;
    }

    /**
     * Sets the value of the benefitStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setBenefitStatus(JAXBElement<String> value) {
        this.benefitStatus = value;
    }

    /**
     * Gets the value of the benefitSubStatus property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getBenefitSubStatus() {
        return benefitSubStatus;
    }

    /**
     * Sets the value of the benefitSubStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setBenefitSubStatus(JAXBElement<String> value) {
        this.benefitSubStatus = value;
    }

    /**
     * Gets the value of the centralMemberRecordID property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getCentralMemberRecordID() {
        return centralMemberRecordID;
    }

    /**
     * Sets the value of the centralMemberRecordID property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setCentralMemberRecordID(JAXBElement<Integer> value) {
        this.centralMemberRecordID = value;
    }

    /**
     * Gets the value of the clientID property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getClientID() {
        return clientID;
    }

    /**
     * Sets the value of the clientID property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setClientID(JAXBElement<Integer> value) {
        this.clientID = value;
    }

    /**
     * Gets the value of the clientName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getClientName() {
        return clientName;
    }

    /**
     * Sets the value of the clientName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setClientName(JAXBElement<String> value) {
        this.clientName = value;
    }

    /**
     * Gets the value of the databaseMemberID property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getDatabaseMemberID() {
        return databaseMemberID;
    }

    /**
     * Sets the value of the databaseMemberID property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setDatabaseMemberID(JAXBElement<Integer> value) {
        this.databaseMemberID = value;
    }

    /**
     * Gets the value of the dateJoinedScheme property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public JAXBElement<XMLGregorianCalendar> getDateJoinedScheme() {
        return dateJoinedScheme;
    }

    /**
     * Sets the value of the dateJoinedScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public void setDateJoinedScheme(JAXBElement<XMLGregorianCalendar> value) {
        this.dateJoinedScheme = value;
    }

    /**
     * Gets the value of the dateOfBirth property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public JAXBElement<XMLGregorianCalendar> getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Sets the value of the dateOfBirth property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public void setDateOfBirth(JAXBElement<XMLGregorianCalendar> value) {
        this.dateOfBirth = value;
    }

    /**
     * Gets the value of the firstName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getFirstName() {
        return firstName;
    }

    /**
     * Sets the value of the firstName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setFirstName(JAXBElement<String> value) {
        this.firstName = value;
    }

    /**
     * Gets the value of the niNumber property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getNINumber() {
        return niNumber;
    }

    /**
     * Sets the value of the niNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setNINumber(JAXBElement<String> value) {
        this.niNumber = value;
    }

    /**
     * Gets the value of the penScopeSchemeID property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getPenScopeSchemeID() {
        return penScopeSchemeID;
    }

    /**
     * Sets the value of the penScopeSchemeID property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setPenScopeSchemeID(JAXBElement<Integer> value) {
        this.penScopeSchemeID = value;
    }

    /**
     * Gets the value of the schemeID property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getSchemeID() {
        return schemeID;
    }

    /**
     * Sets the value of the schemeID property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setSchemeID(JAXBElement<Integer> value) {
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
     * Gets the value of the schemeType property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSchemeType() {
        return schemeType;
    }

    /**
     * Sets the value of the schemeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSchemeType(JAXBElement<String> value) {
        this.schemeType = value;
    }

    /**
     * Gets the value of the surname property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSurname() {
        return surname;
    }

    /**
     * Sets the value of the surname property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSurname(JAXBElement<String> value) {
        this.surname = value;
    }

}
