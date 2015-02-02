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

package me.taylorkelly.mywarp.bukkit.markers;

import com.google.common.eventbus.Subscribe;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.bukkit.MyWarpPlugin;
import me.taylorkelly.mywarp.util.WarpUtils;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.EventWarpManager;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.event.WarpAdditionEvent;
import me.taylorkelly.mywarp.warp.event.WarpRemovalEvent;
import me.taylorkelly.mywarp.warp.event.WarpUpdateEvent;

import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.util.logging.Logger;

/**
 * Sets location-markers for public warps on the map provided by Dynmap.
 */
public class DynmapMarkers {

  private static final String LABEL_ID = "mywarp.warps"; // NON-NLS
  private static final String MARKER_ID_PREFIX = "mywarp.warp."; // NON-NLS
  private static final String ICON_ID = "mywarp_warp-32"; // NON-NLS

  private static final Logger log = Logger.getLogger(DynmapMarkers.class.getName());
  private static final DynamicMessages MESSAGES = new DynamicMessages(
      "me.taylorkelly.mywarp.lang.DynmapMarkers"); // NON-NLS

  private MarkerIcon markerIcon;
  private MarkerSet markerSet;

  /**
   * Initializes this instance with the given Dynmap plugin.
   *
   * @param plugin       the running plugin instance
   * @param dynmapPlugin the running Dynmap instance to use
   * @param manager      the EventWarpManager this Markers run on
   */
  public DynmapMarkers(MyWarpPlugin plugin, DynmapCommonAPI dynmapPlugin,
                       EventWarpManager manager) {
    MarkerAPI markerApi = dynmapPlugin.getMarkerAPI();

    // get Icon for all markers
    markerIcon =
        markerApi.getMarkerIcon(MyWarp.getInstance().getSettings().getDynmapMarkerIconID());
    if (markerIcon == null && !MyWarp.getInstance().getSettings().getDynmapMarkerIconID()
        .equals(ICON_ID)) {
      log.warning(
          "MarkerIcon '" + MyWarp.getInstance().getSettings().getDynmapMarkerIconID() // NON-NLS
          + "' does not exist. Using the default one."); // NON-NLS
      markerIcon = markerApi.getMarkerIcon(ICON_ID);
    }
    if (markerIcon == null) {
      markerIcon = markerApi.createMarkerIcon(ICON_ID, "Warp", // NON-NLS
                                              plugin.getResource("mywarp_warp-32.png")); // NON-NLS
    }

    // create the label
    markerSet = markerApi.getMarkerSet(LABEL_ID);
    if (markerSet == null) {
      markerSet = markerApi.createMarkerSet(LABEL_ID, MyWarp.getInstance().getSettings()
          .getDynmapLayerDisplayName(), null, false);
    } else {
      markerSet.setMarkerSetLabel(MyWarp.getInstance().getSettings().getDynmapLayerDisplayName());
    }
    markerSet.setLayerPriority(MyWarp.getInstance().getSettings().getDynmapLayerPriority());
    markerSet.setHideByDefault(MyWarp.getInstance().getSettings().isDynmapLayerHiddenByDefault());
    markerSet.setLabelShow(MyWarp.getInstance().getSettings().isDynmapMarkerShowLable());
    markerSet.setMinZoom(MyWarp.getInstance().getSettings().getDynmapMarkerMinZoom());

    // add all public warps
    for (Warp warp : manager.filter(WarpUtils.isType(Warp.Type.PUBLIC))) {
      addMarker(warp);
    }
    manager.registerHandler(this);
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
  public void onWarpRemoval(WarpRemovalEvent event) {
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
   * @see Warp#replacePlaceholders(String)
   */
  private String toLabelHtml(Warp warp) {
    String rawLabel = MESSAGES.getString("marker", MyWarp.getInstance().getSettings()
        .getLocalizationDefaultLocale());
    return warp.replacePlaceholders(rawLabel);

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
    markerSet
        .createMarker(toMarkerId(warp), toLabelHtml(warp), true, warp.getWorld().getName(), warp
                          .getPosition().getX(), warp.getPosition().getY(),
                      warp.getPosition().getZ(), markerIcon,
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
      marker.setLocation(warp.getWorld().getName(), warp.getPosition().getX(), warp.getPosition()
          .getY(), warp.getPosition().getZ());
    }
  }
}
