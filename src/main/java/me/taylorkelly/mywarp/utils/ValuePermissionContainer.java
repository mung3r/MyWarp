package me.taylorkelly.mywarp.utils;

/**
 * Classes that store values that are assign all-together via a permission can
 * extend this class as it handles the sorting of the storage objects
 * (alphabetical).
 * 
 */
public abstract class ValuePermissionContainer implements
        Comparable<ValuePermissionContainer> {

    private final String name;

    public ValuePermissionContainer(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(ValuePermissionContainer c) {
        return name.compareTo(c.name);
    }

    public String getName() {
        return name;
    }

}
