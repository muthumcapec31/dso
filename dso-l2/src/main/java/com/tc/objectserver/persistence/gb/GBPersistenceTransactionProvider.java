package com.tc.objectserver.persistence.gb;

import com.tc.gbapi.GBManager;
import com.tc.objectserver.storage.api.PersistenceTransaction;
import com.tc.objectserver.storage.api.PersistenceTransactionProvider;

/**
 * @author tim
 */
public class GBPersistenceTransactionProvider implements PersistenceTransactionProvider {

  private final GBManager manager;

  public GBPersistenceTransactionProvider(GBManager manager) {
    this.manager = manager;
  }

  @Override
  public PersistenceTransaction newTransaction() {
    return new GBTransaction();
  }


  private class GBTransaction implements PersistenceTransaction {
    private final Thread t;

    private GBTransaction() {
      this.t = Thread.currentThread();
      manager.begin();
    }

    @Override
    public Object getTransaction() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void commit() {
      if (Thread.currentThread() != t) {
        throw new IllegalStateException("Begin and commit threads don't match.");
      }
      manager.commit();
    }

    @Override
    public void abort() {
      throw new UnsupportedOperationException();
    }
  }
}