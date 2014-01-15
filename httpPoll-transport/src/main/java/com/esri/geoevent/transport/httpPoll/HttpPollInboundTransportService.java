package com.esri.geoevent.transport.httpPoll;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.transport.Transport;
import com.esri.ges.transport.http.HttpInboundTransportService;
import com.esri.ges.transport.util.XmlTransportDefinition;

public class HttpPollInboundTransportService extends HttpInboundTransportService
{

  public HttpPollInboundTransportService()
  {
    super();
    definition = new XmlTransportDefinition(getResourceAsStream("httpPoll-inbound-transport-definition.xml"),
        super.definition);
  }

  @Override
  public Transport createTransport() throws ComponentException
  {
    return new HttpPollInboundTransport(definition);
  }
}
