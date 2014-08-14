package me.taylorkelly.mywarp.permissions.valuebundles;

/**
 * A bundle for various values that are assigned all together via a permission.
 */
public interface ValueBundle extends Comparable<ValueBundle> {

    /**
     * Gets the bundles identifier.
     * 
     * @return the identifier
     */
    public String getIdentifier();

    /**
     * Gets the full permission that a user needs to have to have the bundled
     * values.
     * 
     * @return the full permission of this bundle
     */
    public String getPermission();

}