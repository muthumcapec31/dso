package com.tc.objectserver.persistence.gb;

import com.tc.gbapi.GBMap;
import com.tc.gbapi.GBMapConfig;
import com.tc.gbapi.impl.GBOnHeapMapConfig;
import com.tc.object.ObjectID;
import com.tc.objectserver.core.api.ManagedObject;
import com.tc.objectserver.managedobject.ManagedObjectSerializer;
import com.tc.objectserver.managedobject.ManagedObjectStateSerializer;
import com.tc.objectserver.persistence.api.ManagedObjectPersistor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Set;

/**
 * @author tim
 */
class GBObjectMap implements GBMap<ObjectID, ManagedObject> {
  private final GBMap<Long, byte[]> backingMap;
  private final ManagedObjectSerializer serializer;

  GBObjectMap(ManagedObjectPersistor persistor, final GBMap<Long, byte[]> backingMap) {
    this.backingMap = backingMap;
    this.serializer = new ManagedObjectSerializer(new ManagedObjectStateSerializer(), persistor);
  }

  public static GBMapConfig<Long, byte[]> getConfig() {
    return new GBOnHeapMapConfig<Long, byte[]>(Long.class, byte[].class);
  }

  @Override
  public Set<ObjectID> keySet() {
    throw new UnsupportedOperationException("Implement me!");
  }

  @Override
  public Collection<ManagedObject> values() {
    throw new UnsupportedOperationException("Implement me!");
  }

  @Override
  public long size() {
    return backingMap.size();
  }

  @Override
  public void put(final ObjectID key, final ManagedObject value) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try {
      ObjectOutput oo = new ObjectOutputStream(byteArrayOutputStream);
      try {
        serializer.serializeTo(value, oo);
      } finally {
        oo.close();
      }
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    backingMap.put(key.toLong(), byteArrayOutputStream.toByteArray());
  }

  @Override
  public ManagedObject get(final ObjectID key) {
    byte[] data = backingMap.get(key.toLong());
    if (data == null) {
      return null;
    }
    try {
      ObjectInput oi = new ObjectInputStream(new ByteArrayInputStream(data));
      return (ManagedObject) serializer.deserializeFrom(oi);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public boolean remove(final ObjectID key) {
    return backingMap.remove(key.toLong());
  }

  @Override
  public void removeAll(final Collection<ObjectID> keys) {
    for (ObjectID key : keys) {
      remove(key);
    }
  }

  @Override
  public boolean containsKey(final ObjectID key) {
    return backingMap.containsKey(key.toLong());
  }

  @Override
  public void clear() {
    backingMap.clear();
  }
}