package edu.jhuapl.sbmt.layer.impl;

/**
 * Extension of {@link DoubleGetter} that gets double values indexed by two
 * indices.
 */
@FunctionalInterface
public interface DoubleGetter2d extends DoubleGetter
{

    /**
     * Get the value associated with the specified indices as a double
     *
     * @param i the index in the I-th dimension
     * @param j the index in the J-th dimension
     * @return the value
     */
    double get(int i, int j);

}