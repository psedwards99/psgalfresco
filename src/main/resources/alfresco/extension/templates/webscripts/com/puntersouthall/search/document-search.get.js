function mapProperty(propertyValue) {
	switch (propertyValue) {
		case 'cm:name' :
			return 'cmis:name';
		default :
			return propertyValue;
	}
}

function mapAspect(aspectValue) {
	switch (aspectValue) {
		case 'cm:cmobject' :
			return 'cmis:document';
		default :
			return aspectValue;
	}
}

function parseConfig() {
	var cfg = new XML(config.script), result = {};

	cfg = cfg.search;

	// loop through all search configurations
	for ( var i in cfg) {
		var id = cfg[i]['@id'].toString();

		// create search configuration object
		result[id] = {
			types : [],
			required : {},
			parameters : {},
			fields : [],
			filterBy : []
		};

		// get configuration values
		var t = cfg[i].type, r = cfg[i].required, p = cfg[i].parameter, f = cfg[i].response;

		// parse types from search configuration
		for ( var j in t) {
			result[id].types.push(t[j]['@name'].toString());
		}

		// parse response fields from search configuration
		for ( var j in f) {
			result[id].fields.push(f[j]['@property'].toString());

			if (f[j]['@filter'] && f[j]['@filter'].toString() === 'true') {
				result[id].filterBy.push({
					aspect : (f[j]['@aspect'] && f[j]['@aspect'].toString() ? mapAspect(f[j]['@aspect'].toString()) : null),
					property : mapProperty(f[j]['@property'].toString()),
					operator : (f[j]['@operator'] && f[j]['@operator'].toString() ? f[j]['@operator'].toString() : '='),
					type : (f[j]['@type'] && f[j]['@type'].toString() ? f[j]['@type'].toString() : 'quoted')
				});
			}
		}

		// parse required parameters from configuration (default operator to = and type to quoted)
		for ( var j in r) {
			var el = r[j];
			result[id].required[el['@name'].toString()] = {
				aspect : (el['@aspect'] && el['@aspect'].toString() ? mapAspect(el['@aspect'].toString()) : null),
				property : mapProperty(el['@property'].toString()),
				operator : (el['@operator'] && el['@operator'].toString() ? el['@operator'].toString() : '='),
				type : (el['@type'] && el['@type'].toString() ? el['@type'].toString() : 'quoted')
			};
		}

		// parse optional parameters from configuration (default operator to = and type to quoted)
		for ( var j in p) {
			var el = p[j];
			result[id].parameters[el['@name'].toString()] = {
				aspect : (el['@aspect'] && el['@aspect'].toString() ? mapAspect(el['@aspect'].toString()) : null),
				property : mapProperty(el['@property'].toString()),
				operator : (el['@operator'] && el['@operator'].toString() ? el['@operator'].toString() : '='),
				type : (el['@type'] && el['@type'].toString() ? el['@type'].toString() : 'quoted')
			};
		}
	}

	return result;
}

function buildQuery(type, aspects, cfg, filterValue) {

	// regex that matches ISO date string
	var dateRegex = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})\.(\d{3})(Z|((\+|-)(\d{2}):(\d{2})))$/;

	// build query base
	// e.g. select * from cmis:document as cmis_document
	var query = 'select * from ' + type + ' as ' + type.replace(':', '_') + ' ';

	// join all aspects
	// e.g. join sys:temporary as sys_temporary on cmis_document.cmis:objectId = sys_temporary.cmis:objectId
	for ( var i in aspects) {
		query += 'join ' + aspects[i] + ' as ' + aspects[i].replace(':', '_') + ' on ' + type.replace(':', '_') + '.cmis:objectId = '
				+ aspects[i].replace(':', '_') + '.cmis:objectId ';
	}

	// filter by required and optional parameters
	query += 'where ';

	// loop through required parameters and append filters
	// e.g. and sys_temporary.some:property = 'some value'
	var first = true;
	for ( var i in cfg.required) {
		var r = cfg.required[i];

		query += (first ? '' : 'and ');
		query += (r.aspect ? r.aspect.replace(':', '_') : type.replace(':', '_')) + '.' + r.property;
		query += ' ' + r.operator;

		// quote value if required
		if (r.type == 'literal') {
			// add value
			query += ' ' + args[i] + ' ';
		} else {
			// add timestamp if in date format
			query += (dateRegex.test('' + args[i])) ? ' timestamp' : '';

			// escape quote
			query += ' \'' + args[i].replace('\'', '\\\'') + '\' ';
		}

		first = false;
	}

	// loop through optional parameters and append filters (if parameter is provided)
	// e.g. and sys_temporary.some:property < some value
	for ( var i in cfg.parameters) {
		if (args[i]) {
			var r = cfg.parameters[i];

			query += (first ? '' : 'and ');
			query += (r.aspect ? r.aspect.replace(':', '_') : type.replace(':', '_')) + '.' + r.property;
			query += ' ' + r.operator;

			// quote value if required
			if (r.type == 'literal') {
				query += ' ' + args[i] + ' ';
			} else {
				// add timestamp if in date format
				query += (dateRegex.test('' + args[i])) ? ' timestamp' : '';

				// escape quote
				query += ' \'' + args[i].replace('\'', '\\\'') + '\' ';
			}

			first = false;
		}
	}

	// loop through fields available for filtering and append filters (if parameter is provided)
	// e.g. and (sys_temporary.some:property < some value or sys_temporary.someOther:property like '%som"')
	if (filterValue) {

		var firstFilterValue = true;
		var numberRegex = /^[+-]?((\.\d+)|(\d+(\.\d+)?))$/;
		for ( var i in cfg.filterBy) {
			var f = cfg.filterBy[i];
			
			if(f.type == 'literal' && !(numberRegex.test('' + filterValue))) {
				continue; 
			}

			query += (!first && firstFilterValue ? 'and ' : '');

			query += (firstFilterValue ? '( ' : 'or ');

			query += (f.aspect ? f.aspect.replace(':', '_') : type.replace(':', '_')) + '.' + f.property;
			query += ' ' + f.operator;

			// quote value if required
			if (f.type == 'literal') {
				query += ' ' + filterValue + ' ';
			} else {
				// add timestamp if in date format
				query += (dateRegex.test('' + filterValue)) ? ' timestamp' : '';

				var filterValueToAppend = (f.operator == 'like' || f.operator == "not like") ? '%' + filterValue + '%' : filterValue;

				// escape quote
				query += ' \'' + filterValueToAppend.replace('\'', '\\\'') + '\' ';
			}

			first = false;
			firstFilterValue = false;
		}

		query += (firstFilterValue ? '' : ') ');
	}

	return (query.indexOf('where ') == query.length - 6 ? query.replace('where ', '') : query);
}

function executeSearch(searchConfig) {
	var result = [], aspects = [];

	// look for all aspects that should be joined in query based on required parameters
	for ( var i in searchConfig.required) {
		var aspect = searchConfig.required[i].aspect;

		if (aspect && aspects.indexOf(aspect) < 0) {
			aspects.push(aspect);
		}
	}

	// look for all aspects that should be joined in query based on provided optional parameters
	for ( var i in searchConfig.parameters) {
		var aspect = searchConfig.parameters[i].aspect;

		if (aspect && args[i] && aspects.indexOf(aspect) < 0) {
			aspects.push(aspect);
		}
	}

	// look for all aspects that should be joined in query based on filter aspect
	var filterValue = args['filterBy'];
	if (filterValue) {
		for ( var i in searchConfig.filterBy) {
			var aspect = searchConfig.filterBy[i].aspect;

			if (aspect && aspects.indexOf(aspect) < 0) {
				aspects.push(aspect);
			}
		}
	}

	// for each type, execute search and append results to response
	for ( var i in searchConfig.types) {
		var query = buildQuery(searchConfig.types[i], aspects, searchConfig, filterValue);

		logger.warn(query);

		result = result.concat(search.query({
			query : query,
			language : 'cmis-alfresco'
		}));
	}

	return result;
}

function main() {
	// parse configuration and retrieve search configuration data
	var cfg = parseConfig(), searchId = url.templateArgs.search, searchConfig = cfg[searchId];

	// in case provided search ID is not valid, return error
	if (!searchConfig) {
		status.code = 400;
		status.message = 'Unsupported search ID: ' + searchId + '.';
		status.redirect = true;
		return;
	}

	// check that all required search parameters are provided
	for ( var i in searchConfig.required) {
		if (!args[i]) {
			status.code = 400;
			status.message = 'Missing required search parameter: ' + i + '.';
			status.redirect = true;
			return;
		}
	}

	// execute search
	model.results = executeSearch(searchConfig);
	model.fields = searchConfig.fields;
}

main();
