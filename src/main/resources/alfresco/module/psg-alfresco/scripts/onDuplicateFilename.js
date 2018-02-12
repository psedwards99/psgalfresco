// Upload component was configured to find a new unique name for clashing filenames

var destNode = behaviour.args[1];
var document = behaviour.args[0].child;
var filename = document.properties["cm:name"];

// Check if document has been uploaded as part of a bulk upload
// and if so get the bulk upload aspect properties
var documentIsBulkUpload = document.hasAspect("psdoc:bulkUploadAspect");
var documentUploadReference = null;
var documentPreUploadName = null;

if (documentIsBulkUpload && (document.properties["psdoc:initiatingDocWorkflowID"] == null)) {
	documentUploadReference = document.properties["psdoc:uploadReference"];
	documentPreUploadName = document.properties["psdoc:preUploadDocumentName"];
	filename = document.properties["psdoc:postUploadDocumentName"];
}

// Get the existing file (if it exists)
var existingFile = destNode.childByNamePath(filename);

var counter = 1;

//new lines
var counterForFilename = "0001";
var pad = "0000";


var dotIndex = null;

//new lines
dotIndex = filename.lastIndexOf(".");
var extension = filename.substr(dotIndex);
if (existingFile !== null && dotIndex > 3) {
	if (isNaN(filename.slice(dotIndex - 4, dotIndex))) {
		//do nothing
	}
	else{
		//remove last 5 characters
		if (filename[dotIndex - 5] == "-"){
			filename = filename.slice(0,dotIndex - 5) + extension;
		}
	}
}

//if no '.' then no extension
if (existingFile !== null && dotIndex == null && filename.length > 4) {
	if (isNaN(filename.slice(filename.length - 5, filename.length))) {
		//do nothing
	}
	else{
		//remove last 5 characters
		if (filename.length - 5){
			filename = filename.slice(0,filename.length - 5);
		}
	}
}

var tmpFilename = filename;

// While there is a file with the same filename in the folder
while (existingFile !== null) {
	// Check if the existing file was part of a bulk upload
	var existingFileIsBulkUpload = existingFile.hasAspect("psdoc:bulkUploadAspect");

	if (documentIsBulkUpload && existingFileIsBulkUpload) {

		var existingFileUploadReference = existingFile.properties["psdoc:uploadReference"];
		var existingFilePreUploadName = existingFile.properties["psdoc:preUploadDocumentName"];

		// If both the uploaded file and the existing file are part of the same upload process, then assumption is that the
		// second file will be the later version so delete the existing version
		if (documentUploadReference === existingFileUploadReference && documentPreUploadName === existingFilePreUploadName) {
			var existingDocument = search.findNode(existingFile.nodeRef);
			existingDocument.remove();
			break;
		}
	}

	dotIndex = filename.lastIndexOf(".");

    if (dotIndex == 0) {
    	// File didn't have a proper 'name' instead it had just a suffix and started with a ".", create "1.txt"
        tmpFilename = counterForFilename + filename;
	}
    else if (dotIndex > 0) {
    	// Filename contained ".", create "filename-1.txt"
        tmpFilename = filename.substring(0, dotIndex) + "-" + counterForFilename + filename.substring(dotIndex);

	}
    else {
    	// Filename didn't contain a dot at all, create "filename-1"
		tmpFilename = filename + "-" + counterForFilename;
	}

	existingFile = destNode.childByNamePath(tmpFilename);
    counter++;

	//new line
    counterForFilename = pad.substring(0, 4 - counter.toString().length) + counter.toString();
}

document.properties["cm:name"] = tmpFilename;
document.save();



/*var test = behaviour.args[0];
var test2 = ""*/