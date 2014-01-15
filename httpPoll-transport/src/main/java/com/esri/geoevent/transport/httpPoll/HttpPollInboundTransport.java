package com.esri.geoevent.transport.httpPoll;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.transport.TransportContext;
import com.esri.ges.transport.TransportDefinition;
import com.esri.ges.transport.http.HttpInboundTransport;
import com.esri.ges.transport.http.HttpTransportContext;

public class HttpPollInboundTransport extends HttpInboundTransport
{
  static final private Log log = LogFactory.getLog(HttpPollInboundTransport.class);
  private String tsparam;
  private String tsformat;
  private String tsinit;
  private String tsvalue;
  private String params;
  private Date ts;
  
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
    
    if(! (context instanceof HttpTransportContext))
      return;
    
    // Parse user defined initial ts
    try
    {
      df = new SimpleDateFormat(tsformat);
    }
    catch(Exception e)
    {
      df = null;
    }
    
    if(df != null)
    {
      if(ts == null)
      {
        try
        {
          Date userdefined = df.parse(tsinit);
          if(userdefined == null)
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
    
    HttpRequest request = ((HttpTransportContext)context).getHttpRequest();
    if(request instanceof HttpPost)
    {
      ArrayList<NameValuePair> postParameters;
      postParameters = new ArrayList<NameValuePair>();
      if(tsvalue.length()>0)
        postParameters.add(new BasicNameValuePair(tsparam, tsvalue));

      try
      {
        Map<String, String> paramMap = parseParameters(params);
        Iterator<Entry<String, String>> it = paramMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            postParameters.add(new BasicNameValuePair((String)pairs.getKey(), (String)pairs.getValue()));
            it.remove();
        }
        
        if(postParameters.size()>0)
        {
          UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters, "utf-8");
          ((HttpPost)request).setEntity(entity);
        }
      }
      catch (UnsupportedEncodingException e)
      {
        log.error(e);
      }
      catch (Exception e)
      {
        log.error(e);
      }
    }
  }
  
  private Map<String, String> parseParameters(String params) throws UnsupportedEncodingException
  {
    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
    String[] pairs = params.split("&");
    for (String pair : pairs) {
        int idx = pair.indexOf("=");
        if(idx>0)
          query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
    }
    return query_pairs;
  }
}
