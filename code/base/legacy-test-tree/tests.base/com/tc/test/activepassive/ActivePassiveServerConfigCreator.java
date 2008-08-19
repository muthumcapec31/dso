/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tc.test.activepassive;

import org.apache.commons.io.FileUtils;

import com.tc.config.schema.setup.TestTVSConfigurationSetupManagerFactory;
import com.tc.config.schema.test.ApplicationConfigBuilder;
import com.tc.config.schema.test.GroupConfigBuilder;
import com.tc.config.schema.test.GroupsConfigBuilder;
import com.tc.config.schema.test.HaConfigBuilder;
import com.tc.config.schema.test.L2ConfigBuilder;
import com.tc.config.schema.test.L2SConfigBuilder;
import com.tc.config.schema.test.MembersConfigBuilder;
import com.tc.config.schema.test.SystemConfigBuilder;
import com.tc.config.schema.test.TerracottaConfigBuilder;
import com.tc.logging.TCLogger;
import com.tc.logging.TCLogging;
import com.tc.test.TestConfigObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

public class ActivePassiveServerConfigCreator {
  public static final String                            DEV_MODE  = "development";
  public static final String                            PROD_MODE = "production";

  private static TCLogger                               logger    = TCLogging
                                                                      .getTestingLogger(ActivePassiveServerConfigCreator.class);
  private final int                                     serverCount;
  private final int[]                                   dsoPorts;
  private final int[]                                   jmxPorts;
  private final int[]                                   l2GroupPorts;
  private final String[]                                serverNames;
  private final String                                  serverPersistence;
  private final boolean                                 serverDiskless;
  private final String                                  configModel;
  private final File                                    configFile;
  private final File                                    tempDir;
  private final TestTVSConfigurationSetupManagerFactory configFactory;
  private final String[]                                dataLocations;
  private final ActivePassiveTestSetupManager           setupManager;
  private final List[]                                  groups;
  private final String                                  testMode;

  public ActivePassiveServerConfigCreator(ActivePassiveTestSetupManager setupManager, int[] dsoPorts, int[] jmxPorts,
                                          int[] l2GroupPorts, String[] serverNames, List[] groups, String configModel,
                                          File configFile, File tempDir,
                                          TestTVSConfigurationSetupManagerFactory configFactory, String testMode) {
    this.setupManager = setupManager;
    this.groups = groups;
    this.testMode = testMode;
    this.serverCount = this.setupManager.getServerCount();
    this.dsoPorts = dsoPorts;
    this.jmxPorts = jmxPorts;
    this.l2GroupPorts = l2GroupPorts;
    this.serverNames = serverNames;
    this.serverPersistence = this.setupManager.getServerPersistenceMode();
    this.serverDiskless = this.setupManager.isNetworkShare();
    this.configModel = configModel;
    this.configFile = configFile;
    this.tempDir = tempDir;
    this.configFactory = configFactory;
    dataLocations = new String[serverCount];

    checkPersistenceAndDiskLessMode();
  }

  public String getDataLocation(int i) {
    if (i < 1 && i > dataLocations.length) { throw new AssertionError("Invalid index=[" + i + "]... there are ["
                                                                      + dataLocations.length
                                                                      + "] servers involved in this test."); }
    if (serverDiskless) {
      return dataLocations[i];
    } else {
      return dataLocations[0];
    }
  }

  private void checkPersistenceAndDiskLessMode() {
    if (!serverDiskless && serverPersistence.equals(ActivePassivePersistenceMode.TEMPORARY_SWAP_ONLY)) { throw new AssertionError(
                                                                                                                                  "The servers are not running in diskless mode so persistence mode should be set to permanent-store"); }
  }

  private void checkConfigurationModel() {
    if (!configModel.equals(DEV_MODE) && !configModel.equals(PROD_MODE)) { throw new AssertionError(
                                                                                                    "Unknown operating mode."); }
  }

  private void cleanDataDirectory(String dataLocation) throws IOException {
    File dbDir = new File(dataLocation);
    logger.info("DBHome: " + dbDir.getAbsolutePath());
    if (dbDir.exists()) {
      FileUtils.cleanDirectory(dbDir);
    }
  }

  public void writeL2Config() throws Exception {
    checkConfigurationModel();
    SystemConfigBuilder system = SystemConfigBuilder.newMinimalInstance();
    system.setConfigurationModel(configModel);

    String dataLocationHome = tempDir.getAbsolutePath() + File.separator + "server-data";
    cleanDataDirectory(dataLocationHome);
    String logLocationHome = tempDir.getAbsolutePath() + File.separator + "server-logs" + File.separator;

    boolean gcEnabled = configFactory.getGCEnabled();
    boolean gcVerbose = configFactory.getGCVerbose();
    int gcIntervalInSec = configFactory.getGCIntervalInSec();

    L2ConfigBuilder[] l2s = new L2ConfigBuilder[serverCount];
    for (int i = 0; i < serverCount; i++) {
      L2ConfigBuilder l2 = new L2ConfigBuilder();
      // TODO: NOT sure: have to see - need to fix and test this part of code for active-active
      if (this.testMode.equals(TestConfigObject.TRANSPARENT_TESTS_MODE_ACTIVE_ACTIVE)) {
        // if group's ha mode is diskless than different data file for each member
        // if group's ha mode is diskbased than same data file for all members
        int grpIndex = getGroupIndex(i);
        String mode = setupManager.getGroupServerShareDataMode(grpIndex);
        boolean isServerDiskless = !mode.equals(ActivePassiveSharedDataMode.DISK) ? true : false;
        if (isServerDiskless) {
          dataLocations[i] = dataLocationHome + File.separator + "server-" + i;
          l2.setData(dataLocations[i]);
        } else {
          l2.setData(dataLocationHome);
          if (dataLocations[0] == null) {
            dataLocations[0] = dataLocationHome;
          }
        }
      } else if (serverDiskless) {
        dataLocations[i] = dataLocationHome + File.separator + "server-" + i;
        l2.setData(dataLocations[i]);
      } else {
        l2.setData(dataLocationHome);
        if (dataLocations[0] == null) {
          dataLocations[0] = dataLocationHome;
        }
      }
      l2.setLogs(logLocationHome + "server-" + i);
      l2.setName(serverNames[i]);
      l2.setDSOPort(dsoPorts[i]);
      l2.setJMXPort(jmxPorts[i]);
      l2.setL2GroupPort(l2GroupPorts[i]);
      l2.setPersistenceMode(serverPersistence);
      l2.setGCEnabled(gcEnabled);
      l2.setGCVerbose(gcVerbose);
      l2.setGCInterval(gcIntervalInSec);
      l2s[i] = l2;
    }
    HaConfigBuilder ha = new HaConfigBuilder();
    if (this.serverDiskless) {
      ha.setMode(HaConfigBuilder.HA_MODE_NETWORKED_ACTIVE_PASSIVE);
    } else {
      ha.setMode(HaConfigBuilder.HA_MODE_DISK_BASED_ACTIVE_PASSIVE);
    }
    ha.setElectionTime(this.setupManager.getElectionTime() + "");

    L2SConfigBuilder l2sConfigbuilder = new L2SConfigBuilder();
    l2sConfigbuilder.setL2s(l2s);
    l2sConfigbuilder.setHa(ha);

    int indent = 7;
    GroupsConfigBuilder groupsConfigBuilder = new GroupsConfigBuilder();
    for (int i = 0; i < this.groups.length; i++) {
      GroupConfigBuilder group = new GroupConfigBuilder();
      HaConfigBuilder groupHa = new HaConfigBuilder(indent);
      String mode = null;
      if (this.setupManager.getGroupServerShareDataMode(i).equals(ActivePassiveSharedDataMode.DISK)) mode = HaConfigBuilder.HA_MODE_DISK_BASED_ACTIVE_PASSIVE;
      else mode = HaConfigBuilder.HA_MODE_NETWORKED_ACTIVE_PASSIVE;
      groupHa.setMode(mode);
      groupHa.setElectionTime("" + this.setupManager.getGroupElectionTime(i));
      MembersConfigBuilder members = new MembersConfigBuilder();
      for (Iterator iter = groups[i].iterator(); iter.hasNext();) {
        String memberName = (String) iter.next();
        members.addMember(memberName);
      }
      group.setHa(groupHa);
      group.setMembers(members);
      groupsConfigBuilder.addGroupConfigBuilder(group);
    }
    l2sConfigbuilder.setGroups(groupsConfigBuilder);

    ApplicationConfigBuilder app = ApplicationConfigBuilder.newMinimalInstance();

    TerracottaConfigBuilder configBuilder = new TerracottaConfigBuilder();
    configBuilder.setSystem(system);
    configBuilder.setServers(l2sConfigbuilder);
    configBuilder.setApplication(app);

    String configAsString = configBuilder.toString();
    System.err.println("Writing config to file:" + configFile.getAbsolutePath() + configAsString);

    FileOutputStream fileOutputStream = new FileOutputStream(configFile);
    PrintWriter out = new PrintWriter((fileOutputStream));
    out.println(configAsString);
    out.flush();
    out.close();
  }

  private int getGroupIndex(int serverIndex) {
    int members = 0;
    for (int i = 0; i < setupManager.getActiveServerGroupCount(); i++) {
      members += setupManager.getGroupMemberCount(i);
      if (serverIndex < members) return i;
    }
    return setupManager.getActiveServerGroupCount() - 1;
  }
}
