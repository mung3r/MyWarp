package me.taylorkelly.mywarp.markers;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.data.Warp.Type;

import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import com.google.common.base.Predicate;

public class DynmapMarkers implements Markers {

    private final MarkerAPI markerAPI;
    private MarkerIcon markerIcon;
    private MarkerSet markerSet;

    private static final String LABEL_ID = "mywarp.warps";
    private static final String MARKER_ID = "mywarp.warp.";
    private static final String ICON_ID = "mywarp_warp-32";

    public DynmapMarkers(DynmapCommonAPI dynmapPlugin) {
        markerAPI = dynmapPlugin.getMarkerAPI();

        // get Icon for all markers
        markerIcon = markerAPI.getMarkerIcon(MyWarp.inst().getWarpSettings().dynmapMarkerIconID);
        if (markerIcon == null && !MyWarp.inst().getWarpSettings().dynmapMarkerIconID.equals(ICON_ID)) {
            MyWarp.logger().warning(
                    "MarkerIcon '" + MyWarp.inst().getWarpSettings().dynmapMarkerIconID
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
            markerSet = markerAPI.createMarkerSet(LABEL_ID,
                    MyWarp.inst().getWarpSettings().dynmapLayerDisplayName, null, false);
        } else {
            markerSet.setMarkerSetLabel(MyWarp.inst().getWarpSettings().dynmapLayerDisplayName);
        }
        markerSet.setLayerPriority(MyWarp.inst().getWarpSettings().dynmapLayerPriority);
        markerSet.setHideByDefault(MyWarp.inst().getWarpSettings().dynmapLayerHideByDefault);
        markerSet.setLabelShow(MyWarp.inst().getWarpSettings().dynmapMarkerShowLable);
        markerSet.setMinZoom(MyWarp.inst().getWarpSettings().dynmapMarkerMinZoom);

        // add all public warps
        for (Warp warp : MyWarp.inst().getWarpManager().getWarps(new Predicate<Warp>() {

            @Override
            public boolean apply(Warp warp) {
                return warp.isType(Type.PUBLIC);
            }

        })) {
            addWarp(warp);
        }
    }

    @Override
    public void addWarp(Warp warp) {
        markerSet.createMarker(MARKER_ID + warp.getName(), warpLabel(warp), true, warp.getWorld().getName(),
                warp.getX(), warp.getY(), warp.getZ(), markerIcon, false);
    }

    @Override
    public void updateWarp(Warp warp) {
        Marker marker = getMatchingMarker(MARKER_ID + warp.getName());

        if (marker != null) {
            marker.setLocation(warp.getWorld().getName(), warp.getX(), warp.getY(), warp.getZ());
            marker.setLabel(warpLabel(warp));
        }
    }

    @Override
    public void deleteWarp(Warp warp) {
        Marker marker = getMatchingMarker(MARKER_ID + warp.getName());

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
        return warp.replaceWarpMacros(
                MyWarp.inst().getLocalizationManager()
                        .getString("dynmap.marker", MyWarp.inst().getWarpSettings().localizationDefLocale),
                null);

    }
}