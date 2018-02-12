var node = document;

var servicesFolder = getCreateFolder(companyhome, "Services");
var automaticFolder = getCreateFolder(servicesFolder,"Automatic Workflow Creator");
var successFolder = getCreateFolder(automaticFolder, "Successful uploads");
var failedFolder = getCreateFolder(automaticFolder, "Failed uploads");
var tempFolder = getCreateFolder(companyhome, "Temporary Workings");
var tempFolder = getCreateFolder(tempFolder, "Automatic Workflow Creator");
var holdingFolder = companyhome.childByNamePath("Uploads/Holding Folder/");
var processSucess = true;

logger.info("Node being actioned by automatic workflow launcher : " +  node.nodeRef);
var wf = processData(node.content);
logger.info("Number of rows being processed : " + wf.length);


var keys = [];
for (var prop in wf[0]) {
    if (wf[0].hasOwnProperty(prop)) {
		keys.push(prop);
		//attContent += prop + ': ' + wf[0][prop] + "\n";
    }
}
keys.sort();

for (var i = 0; i <wf.length; i++) {
	var memberID = wf[i].MemberID;
	var penscopeSchemeID = wf[i].PenscopeSchemeID;
	var doer = wf[i].Doer;
	var teamName =  wf[i].TeamName;
	var subActivity = wf[i].SubActivity;




	var jsonArray = psalWebService.getMemberDetails(null, memberID, null,null,penscopeSchemeID,null,null,null);
	var numberOfMember = jsonArray.length();



	if(numberOfMember == 1){

		var SchemeName = jsonArray.get(0).getString("SchemeName");
		var clientGroup  = "ALF-Client-" + jsonArray.get(0).getString("ClientID") + "-Admin-FullAccess";
		//var clientGroup  = "ALF-Client-1206-Admin-FullAccess";
		logger.debug("Client group string being searched for : " + clientGroup);
		var workflowParameters = [];
		if (doer != ""){
			logger.debug("doer found : " + doer);
			workflowParameters["pswf:doer"] = doer;
			workflowParameters["pswf:decision"] = "Pass to doer";
			workflowParameters["pswf:certificateWorkflowRequired"] = false;
			workflowParameters["pswf:calcs"] = 1;
			workflowParameters["pswf:teamName"] = teamName;
			workflowParameters["pswf:subActivity"] = subActivity;

		}
		//var benefitStatus = member.get(0).getBenefitStatus().value;
		//var benefitSubStatus = member.get(0).getBenefitSubStatus().value;
		var centralMemberRecordID = jsonArray.get(0).getString("CentralMemberRecordID");
		var clientID = jsonArray.get(0).getString("ClientID");
		var clientName = jsonArray.get(0).getString("ClientName");
		var databaseMemberID = jsonArray.get(0).getString("DatabaseMemberID");
		//var dateJoinedScheme = member.get(0).getDateJoinedScheme().value;
		var dateOfBirth = jsonArray.get(0).getString("DateOfBirth");
		//var convertedDateOfBirth = new Date(dateOfBirth.getYear(), dateOfBirth.getMonth()-1, dateOfBirth.getDay(),12);
		//convertedDateOfBirth = utils.toISO8601(convertedDateOfBirth);
		var firstName = jsonArray.get(0).getString("FirstName");
		var nino =jsonArray.get(0).getString("NINumber");
		var schemeID = jsonArray.get(0).getString("SchemeID");
		var schemeName = jsonArray.get(0).getString("SchemeName");
		//var schemeType = member.get(0).getSchemeType().value;
		var surname = jsonArray.get(0).getString("Surname");

		//var dest = companyhome.childByNamePath("Uploads/Move Document");

		var taskType = "Admin-MemberWork";

		//if(wf[i].Scheme_Member_task == "Member"){taskType = 'Admin-MemberWork';}
		//else{ taskType = 'Admin-SchemeWork';}

		var activity = wf[i].Activity;

		if (taskType == 'Admin-MemberWork' ){
			workflowParameters["pswf:centralID"] = centralMemberRecordID;
			workflowParameters["pswf:relevantMemberID"] = databaseMemberID;
			workflowParameters["pswf:surname"] = surname;
			workflowParameters["pswf:firstName"] = firstName;
			workflowParameters["pswf:nino"] = nino;
			workflowParameters["pswf:dob"] = dateOfBirth;
			var groupAssignee = people.getGroup(clientGroup);
			workflowParameters["bpm:groupAssignee"] = groupAssignee;
			logger.debug("client group found is : " + groupAssignee);
			workflowParameters["pswf:activityVariableForVOCreateTask"] = "IC "+ activity;
		}


		//workflowParameters["pswf:taskNumber"] = foundNode.properties['psdoc:taskNumber'];
		//print(foundNode.properties['psdoc:taskNumber']);
		workflowParameters["pswf:calcs"] = 1;
		workflowParameters["pswf:workflowTaskType"] = taskType;
		workflowParameters["pswf:sourceSystem"] = "Manual launch - Bulk Load";
		workflowParameters["pswf:adminSchemeGroup"] = "GROUP_" + clientGroup;
		//workflowParameters["pswf:teamName"] = clientGroup;
		workflowParameters["pswf:clientID"] = clientID;
		workflowParameters["pswf:clientName"] = clientName;
		workflowParameters["pswf:adminSchemeID"] = schemeID;
		workflowParameters["pswf:adminSchemeName"] = schemeName;

		workflowParameters["pswf:activity"] = activity;


		var dateRec = parseDate(wf[i].WorkflowStartDate);  // Jan is 0
		dateRec = utils.toISO8601(dateRec);
		workflowParameters["pswf:dateReceived"] = dateRec;
		// create attachment as a text file with info.
		var attachment =  tempFolder.createFile("workflow information " + memberID + ".txt");
		var attContent = "";
		for (c = 0; c <keys.length; c++) {
			k = keys[c];
			attContent += k + ': ' + wf[i][k]+ "\n";
		}
		//print(attContent);
		attachment.content = attContent;
		attachment.save();
		//logger.info("attachment file - " + attachment.content);


		var workflowDefinition = workflow.getDefinitionByName("activiti$adminMemberWorkflowProcess");
		//outputFile.content = workflowDefinition;
		try{
			var workflowPackage = workflow.createPackage();
			workflowPackage.addNode(attachment);
			attachment.move(holdingFolder);
			//logger.info("attempting to launch workflow");
			var workflowPath = workflowDefinition.startWorkflow(workflowPackage, workflowParameters);
			logger.info("workflow for leaver with member id : "+ memberID + " " + firstName + " "  + surname +  " created.");
		}
		catch(e){
			processSucess = false;
			logger.error("ERROR - workflow for leaver with member id : "+ memberID + " " + firstName + " "  + surname + " not created, " + e.message);
		}



	}
	else{
		processSucess = false;
		logger.info("workflow for leaver with member id : "+ memberID + " not created, could not find single member in CMDB.");
	}

}
if(processSucess){
	node.move(successFolder);
	logger.info("Process complete with no problems");
}
else{
	node.move(failedFolder);
	logger.info("Process complete with problems, check log file.");
}


function processData(allText) {
    var allTextLines = allText.split("\n");
	//("all text lines - " +allTextLines[0]);
	//logger.log(allTextLines.toString());
    var headers = allTextLines[0].split(',');
    var lines = [];
	logger.info("Total Number of lines inc header : " + allTextLines.length);
    for (var k=1; k<allTextLines.length; k++) {
        var data = allTextLines[k].split(',');

        if (data.length == headers.length) {

            var tarr = [];
			var dataRowObj = {};
            for (var j=0; j<headers.length; j++) {
                //tarr.push(headers[j]+":"+data[j]);
				dataRowObj[headers[j]] = replaceComma(data[j]);

            }
			//lines.push(tarr);
            lines.push(dataRowObj);
        }
    }

     return(lines);
}

function replaceComma(str){
	str = str.replace("--COMMA--",",");
	return str;
}


//will parse date in the format dd/mm/yyyy
function parseDate(date){
	var day = date.substring(0, 2);
	var month = date.substring(3, 5);
	var year = date.substring(6, 10);

	return new Date(year, month-1, day,12);


}

function getCreateFolder(parent, folderName){
	var folder = parent.childByNamePath(folderName);
	if (folder!= null){
		return folder;
	}
	else{
		return parent.createFolder(folderName);
	}

}