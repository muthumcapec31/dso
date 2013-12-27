/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package com.tc.management.remote.connect;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.tc.management.remote.protocol.terracotta.ClientTunnelingEventHandler;
import com.tc.management.remote.protocol.terracotta.JMXConnectStateMachine;
import com.tc.management.remote.protocol.terracotta.L1ConnectionMessage;
import com.tc.management.remote.protocol.terracotta.L1ConnectionMessage.Connecting;
import com.tc.management.remote.protocol.terracotta.L1ConnectionMessage.Disconnecting;
import com.tc.net.TCSocketAddress;
import com.tc.net.protocol.tcm.ChannelID;
import com.tc.net.protocol.tcm.MessageChannel;
import com.tc.statistics.StatisticsGateway;

import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import junit.framework.Assert;

public class ClientConnectEventHandlerTest {

  @Mock
  private MessageChannel                                       channel;
  @Mock
  private MBeanServer                                          mbs;
  @Mock
  private JMXConnector                                         jmxConnector;
  @Mock
  private ClientBeanBag                                        clientBeanBag;
  @Mock
  private StatisticsGateway                                    statisticsGateway;

  private ClientConnectEventHandlerforTest                     clientConnectEventHandler;

  private final class ClientConnectEventHandlerforTest extends ClientConnectEventHandler {
    private ClientConnectEventHandlerforTest(final StatisticsGateway statisticsGateway) {
      super(statisticsGateway);
    }

    @Override
    protected JMXConnector getJmxConnector(JMXServiceURL serviceURL, Map environment) {
      return jmxConnector;
    }

    @Override
    protected ClientBeanBag createClientBeanBag(final L1ConnectionMessage msg, final MessageChannel channel1,
                                                final MBeanServer l2MBeanServer,
                                                final MBeanServerConnection l1MBeanServerConnection) {
      return clientBeanBag;
    }
    int getClientBeanBagsMapSize() {
      return clientBeanBags.size();
    }
  }

  @Before
  public void setUp() throws Throwable {
    MockitoAnnotations.initMocks(this);
    JMXConnectStateMachine state = new JMXConnectStateMachine();
    when(channel.getAttachment(ClientTunnelingEventHandler.STATE_ATTACHMENT)).thenReturn(state);
    when(channel.getRemoteAddress()).thenReturn(new TCSocketAddress(59899));
    when(channel.getChannelID()).thenReturn(new ChannelID(1234567890L));
    when(jmxConnector.getMBeanServerConnection()).thenReturn(null);
    when(clientBeanBag.updateRegisteredBeans()).thenReturn(true);
    Mockito
        .doNothing()
        .when(jmxConnector)
        .addConnectionNotificationListener((NotificationListener) Matchers.any(), (NotificationFilter) Matchers.any(),
                                           Matchers.any());
    clientConnectEventHandler = new ClientConnectEventHandlerforTest(statisticsGateway);
  }

  @Test
  public void testEventHandleWithDisconnecting() throws Throwable {

    Connecting context = new Connecting(mbs, channel, null, null);
    clientConnectEventHandler.handleEvent(context);
    Assert.assertEquals(1, clientConnectEventHandler.getClientBeanBagsMapSize());
    Disconnecting context2 = new Disconnecting(channel);
    clientConnectEventHandler.handleEvent(context2);
    Assert.assertEquals(0, clientConnectEventHandler.getClientBeanBagsMapSize());
  }

}
