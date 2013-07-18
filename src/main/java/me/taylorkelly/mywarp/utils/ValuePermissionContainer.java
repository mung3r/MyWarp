package me.taylorkelly.mywarp.utils;

/**
 * Classes that store values that are assign all-together via a permission can
 * extend this class as it handles the sorting of the storage objects
 * (alphabetical).
 */
public abstract class ValuePermissionContainer implements Comparable<ValuePermissionContainer> {

    private final String name;

    /**
     * Constructs this instance and assigns the name
     * 
     * @param name
     *            the name of this container, used on permission-lookup
     */
    public ValuePermissionContainer(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(ValuePermissionContainer c) {
        return name.compareTo(c.name);
    }

    /**
     * Gets the name of the value permission container
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

}
