/**
 * Copyright (C) 2011 - 2014, MyWarp team and contributors
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
package me.taylorkelly.mywarp.markers;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.utils.WarpUtils;

import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

/**
 * Sets location-markers for public warps on the map provided by Dynmap.
 */
public class DynmapMarkers implements Markers {

    private final MarkerAPI markerAPI;
    private MarkerIcon markerIcon;
    private MarkerSet markerSet;

    private static final String LABEL_ID = "mywarp.warps";
    private static final String MARKER_ID = "mywarp.warp";
    private static final String ICON_ID = "mywarp_warp-32";

    /**
     * Initializes this instance with the given dynmap-plugin.
     * 
     * @param dynmapPlugin
     *            the running dynmap instance to use
     */
    public DynmapMarkers(DynmapCommonAPI dynmapPlugin) {
        markerAPI = dynmapPlugin.getMarkerAPI();

        // get Icon for all markers
        markerIcon = markerAPI.getMarkerIcon(MyWarp.inst().getSettings().getDynmapMarkerIconID());
        if (markerIcon == null && !MyWarp.inst().getSettings().getDynmapMarkerIconID().equals(ICON_ID)) {
            MyWarp.logger().warning(
                    "MarkerIcon '" + MyWarp.inst().getSettings().getDynmapMarkerIconID()
                            + "' does not exist. Using the default one.");
            markerIcon = markerAPI.getMarkerIcon(ICON_ID);
        }
        if (markerIcon == null) {
            markerIcon = markerAPI.createMarkerIcon(ICON_ID, "Warp",
                    MyWarp.inst().getResource("mywarp_warp-32.png"));
        }

        // create the label
        markerSet = markerAPI.getMarkerSet(LABEL_ID);
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet(LABEL_ID, MyWarp.inst().getSettings()
                    .getDynmapLayerDisplayName(), null, false);
        } else {
            markerSet.setMarkerSetLabel(MyWarp.inst().getSettings().getDynmapLayerDisplayName());
        }
        markerSet.setLayerPriority(MyWarp.inst().getSettings().getDynmapLayerPriority());
        markerSet.setHideByDefault(MyWarp.inst().getSettings().isDynmapLayerHiddenByDefault());
        markerSet.setLabelShow(MyWarp.inst().getSettings().isDynmapMarkerShowLable());
        markerSet.setMinZoom(MyWarp.inst().getSettings().getDynmapMarkerMinZoom());

        // add all public warps
        for (Warp warp : MyWarp.inst().getWarpManager().getWarps(WarpUtils.isType(Warp.Type.PUBLIC))) {
            addMarker(warp);
        }
    }

    @Override
    public void addMarker(Warp warp) {
        if (!warp.isType(Warp.Type.PUBLIC)) {
            return;
        }
        markerSet.createMarker(toMarkerId(warp), toLabelHtml(warp), true, warp.getWorld().getName(),
                warp.getX(), warp.getY(), warp.getZ(), markerIcon, false);
    }

    @Override
    public void deleteMarker(Warp warp) {
        Marker marker = markerSet.findMarker(toMarkerId(warp));
        if (marker != null) {
            marker.deleteMarker();
        }
    }

    /**
     * Gets the label for the given warp.
     * 
     * @see Warp#replacePlaceholders(String)
     * @param warp
     *            the warp
     * @return the label for this warp
     */
    private String toLabelHtml(Warp warp) {
        return warp.replacePlaceholders(MyWarp.inst().getLocalizationManager()
                .getString("dynmap.marker", MyWarp.inst().getSettings().getLocalizationDefaultLocale()));

    }

    /**
     * Gets the ID for the marker of the given warp.
     * 
     * @param warp
     *            the warp
     * @return the unique ID of the marker for the given warp
     */
    private String toMarkerId(Warp warp) {
        return MARKER_ID + "." + warp.getName();
    }

    /**
     * Updates the label of the given warp's marker.
     * 
     * @param warp
     *            the warp
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
     * Updates the location of the given warp's marker.
     * 
     * @param warp
     *            the warp
     */
    private void updateLocation(Warp warp) {
        if (!warp.isType(Warp.Type.PUBLIC)) {
            return;
        }
        Marker marker = markerSet.findMarker(toMarkerId(warp));
        if (marker != null) {
            marker.setLocation(warp.getWorld().getName(), warp.getX(), warp.getY(), warp.getZ());
        }
    }

    @Override
    public void updateMarker(Warp warp, UpdateType type) {
        switch (type) {
        case CREATOR:
        case VISITS:
            updateLabel(warp);
            break;
        case LOCATION:
            updateLocation(warp);
            break;
        case TYPE:
            if (warp.isType(Warp.Type.PUBLIC)) {
                addMarker(warp);
            } else {
                deleteMarker(warp);
            }
            break;
        default:
            break;
        }
    }
}
