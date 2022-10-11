package edu.jhuapl.sbmt.layer.api;

/**
 * Indexable collection of {@link KeyValue} pair instances.
 *
 * @author James Peachey
 *
 */
public interface KeyValueCollection
{

    /**
     * Return the number of this collection's key-value pairs. Implementations
     * shall not return a negative number.
     *
     * @return the number of key-value pairs in the collection
     */
    int size();

    /**
     * Return the {@link KeyValue} instance for the specified index. This method
     * shall never return null.
     * <p>
     * Implementations may allow duplicate key-value pairs, and/or distinct
     * pairs with duplicate keys.
     *
     * @param i index of the key-value pair
     * @return the key-value pair
     * @throws IndexOutOfBoundsException if the index is out-of-bounds
     */
    KeyValue get(int i);

}
