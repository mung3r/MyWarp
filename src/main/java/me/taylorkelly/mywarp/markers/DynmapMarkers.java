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

/**
 * Sets location-markers for public warps on the map provided by Dynmap
 */
public class DynmapMarkers implements Markers {

    private final MarkerAPI markerAPI;
    private MarkerIcon markerIcon;
    private MarkerSet markerSet;

    private static final String LABEL_ID = "mywarp.warps";
    private static final String MARKER_ID = "mywarp.warp.";
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
            markerSet = markerAPI.createMarkerSet(LABEL_ID,
                    MyWarp.inst().getSettings().getDynmapLayerDisplayName(), null, false);
        } else {
            markerSet.setMarkerSetLabel(MyWarp.inst().getSettings().getDynmapLayerDisplayName());
        }
        markerSet.setLayerPriority(MyWarp.inst().getSettings().getDynmapLayerPriority());
        markerSet.setHideByDefault(MyWarp.inst().getSettings().isDynmapLayerHiddenByDefault());
        markerSet.setLabelShow(MyWarp.inst().getSettings().isDynmapMarkerShowLable());
        markerSet.setMinZoom(MyWarp.inst().getSettings().getDynmapMarkerMinZoom());

        // add all public warps
        for (Warp warp : MyWarp.inst().getWarpManager().getWarps(new Predicate<Warp>() {

            @Override
            public boolean apply(Warp warp) {
                return warp.isType(Type.PUBLIC);
            }

        })) {
            addMarker(warp);
        }
    }

    @Override
    public void addMarker(Warp warp) {
        markerSet.createMarker(getMarkerId(warp), getLabel(warp), true, warp.getWorld().getName(),
                warp.getX(), warp.getY(), warp.getZ(), markerIcon, false);
    }

    @Override
    public void updateMarker(Warp warp) {
        if (warp.getType() != Warp.Type.PUBLIC) {
            return;
        }
        Marker marker = markerSet.findMarker(getMarkerId(warp));
        if (marker != null) {
            marker.setLocation(warp.getWorld().getName(), warp.getX(), warp.getY(), warp.getZ());
            marker.setLabel(getLabel(warp));
        }
    }

    @Override
    public void deleteMarker(Warp warp) {
        Marker marker = markerSet.findMarker(getMarkerId(warp));
        if (marker != null) {
            marker.deleteMarker();
        }
    }

    /**
     * Gets the ID for the marker of the given warp.
     * 
     * @param warp
     *            the warp
     * @return the unique ID of the marker for the given warp
     */
    private String getMarkerId(Warp warp) {
        return MARKER_ID + warp.getName();
    }

    /**
     * Gets the label for the given warp.
     * 
     * @param warp
     *            the warp
     * @return the label for this warp
     */
    private String getLabel(Warp warp) {
        return warp.replacePlaceholders(MyWarp.inst().getLocalizationManager()
                .getString("dynmap.marker", MyWarp.inst().getSettings().getLocalizationDefaultLocale()));

    }

    @Override
    public void handleTypeChange(Warp warp) {
        if (warp.getType() == Warp.Type.PUBLIC) {
            addMarker(warp);
        } else {
            deleteMarker(warp);
        }
    }
}