package com.tc.license;

import org.terracotta.license.License;
import org.terracotta.license.LicenseException;

public interface LicenseUsageManager {

  public enum LicenseServerState {
    UNINITIALIZED, ACTIVE, PASSIVE
  }

  public void registerNode(String jvmId, String machineName, String checksum) throws LicenseException;

  public void unregisterNode(String jvmId) throws LicenseException;

  public boolean allocateL1BigMemory(String clientUUID, String fullyQualifiedCacheName, long memoryInBytes)
      throws LicenseException;

  public void releaseL1BigMemory(String clientUUID, String fullyQualifiedCacheName) throws LicenseException;

  public void allocateL2BigMemory(String serverUUID, long memoryInBytes) throws LicenseException;

  public void releaseL2BigMemory(String serverUUID) throws LicenseException;

  public void l1Joined(String clientUUID) throws LicenseException;

  public void l1Removed(String clientUUID) throws LicenseException;

  public boolean verifyCapability(String capability) throws LicenseException;

  public boolean reloadLicense(String license) throws LicenseException;

  public License getLicense();

  public LicenseServerState getState();
}
