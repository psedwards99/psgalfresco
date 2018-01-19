package com.puntersouthall.javascript;

import com.puntersouthall.webservice.alfresco.ArrayOfMemberType;
import com.puntersouthall.webservice.alfresco.IPunterSouthallService;
import com.puntersouthall.webservice.alfresco.PunterSouthallService;
import org.alfresco.repo.processor.BaseProcessorExtension;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.puntersouthall.webservice.alfresco.*;

public class WebServiceWrapper extends BaseProcessorExtension {

	
	private PunterSouthallService punterSouthallService;
	private IPunterSouthallService iPSService;

	
	public WebServiceWrapper(){
		this.punterSouthallService = new PunterSouthallService();
		this.iPSService = punterSouthallService.getBasicHttpBindingIPunterSouthallService();
		
	}
	


	
	public JSONArray getClientNames() throws JSONException{
		ArrayOfClientType clientsNames = iPSService.getClientNames();
		JSONArray jArray = new JSONArray();
		try
		{
		    
		    for (ClientType clientType: clientsNames.getClientType())
		    {
		         JSONObject client = new JSONObject();
		         client.put("ClientName", clientType.getClientName().getValue());
		         client.put("ClientID", clientType.getClientID());
		         jArray.put(client);
		    }
		
		} catch (JSONException jse) {
		    jse.printStackTrace();
		}	
				
		return jArray;
	}
	
	public JSONArray getMemberDetails(String CentralMemberRecordID, String DatabaseMemberID, String ClientID,String SchemeID,
			String PenScopeSchemeID,String FirstName,String Surname,String NINumber	) throws JSONException{
		
		if(CentralMemberRecordID == null) CentralMemberRecordID = "";
		if(DatabaseMemberID == null) DatabaseMemberID = "";
		if(ClientID == null) ClientID = "";
		if(SchemeID == null) SchemeID = "";
		if(PenScopeSchemeID == null) PenScopeSchemeID = "";
		if(FirstName == null) FirstName = "";
		if(Surname == null) Surname = "";
		if(NINumber == null) NINumber = "";
		
		
		String query = "{\"CentralMemberRecordID\":\"" + CentralMemberRecordID + "\"," 
			+	"\"DatabaseMemberID\":\"" + DatabaseMemberID + "\","
			+	"\"ClientID\":\"" + ClientID + "\","	
			+	"\"SchemeID\":\"" + SchemeID + "\","
			+	"\"PenScopeSchemeID\":\"" + PenScopeSchemeID + "\","
			+	"\"FirstName\":\"" + FirstName + "\","
			+	"\"Surname\":\"" + Surname + "\","
			+	"\"NINumber\":\"" + NINumber + "\"}";

		ArrayOfMemberType members = iPSService.getMemberDetails(query);
		JSONArray jArray = new JSONArray();
		try
		{
		    
		    for (MemberType memberType : members.getMemberType())
		    {
		         JSONObject member = new JSONObject();
		         member.put("BenefitStatus", memberType.getBenefitStatus().getValue());
		         member.put("BenefitSubStatus", memberType.getBenefitSubStatus().getValue());
		         member.put("CentralMemberRecordID", memberType.getCentralMemberRecordID().getValue());
		         member.put("ClientID", memberType.getClientID().getValue());
		         member.put("ClientName", memberType.getClientName().getValue());
		         member.put("DatabaseMemberID", memberType.getDatabaseMemberID().getValue());
		         member.put("DateJoinedScheme", memberType.getDateJoinedScheme().getValue());
		         member.put("DateOfBirth", memberType.getDateOfBirth().getValue());
		         member.put("FirstName", memberType.getFirstName().getValue());
		         member.put("NINumber", memberType.getNINumber().getValue());
		         member.put("PenScopeSchemeID", memberType.getPenScopeSchemeID().getValue());
		         member.put("SchemeID", memberType.getSchemeID().getValue());
		         member.put("SchemeName", memberType.getSchemeName().getValue());
		         member.put("SchemeType", memberType.getSchemeType().getValue());
		         member.put("Surname", memberType.getSurname().getValue());
		         jArray.put(member);
		    }
		
		} catch (JSONException jse) {
		    jse.printStackTrace();
		}	
				
		return jArray;
	}
	
	
	
}
