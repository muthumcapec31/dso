/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package com.tc.objectserver.api;

/**
 *
 * @author mscott
 */
public interface Transaction {
    void commit();
    void abort();
    void addTransactionListener(TransactionListener l);
}
