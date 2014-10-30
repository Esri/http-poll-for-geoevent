/*
  Copyright 1995-2014 Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
 */

package com.esri.geoevent.transport.httpPoll;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.transport.TransportContext;
import com.esri.ges.transport.TransportDefinition;
import com.esri.ges.transport.http.HttpInboundTransport;
import com.esri.ges.transport.http.HttpTransportContext;

public class HttpPollInboundTransport extends HttpInboundTransport
{
	private static final BundleLogger	LOGGER	= BundleLoggerFactory.getLogger(HttpPollInboundTransport.class);

	private String										tsparam;
	private String										tsformat;
	private String										tsinit;
	private String										tsvalue;
	private String										params;
	private Date											ts;

	public HttpPollInboundTransport(TransportDefinition definition) throws ComponentException
	{
		super(definition);
	}

	@Override
	public synchronized void start()
	{
		super.start();
	}

	@Override
	public synchronized void stop()
	{
		super.stop();
	}

	@Override
	public synchronized void setup()
	{
		super.setup();
		tsparam = getProperty("lastPollTimestampName").getValueAsString();
		tsinit = getProperty("initialLastTimestamp").getValueAsString();
		tsformat = getProperty("timestampFormat").getValueAsString();
		params = getProperty("clientParameters").getValueAsString();
	}

	@Override
	public void beforeConnect(TransportContext context)
	{
		DateFormat df = null;
		tsvalue = "";

		if (!(context instanceof HttpTransportContext))
			return;

		// Parse user defined initial ts
		try
		{
			df = new SimpleDateFormat(tsformat);
		}
		catch (Exception e)
		{
			df = null;
		}

		if (df != null)
		{
			if (ts == null)
			{
				try
				{
					Date userdefined = df.parse(tsinit);
					if (userdefined == null)
					{
						ts = new Date(0);
						tsvalue = df.format(ts);
					}
					else
					{
						tsvalue = df.format(userdefined);
					}

				}
				catch (ParseException e)
				{
					ts = new Date(0);
					tsvalue = df.format(ts);
				}
			}
			else
			{
				tsvalue = df.format(ts);
			}
			ts = new Date();
		}

		HttpRequest request = ((HttpTransportContext) context).getHttpRequest();
		if (request instanceof HttpPost)
		{
			ArrayList<NameValuePair> postParameters;
			postParameters = new ArrayList<NameValuePair>();
			if (tsvalue.length() > 0)
				postParameters.add(new BasicNameValuePair(tsparam, tsvalue));

			try
			{
				Map<String, String> paramMap = parseParameters(params);
				Iterator<Entry<String, String>> itr = paramMap.entrySet().iterator();
				while (itr.hasNext())
				{
					Entry<String, String> pairs = itr.next();
					postParameters.add(new BasicNameValuePair((String) pairs.getKey(), (String) pairs.getValue()));
					itr.remove();
				}

				if (postParameters.size() > 0)
				{
					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters, StandardCharsets.UTF_8);
					((HttpPost) request).setEntity(entity);
				}
			}
			catch (Exception error)
			{
				LOGGER.error("PARSING_ERROR", error.getMessage());
				LOGGER.info(error.getMessage(), error);
			}
		}
	}

	private Map<String, String> parseParameters(String params) throws UnsupportedEncodingException
	{
		Map<String, String> queryPairs = new LinkedHashMap<String, String>();
		String[] pairs = params.split("&");
		for (String pair : pairs)
		{
			int idx = pair.indexOf("=");
			if (idx > 0)
				queryPairs.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.toString()), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}
		return queryPairs;
	}
}
