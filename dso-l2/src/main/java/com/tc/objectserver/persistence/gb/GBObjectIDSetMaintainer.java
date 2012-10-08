package com.tc.objectserver.persistence.gb;

import com.tc.gbapi.GBMapMutationListener;
import com.tc.gbapi.GBRetriever;
import com.tc.object.ObjectID;
import com.tc.objectserver.persistence.api.PersistentCollectionsUtil;
import com.tc.util.ObjectIDSet;

import java.util.Map;

/**
 * @author tim
 */
public class GBObjectIDSetMaintainer implements GBMapMutationListener<Long, byte[]> {

  private final ObjectIDSet extantObjectIDSet = new ObjectIDSet();
  private final ObjectIDSet evictableObjectIDSet = new ObjectIDSet();
  private final ObjectIDSet mapObjectIDSet = new ObjectIDSet();

  enum ObjectMetadataEnum {
    TYPE
  }

  public ObjectIDSet objectIDSnapshot() {
    return new ObjectIDSet(extantObjectIDSet);
  }

  public ObjectIDSet evictableObjectIDSetSnapshot() {
    return new ObjectIDSet(evictableObjectIDSet);
  }

  public ObjectIDSet mapObjectIDSetSnapshot() {
    return new ObjectIDSet(mapObjectIDSet);
  }

  @Override
  public void added(GBRetriever<Long> key, GBRetriever<byte[]> value, Map<? extends Enum, Object> metadata) {
    byte type  = value.retrieve()[8]; // TODO: Make this less hard coded
    ObjectID k = new ObjectID(key.retrieve());
    if (PersistentCollectionsUtil.isEvictableMapType(type)) {
      evictableObjectIDSet.add(k);
    }
    if (PersistentCollectionsUtil.isPersistableCollectionType(type)) {
      mapObjectIDSet.add(k);
    }
    extantObjectIDSet.add(k);
  }

  @Override
  public void removed(GBRetriever<Long> key, GBRetriever<byte[]> value, Map<? extends Enum, Object> metadata) {
    byte type  = value.retrieve()[8];
    ObjectID k = new ObjectID(key.retrieve());
    if (PersistentCollectionsUtil.isEvictableMapType(type)) {
      evictableObjectIDSet.remove(k);
    }
    if (PersistentCollectionsUtil.isPersistableCollectionType(type)) {
      mapObjectIDSet.remove(k);
    }
    extantObjectIDSet.remove(k);
  }

}