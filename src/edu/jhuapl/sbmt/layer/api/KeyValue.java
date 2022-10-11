package edu.jhuapl.sbmt.layer.api;

/**
 * A key-value pair, that is an association between two strings.
 *
 * @author James Peachey
 *
 */
public interface KeyValue
{

    /**
     * Return the key of the key-value pair. This method may return an empty
     * string, but it shall never return null.
     *
     * @return the key
     */
    String key();

    /**
     * Return the value of the key-value pair. This method may return an empty
     * string, but it shall never return null.
     *
     * @return the value
     */
    String value();

}
