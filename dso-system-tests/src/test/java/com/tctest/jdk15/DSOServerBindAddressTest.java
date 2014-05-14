/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tctest.jdk15;

import org.terracotta.test.util.WaitUtil;

import com.tc.config.schema.setup.ConfigurationSetupException;
import com.tc.config.schema.setup.L2ConfigurationSetupManager;
import com.tc.config.schema.setup.TestConfigurationSetupManagerFactory;
import com.tc.exception.TCRuntimeException;
import com.tc.lang.StartupHelper;
import com.tc.lang.StartupHelper.StartupAction;
import com.tc.lang.TCThreadGroup;
import com.tc.lang.ThrowableHandlerImpl;
import com.tc.logging.TCLogging;
import com.tc.net.protocol.transport.NullConnectionPolicy;
import com.tc.object.BaseDSOTestCase;
import com.tc.objectserver.impl.DistributedObjectServer;
import com.tc.objectserver.managedobject.ManagedObjectStateFactory;
import com.tc.objectserver.mgmt.ObjectStatsRecorder;
import com.tc.properties.TCPropertiesConsts;
import com.tc.properties.TCPropertiesImpl;
import com.tc.server.NullTCServerInfo;
import com.tc.util.Assert;
import com.tc.util.PortChooser;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

/**
 * Test for DEV-1060
 *
 * @author Manoj
 */
public class DSOServerBindAddressTest extends BaseDSOTestCase {
  private final TCThreadGroup     group     = new TCThreadGroup(
                                                                new ThrowableHandlerImpl(TCLogging
                                                                    .getLogger(DistributedObjectServer.class)));
  private static final String[]   bindAddrs = { "0.0.0.0", "127.0.0.1", localAddr() };
  private DistributedObjectServer server;

  static String localAddr() {
    try {
      String rv = InetAddress.getLocalHost().getHostAddress();
      if (rv.startsWith("127.")) { throw new RuntimeException("Wrong local address " + rv); }
      return rv;
    } catch (UnknownHostException uhe) {
      throw new TCRuntimeException("Host resolve error:" + uhe);
    }
  }

  private class StartAction implements StartupAction {
    private final int    tsaPort;
    private final int    jmxPort;
    private final String bindAddr;
    private final int    tsaGroupPort;

    public StartAction(String bindAddr, int tsaPort, int jmxPort, int tsaGroupPort) {
      this.bindAddr = bindAddr;
      this.tsaPort = tsaPort;
      this.jmxPort = jmxPort;
      this.tsaGroupPort = tsaGroupPort;
    }

    @Override
    public void execute() throws Throwable {
      TCPropertiesImpl.getProperties().setProperty(TCPropertiesConsts.L2_OBJECTMANAGER_DGC_INLINE_ENABLED, "false");
      server = new DistributedObjectServer(createL2Manager(bindAddr, tsaPort, jmxPort, tsaGroupPort), group,
                                           new NullConnectionPolicy(), new NullTCServerInfo(),
                                           new ObjectStatsRecorder());
      server.start();
    }

  }

  public void testDSOServerAndJMXBindAddress() throws Exception {
    PortChooser pc = new PortChooser();

    ManagedObjectStateFactory.disableSingleton(true);

    for (int i = 0; i < bindAddrs.length; i++) {
      String bind = bindAddrs[i];
      int tsaPort = pc.chooseRandomPort();
      int jmxPort = pc.chooseRandomPort();
      int tsaGroupPort = pc.chooseRandomPort();

      new StartupHelper(group, new StartAction(bind, tsaPort, jmxPort, tsaGroupPort)).startUp();

      final DistributedObjectServer dsoServer = server;
      WaitUtil.waitUntilCallableReturnsTrue(new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          try {
            dsoServer.getListenAddr();
            return true;
          } catch (IllegalStateException ise) {
            //
          }
          return false;
        }
      });

      if (i == 0) {
        Assert.eval(server.getListenAddr().isAnyLocalAddress());
      } else {
        assertEquals(server.getListenAddr().getHostAddress(), bind);
      }
      Assert.assertNotNull(server.getJMXConnServer());
      assertEquals(server.getJMXConnServer().getAddress().getHost(), bind);

      testSocketConnect(bind, new int[] { tsaPort, jmxPort, tsaGroupPort }, true);

      server.stop();
      Thread.sleep(3000);
    }
  }

  private void testSocketConnect(String host, int[] ports, boolean testNegative) throws IOException {
    InetAddress addr = InetAddress.getByName(host);
    if (addr.isAnyLocalAddress()) {
      // should be able to connect on both localhost and local IP
      testSocketConnect("127.0.0.1", ports, false);
      testSocketConnect(localAddr(), ports, false);
    } else {
      // positive case
      for (int port : ports) {
        testSocket(host, port, false);
      }

      if (testNegative) {
        // negative case
        for (int port : ports) {
          if (addr.isLoopbackAddress()) {
            testSocket(localAddr(), port, true);
          } else if (InetAddress.getByName(localAddr()).equals(addr)) {
            testSocket("127.0.0.1", port, true);
          } else {
            throw new AssertionError(addr);
          }
        }
      }
    }
  }

  private static void testSocket(String host, int port, boolean expectFailure) throws IOException {
    System.err.print("testing connect on " + host + ":" + port + " ");
    Socket s = null;
    try {
      s = new Socket(host, port);
      if (expectFailure) {
        System.err.println("[FAIL]");
        throw new AssertionError("should not connect");
      }
    } catch (IOException ioe) {
      if (!expectFailure) {
        System.err.println("[FAIL]");
        throw ioe;
      }
    } finally {
      closeQuietly(s);
    }

    System.err.println("[OK]");
  }

  private static void closeQuietly(Socket s) {
    if (s == null) return;
    try {
      s.close();
    } catch (IOException ioe) {
      // ignore
    }
  }

  public L2ConfigurationSetupManager createL2Manager(String bindAddress, int tsaPort, int jmxPort, int tsaGroupPort)
      throws ConfigurationSetupException {
    TestConfigurationSetupManagerFactory factory = super.configFactory();
    L2ConfigurationSetupManager manager = factory.createL2TVSConfigurationSetupManager(null);
    manager.dsoL2Config().tsaPort().setIntValue(tsaPort);
    manager.dsoL2Config().tsaPort().setBind(bindAddress);

    manager.commonl2Config().jmxPort().setIntValue(jmxPort);
    manager.commonl2Config().jmxPort().setBind(bindAddress);

    manager.dsoL2Config().tsaGroupPort().setIntValue(tsaGroupPort);
    manager.dsoL2Config().tsaGroupPort().setBind(bindAddress);

    return manager;
  }
}
