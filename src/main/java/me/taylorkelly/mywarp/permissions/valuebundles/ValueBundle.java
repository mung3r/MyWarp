package me.taylorkelly.mywarp.permissions.valuebundles;

/**
 * A bundle for various values that are assigned all together via a defined
 * permission.
 */
public abstract class ValueBundle implements Comparable<ValueBundle> {

    private final String identifier;

    /**
     * Initializes this bundle with the given identifier. The identifier should
     * be unique.
     * 
     * @param identifier
     *            the identifier
     */
    public ValueBundle(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public int compareTo(ValueBundle that) {
        return identifier.compareTo(that.identifier);
    }

    /**
     * Gets the bundles identifier.
     * 
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the base-permission of this bundle. The base-permission is identical
     * for a set of bundles that set the same value-types. It is suffixed by a
     * bundles identifier to get the full permission that represents an
     * individual bundle.
     * 
     * @return the base-permission
     */
    protected abstract String getBasePermission();

    /**
     * Gets the full permission that a user needs to have to have the bundled
     * values.
     * 
     * @return the full permission of this bundle
     */
    public String getPermission() {
        return getBasePermission() + "." + identifier;
    }

}
