package edu.jhuapl.sbmt.layer.impl;

/**
 * Checker for validity of 2-d layer double values, based on the location
 * (index pair) and/or the value at that location.
 */
@FunctionalInterface
public interface ValidityChecker2d extends ValidityChecker
{

    /**
     * Return true if the value should be considered "valid" for display or
     * computation.
     *
     * @param i the index in the I-th dimension to check
     * @param j the index in the J-th dimension to check
     * @param value the value to check
     * @return true if the value located by the index pair is valid
     */
    boolean isValid(int i, int j, double value);

}