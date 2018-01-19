// Update the assignee of the task 

var task = workflow.getTask("activiti$"+args["taskID"].toString());
var properties = task.properties;
// logger.log(properties["owner"]);
properties["cm:owner"] = args["Assignee"] ;


if (args["TaskStage"] == "Do Task" ||
	args["TaskStage"] == "Re-Do Task" ||
	args["TaskStage"] == "Do Certificates" ||
	args["TaskStage"] == "File deletion request"){
	properties["pswf:doer"] = args["Assignee"] ;
	properties["pswf:teamName"] = "GROUP_" + args["TeamName"] ;
}
else if (args["TaskStage"] == "Check Task" || 
		 args["TaskStage"] == "Workflow Deletion Request" || 
		 args["TaskStage"] == "Check Certificates" ||
		 args["TaskStage"] == "File Deletion Request - Authorise"){
	properties["pswf:checker"] = args["Assignee"] ;
}
else if (args["TaskStage"] == "Authorise Task"){
	properties["pswf:authoriser"] = args["Assignee"] ;
}
else if (args["TaskStage"] == "Diary note deletion request"){
	properties["pswf:diaryDecisionMadeBy"] = args["Assignee"] ;
}

task.setProperties(properties); 