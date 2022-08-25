package edu.jhuapl.sbmt.layer.impl;

/**
 * Checker for validity of 3-d layer double values, based on the location
 * (index triple) and/or the value at that location.
 */
@FunctionalInterface
public interface ValidityChecker3d extends ValidityChecker
{

    /**
     * Return true if the value should be considered "valid" for display or
     * computation.
     *
     * @param i the index in the I-th dimension to check
     * @param j the index in the J-th dimension to check
     * @param k the index in the K-th dimension to check
     * @param value the value to check
     * @return true if the value located by the index triple is valid
     */
    boolean isValid(int i, int j, int k, double value);

}