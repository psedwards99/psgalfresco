package com.puntersouthall.remote;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.*;

import java.util.HashMap;
import java.util.Map;

public class ExternalResourcesWebScript extends DeclarativeWebScript {

	private static Logger logger = LoggerFactory.getLogger(ExternalResourcesWebScript.class);

	private String externalUrlBase;

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

		logger.info("ExternalResourcesWebScript invocation...");
		String externalServiceURL = externalUrlBase + StringUtils.substringAfter(req.getURL(), "/external/");

		try {
			externalServiceURL = URIUtil.encodeQuery(externalServiceURL);
		} catch (URIException e) {
			e.printStackTrace();
			throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
					String.format("Error encoding URI \"%s\".", externalServiceURL));
		}

		logger.info(String.format("Calling external service to retireve data: %s", externalServiceURL));

		HttpClient httpClient = new HttpClient();
		HttpMethod method = new GetMethod(externalServiceURL);

		String response = null;
		int responseCode = -1;
		try {

			// Calling external service
			responseCode = httpClient.executeMethod(method);

			if (responseCode == Status.STATUS_OK) {
				response = method.getResponseBodyAsString();

				if (logger.isDebugEnabled()) {
					logger.debug("External service body: ", response);
				}
			}
		} catch (Exception e) {
			int responseStatusCode = method.getStatusLine().getStatusCode();
			String responseStatusPhrase = method.getStatusLine().getReasonPhrase();

			String.format("Request failed with status code: %1$s, details: %3$s", responseStatusCode,
					responseStatusPhrase);
			e.printStackTrace();
			throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, e.getMessage());
		}

		if (responseCode != Status.STATUS_OK) {
			throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
					String.format("The response code of external service call was %s.", responseCode));
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("json", response);
		return model;
	}

	public String getExternalUrlBase() {
		return externalUrlBase;
	}

	public void setExternalUrlBase(String externalUrlBase) {
		this.externalUrlBase = externalUrlBase;
	}

}