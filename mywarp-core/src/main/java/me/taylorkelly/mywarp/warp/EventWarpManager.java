/*
 * Copyright (C) 2011 - 2015, MyWarp team and contributors
 *
 * This file is part of MyWarp.
 *
 * MyWarp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyWarp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyWarp. If not, see <http://www.gnu.org/licenses/>.
 */

package me.taylorkelly.mywarp.warp;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.eventbus.EventBus;

import me.taylorkelly.mywarp.warp.event.WarpAdditionEvent;
import me.taylorkelly.mywarp.warp.event.WarpEvent;
import me.taylorkelly.mywarp.warp.event.WarpRemovalEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides an event-framework for the Warps managed by it. Warps can fire {@link WarpEvent}s through this manager and
 * the manager itself will fire {@link WarpAdditionEvent}s and {@link WarpRemovalEvent}s when Warps are added to or
 * removed from it. Handlers can listen to these events by registering themselves at this manager and providing the
 * appropriate handler methods annotated by {@link com.google.common.eventbus.Subscribe} annotations.<p> This manager
 * fires all events in the thread it runs. Events cannot be expected to be threadsafe and it lies in the responsibility
 * of the handler to manage further thread-safety if required. </p>
 *
 * @see com.google.common.eventbus.EventBus
 */
public class EventWarpManager extends AbstractWarpManager {

  /**
   * Stores all Warps managed by this manager under their name.
   */
  private final Map<String, Warp> warpMap = new HashMap<String, Warp>();

  /**
   * Seeds Events to registered Handlers.
   */
  private final EventBus eventBus = new EventBus();

  /**
   * Initializes this manager.
   *
   * @param handlers the initial handlers
   */
  public EventWarpManager(Object... handlers) {
    for (Object handler : handlers) {
      registerHandler(handler);
    }
  }

  /**
   * Registers the given Object as a handler for events fired by this WarpManager and the Warps managed by it.
   *
   * @param handler the handler
   */
  public void registerHandler(Object handler) {
    eventBus.register(handler);
  }

  /**
   * Unregisters the given Object as a handler for events fired by this WarpManager and the Warps managed by it.
   *
   * @param handler the handler
   */
  public void unregisterHandler(Object handler) {
    eventBus.unregister(handler);
  }

  /**
   * Posts the given WarpEvent to all registered handlers.
   *
   * @param event the WarpEvent
   */
  protected void postEvent(WarpEvent event) {
    eventBus.post(event);
  }

  @Override
  public void add(Warp warp) {
    warp = new EventfullWarp(warp, this);
    warpMap.put(warp.getName(), warp);
    postEvent(new WarpAdditionEvent(warp));
  }

  @Override
  public void remove(Warp warp) {
    warpMap.remove(warp.getName());
    postEvent(new WarpRemovalEvent(warp));
  }

  /**
   * Populates this manager with the given Warps. Unlike {@link #add(Warp)} this method will not fire {@link
   * WarpAdditionEvent}s for addition of the given Warps.
   *
   * @param warps the Warps
   */
  public void populate(Iterable<Warp> warps) {
    for (Warp warp : warps) {
      warp = new EventfullWarp(warp, this);
      warpMap.put(warp.getName(), warp);
    }
  }

  /**
   * Clears this manager, removing all Warps previously managed by it. Unlike {@link #remove(Warp)} this method will not
   * fire {@link WarpRemovalEvent}s for the removal of the Warps.
   */
  public void clear() {
    warpMap.clear();
  }

  @Override
  public int getSize() {
    return warpMap.size();
  }

  @Override
  public boolean contains(String name) {
    return warpMap.containsKey(name);
  }

  @Override
  public Optional<Warp> get(String name) {
    return Optional.fromNullable(warpMap.get(name));
  }

  @Override
  public Collection<Warp> filter(Predicate<Warp> predicate) {
    return Collections2.filter(warpMap.values(), predicate);
  }
}
