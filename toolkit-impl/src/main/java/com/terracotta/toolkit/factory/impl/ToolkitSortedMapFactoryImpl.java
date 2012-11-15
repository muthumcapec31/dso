/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package com.terracotta.toolkit.factory.impl;

import org.terracotta.toolkit.ToolkitObjectType;
import org.terracotta.toolkit.collections.ToolkitSortedMap;
import org.terracotta.toolkit.config.Configuration;
import org.terracotta.toolkit.internal.ToolkitInternal;

import com.terracotta.toolkit.collections.DestroyableToolkitSortedMap;
import com.terracotta.toolkit.collections.map.ToolkitSortedMapImpl;
import com.terracotta.toolkit.factory.ToolkitFactoryInitializationContext;
import com.terracotta.toolkit.factory.ToolkitObjectFactory;
import com.terracotta.toolkit.roots.impl.ToolkitTypeConstants;
import com.terracotta.toolkit.type.IsolatedToolkitTypeFactory;

public class ToolkitSortedMapFactoryImpl extends
    AbstractPrimaryToolkitObjectFactory<ToolkitSortedMap, ToolkitSortedMapImpl> {

  private static final SortedMapIsolatedTypeFactory FACTORY = new SortedMapIsolatedTypeFactory();

  public ToolkitSortedMapFactoryImpl(ToolkitInternal toolkit, ToolkitFactoryInitializationContext context) {
    super(toolkit, context.getToolkitTypeRootsFactory()
        .createAggregateIsolatedTypeRoot(ToolkitTypeConstants.TOOLKIT_SORTED_MAP_ROOT_NAME, FACTORY,
                                         context.getPlatformService()));
  }

  @Override
  public ToolkitObjectType getManufacturedToolkitObjectType() {
    return ToolkitObjectType.SORTED_MAP;
  }

  private static class SortedMapIsolatedTypeFactory implements
      IsolatedToolkitTypeFactory<ToolkitSortedMap, ToolkitSortedMapImpl> {

    @Override
    public ToolkitSortedMap createIsolatedToolkitType(ToolkitObjectFactory<ToolkitSortedMap> factory, String name,
                                                      Configuration config, ToolkitSortedMapImpl tcClusteredObject) {
      return new DestroyableToolkitSortedMap(factory, tcClusteredObject, name);
    }

    @Override
    public ToolkitSortedMapImpl createTCClusteredObject(Configuration config) {
      return new ToolkitSortedMapImpl();
    }

  }

}
