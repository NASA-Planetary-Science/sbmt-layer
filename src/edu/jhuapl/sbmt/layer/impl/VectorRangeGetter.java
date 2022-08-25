package edu.jhuapl.sbmt.layer.impl;

import edu.jhuapl.sbmt.layer.api.Layer;

/**
 * Interface for abstractions that provide ranges for {@link Layer}s that
 * contain vector-valued pixels.
 */
public interface VectorRangeGetter extends RangeGetter
{
    /**
     * Return the number of range elements.
     *
     * @return number of elements in the vector
     */
    int size();

    /**
     * Return the {@link RangeGetter} associated with a specified index.
     *
     * @param index the index
     * @return the range getter
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    RangeGetter get(int index);
}