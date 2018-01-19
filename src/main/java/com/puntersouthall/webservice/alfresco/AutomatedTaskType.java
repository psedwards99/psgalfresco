
package com.puntersouthall.webservice.alfresco;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for AutomatedTaskType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AutomatedTaskType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Activity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AutomatedLaunchListID" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="CentralMemberRecordID" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ClientID" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ClientName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DatabaseMemberID" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="DateReceived" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Deadline" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="MemberDateOfBirth" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="MemberFirstName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MemberNINumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MemberSurname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SchemeName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SchemeNameID" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="SubActivity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TeamName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AutomatedTaskType", propOrder = {
    "activity",
    "automatedLaunchListID",
    "centralMemberRecordID",
    "clientID",
    "clientName",
    "comment",
    "databaseMemberID",
    "dateReceived",
    "deadline",
    "memberDateOfBirth",
    "memberFirstName",
    "memberNINumber",
    "memberSurname",
    "schemeName",
    "schemeNameID",
    "subActivity",
    "teamName"
})
public class AutomatedTaskType {

    @XmlElementRef(name = "Activity", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> activity;
    @XmlElement(name = "AutomatedLaunchListID")
    protected Integer automatedLaunchListID;
    @XmlElementRef(name = "CentralMemberRecordID", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> centralMemberRecordID;
    @XmlElement(name = "ClientID")
    protected Integer clientID;
    @XmlElementRef(name = "ClientName", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> clientName;
    @XmlElementRef(name = "Comment", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> comment;
    @XmlElementRef(name = "DatabaseMemberID", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> databaseMemberID;
    @XmlElement(name = "DateReceived")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateReceived;
    @XmlElementRef(name = "Deadline", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<XMLGregorianCalendar> deadline;
    @XmlElementRef(name = "MemberDateOfBirth", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<XMLGregorianCalendar> memberDateOfBirth;
    @XmlElementRef(name = "MemberFirstName", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> memberFirstName;
    @XmlElementRef(name = "MemberNINumber", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> memberNINumber;
    @XmlElementRef(name = "MemberSurname", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> memberSurname;
    @XmlElementRef(name = "SchemeName", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> schemeName;
    @XmlElement(name = "SchemeNameID")
    protected Integer schemeNameID;
    @XmlElementRef(name = "SubActivity", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> subActivity;
    @XmlElementRef(name = "TeamName", namespace = "http://puntersouthall.com/webservice/alfresco/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> teamName;

    /**
     * Gets the value of the activity property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getActivity() {
        return activity;
    }

    /**
     * Sets the value of the activity property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setActivity(JAXBElement<String> value) {
        this.activity = value;
    }

    /**
     * Gets the value of the automatedLaunchListID property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAutomatedLaunchListID() {
        return automatedLaunchListID;
    }

    /**
     * Sets the value of the automatedLaunchListID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAutomatedLaunchListID(Integer value) {
        this.automatedLaunchListID = value;
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
     *     {@link Integer }
     *     
     */
    public Integer getClientID() {
        return clientID;
    }

    /**
     * Sets the value of the clientID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setClientID(Integer value) {
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
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setComment(JAXBElement<String> value) {
        this.comment = value;
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
     * Gets the value of the dateReceived property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateReceived() {
        return dateReceived;
    }

    /**
     * Sets the value of the dateReceived property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateReceived(XMLGregorianCalendar value) {
        this.dateReceived = value;
    }

    /**
     * Gets the value of the deadline property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public JAXBElement<XMLGregorianCalendar> getDeadline() {
        return deadline;
    }

    /**
     * Sets the value of the deadline property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public void setDeadline(JAXBElement<XMLGregorianCalendar> value) {
        this.deadline = value;
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
     * Gets the value of the schemeNameID property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSchemeNameID() {
        return schemeNameID;
    }

    /**
     * Sets the value of the schemeNameID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSchemeNameID(Integer value) {
        this.schemeNameID = value;
    }

    /**
     * Gets the value of the subActivity property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSubActivity() {
        return subActivity;
    }

    /**
     * Sets the value of the subActivity property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSubActivity(JAXBElement<String> value) {
        this.subActivity = value;
    }

    /**
     * Gets the value of the teamName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTeamName() {
        return teamName;
    }

    /**
     * Sets the value of the teamName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTeamName(JAXBElement<String> value) {
        this.teamName = value;
    }

}
