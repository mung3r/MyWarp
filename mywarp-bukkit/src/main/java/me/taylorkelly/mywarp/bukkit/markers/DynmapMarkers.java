/*
 * Copyright (C) 2011 - 2016, MyWarp team and contributors
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

package me.taylorkelly.mywarp.bukkit.markers;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import me.taylorkelly.mywarp.Game;
import me.taylorkelly.mywarp.bukkit.BukkitSettings;
import me.taylorkelly.mywarp.bukkit.MyWarpPlugin;
import me.taylorkelly.mywarp.util.MyWarpLogger;
import me.taylorkelly.mywarp.util.WarpUtils;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;
import me.taylorkelly.mywarp.warp.event.WarpAdditionEvent;
import me.taylorkelly.mywarp.warp.event.WarpDeletionEvent;
import me.taylorkelly.mywarp.warp.event.WarpUpdateEvent;

import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.slf4j.Logger;

/**
 * Sets location-markers for public warps on the map provided by Dynmap.
 */
public class DynmapMarkers {

  private static final String LABEL_ID = "mywarp.warps";
  private static final String MARKER_ID_PREFIX = "mywarp.warp.";
  private static final String ICON_ID = "mywarp_warp-32";

  private static final Logger log = MyWarpLogger.getLogger(DynmapMarkers.class);
  private static final DynamicMessages msg = new DynamicMessages("me.taylorkelly.mywarp.lang.DynmapMarkers");

  private final BukkitSettings settings;
  private final Game game;

  private MarkerIcon markerIcon;
  private MarkerSet markerSet;

  /**
   * Initializes this instance with the given Dynmap plugin.
   *  @param plugin       the running plugin instance
   * @param dynmapPlugin the running Dynmap instance to use
   * @param manager      the WarpManager whose warps are shown on Dynmap
   * @param eventBus     the EventBus that fires the events that initiate marker changes
   * @param game
   */
  public DynmapMarkers(MyWarpPlugin plugin, DynmapCommonAPI dynmapPlugin, WarpManager manager, EventBus eventBus,
                       Game game) {
    this.game = game;
    this.settings = plugin.getSettings();

    MarkerAPI markerApi = dynmapPlugin.getMarkerAPI();

    // get Icon for all markers
    markerIcon = markerApi.getMarkerIcon(settings.getDynmapMarkerIconId());
    if (markerIcon == null && !settings.getDynmapMarkerIconId().equals(ICON_ID)) {
      log.warn("MarkerIcon '{}' does not exist. Using the default one.", settings.getDynmapMarkerIconId());
      markerIcon = markerApi.getMarkerIcon(ICON_ID);
    }
    if (markerIcon == null) {
      markerIcon = markerApi.createMarkerIcon(ICON_ID, "Warp", plugin.getResource("mywarp_warp-32.png"));
    }

    // create the label
    markerSet = markerApi.getMarkerSet(LABEL_ID);
    if (markerSet == null) {
      markerSet = markerApi.createMarkerSet(LABEL_ID, settings.getDynmapLayerDisplayName(), null, false);
    } else {
      markerSet.setMarkerSetLabel(settings.getDynmapLayerDisplayName());
    }
    markerSet.setLayerPriority(settings.getDynmapLayerPriority());
    markerSet.setHideByDefault(settings.isDynmapLayerHiddenByDefault());
    markerSet.setLabelShow(settings.isDynmapMarkerShowLable());
    markerSet.setMinZoom(settings.getDynmapMarkerMinZoom());

    // add all public warps
    for (Warp warp : manager.filter(WarpUtils.isType(Warp.Type.PUBLIC))) {
      addMarker(warp);
    }
    eventBus.register(this);
  }

  /**
   * Called when a Warp is added.
   *
   * @param event the event
   */
  @Subscribe
  public void onWarpAddition(WarpAdditionEvent event) {
    addMarker(event.getWarp());
  }

  /**
   * Called when a Warp is removed.
   *
   * @param event the event
   */
  @Subscribe
  public void onWarpDeletion(WarpDeletionEvent event) {
    removeMarker(event.getWarp());
  }

  /**
   * Called when an existing Warp is updated in some way.
   *
   * @param event the event
   */
  @Subscribe
  public void onWarpUpdate(WarpUpdateEvent event) {
    switch (event.getType()) {
      case CREATOR:
      case VISITS:
        updateLabel(event.getWarp());
        break;
      case LOCATION:
        updateLocation(event.getWarp());
        break;
      case TYPE:
        Warp warp = event.getWarp();
        switch (warp.getType()) {
          case PRIVATE:
            removeMarker(warp);
            break;
          case PUBLIC:
            addMarker(warp);
            break;
        }
        break;
      default:
        break;
    }
  }

  /**
   * Gets the label for the given Warp.
   *
   * @param warp the Warp
   * @return the label for this Warp
   * @see WarpUtils#replaceTokens(String, Warp)
   */
  private String toLabelHtml(Warp warp) {
    String rawLabel = msg.getString("marker", settings.getLocalizationDefaultLocale());
    return WarpUtils.replaceTokens(rawLabel, warp);

  }

  /**
   * Gets the ID for the marker of the given warp.
   *
   * @param warp the warp
   * @return the unique ID of the marker for the given warp
   */
  private String toMarkerId(Warp warp) {
    return MARKER_ID_PREFIX + warp.getName();
  }

  /**
   * Adds a marker for the given Warp.
   *
   * @param warp the Warp
   */
  private void addMarker(Warp warp) {
    if (!warp.isType(Warp.Type.PUBLIC)) {
      return;
    }
    markerSet.createMarker(toMarkerId(warp), toLabelHtml(warp), true, warp.getWorld(game).getName(),
                           warp.getPosition().getX(), warp.getPosition().getY(), warp.getPosition().getZ(), markerIcon,
                           false);
  }

  /**
   * Removes the given Warp's marker.
   *
   * @param warp the warp
   */
  private void removeMarker(Warp warp) {
    Marker marker = markerSet.findMarker(toMarkerId(warp));
    if (marker != null) {
      marker.deleteMarker();
    }
  }

  /**
   * Updates the label of the given Warp's marker.
   *
   * @param warp the Warp
   */
  private void updateLabel(Warp warp) {
    if (!warp.isType(Warp.Type.PUBLIC)) {
      return;
    }
    Marker marker = markerSet.findMarker(toMarkerId(warp));
    if (marker != null) {
      marker.setLabel(toLabelHtml(warp), true);
    }
  }

  /**
   * Updates the location of the given Warp's marker.
   *
   * @param warp the Warp
   */
  private void updateLocation(Warp warp) {
    if (!warp.isType(Warp.Type.PUBLIC)) {
      return;
    }
    Marker marker = markerSet.findMarker(toMarkerId(warp));
    if (marker != null) {
      marker.setLocation(warp.getWorld(game).getName(), warp.getPosition().getX(), warp.getPosition().getY(),
                         warp.getPosition().getZ());
    }
  }
}
