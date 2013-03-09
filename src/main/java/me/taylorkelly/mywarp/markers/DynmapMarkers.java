package me.taylorkelly.mywarp.markers;

import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.utils.WarpLogger;

public class DynmapMarkers implements Markers {

    private final MarkerAPI markerAPI;
    private MarkerIcon markerIcon;
    private MarkerSet markerSet;

    private static final String LABEL_ID = "mywarp.warps";
    private static final String MARKER_ID = "mywarp.warp.";
    private static final String ICON_ID = "mywarp_warp-32";

    public DynmapMarkers(MyWarp plugin) {
        markerAPI = ((DynmapCommonAPI) plugin.getServer().getPluginManager()
                .getPlugin("dynmap")).getMarkerAPI();

        // get Icon for all markers
        markerIcon = markerAPI.getMarkerIcon(WarpSettings.markerIconID);
        if (markerIcon == null && !WarpSettings.markerIconID.equals(ICON_ID)) {
            WarpLogger.warning("MarkerIcon '" + WarpSettings.markerIconID
                    + "' does not exist. Using the default one.");
            markerIcon = markerAPI.getMarkerIcon(ICON_ID);
        }
        if (markerIcon == null) {
            markerIcon = markerAPI.createMarkerIcon(ICON_ID, "Warp",
                    plugin.getResource("mywarp_warp-32.png"));
        }

        // create the label
        markerSet = markerAPI.getMarkerSet(LABEL_ID);
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet(LABEL_ID,
                    WarpSettings.layerDisplayName, null, false);
        } else {
            markerSet.setMarkerSetLabel(WarpSettings.layerDisplayName);
        }
        markerSet.setLayerPriority(WarpSettings.layerPriority);
        markerSet.setHideByDefault(WarpSettings.hideLayerByDefault);
        markerSet.setLabelShow(WarpSettings.showMarkerLable);
        markerSet.setMinZoom(WarpSettings.markerMinZoom);

        // add all warps
        for (Warp warp : plugin.getWarpList().getPublicWarps()) {
            addWarp(warp);
        }
    }

    @Override
    public void addWarp(Warp warp) {
        markerSet.createMarker(MARKER_ID + warp.name, warpLabel(warp), true,
                warp.world, warp.x, warp.y, warp.z, markerIcon, false);
    }

    @Override
    public void updateWarp(Warp warp) {
        Marker marker = getMatchingMarker(MARKER_ID + warp.name);

        if (marker != null) {
            marker.setLocation(warp.world, warp.x, warp.y, warp.z);
            marker.setLabel(warpLabel(warp));
        }
    }

    @Override
    public void deleteWarp(Warp warp) {
        Marker marker = getMatchingMarker(MARKER_ID + warp.name);

        if (marker != null) {
            marker.deleteMarker();
        }
    }

    private Marker getMatchingMarker(String id) {
        for (Marker marker : markerSet.getMarkers()) {
            if (marker.getMarkerID().equals(id)) {
                return marker;
            }
        }
        return null;
    }

    private String warpLabel(Warp warp) {
        return "<b>"
                + warp.name
                + "</b></br><i>"
                + LanguageManager.getEffectiveString("dynmap.createdBy",
                        "%creator%", warp.creator) + "</i>";

    }
}