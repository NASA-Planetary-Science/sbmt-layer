package edu.jhuapl.sbmt.layer.api;

import java.util.List;
import java.util.Set;

/**
 * A collection of arbitrarily complex data whose elements are indexed in two
 * dimensions, I and J. Beyond this 2-index look-up, implementations are free to
 * structure and store their data in any desired form. Callers may obtain the
 * data associated with an (I, J) pair using an instance of {@link Pixel}.
 *
 * @see Pixel
 *
 * @author James Peachey
 *
 */
public interface Layer
{

    /**
     * Return the size of the layer in the I-th dimension. The size is a
     * one-past-last upper bound on the I index.
     *
     * @return the number of in-bounds values of the I index
     */
    int iSize();

    /**
     * Return the size of the layer in the J-th dimension. The size is a
     * one-past-last upper bound on the J index.
     *
     * @return the number of in-bounds values of the J index
     */
    int jSize();

    /**
     * Return the dimensionality and sizes of data elements located at each pair
     * of indices (I, J). Scalar implementations shall return a list with just
     * one element, equal to the integer 1. Vector implementations shall return
     * a list with just one element: an integer that indicates the maximum size
     * of any vector in the layer. Higher-dimensional data structures shall
     * return a list with one entry for each dimension. No implementation shall
     * return a null list, nor shall any list entry be null or non-positive.
     * <p>
     * Data associated with a particular pair of indices are permitted to have
     * variable size in any dimesion. The sizes in the returned list give the
     * maximum size of data elements in each dimension.
     *
     * @return the size information.
     */
    List<Integer> dataSizes();

    /**
     * Return a flag that indicates whether the specified indices are in-bounds
     * (true) or out-of-bounds (false) within the {@link Layer}.
     * <p>
     * The I index is in-bounds if it is in the half-open range [0, iSize() ).
     * The J index is in-bounds if it is in the half-open range [0, jSize() ).
     *
     * @param i the I index
     * @param j the J index
     * @return true if both indices are in bounds, false if either is not
     */
    default boolean isInBounds(int i, int j)
    {
        return i >= 0 && i < iSize() && j >= 0 && j < jSize();
    }

    /**
     * Return a set of the types of {@link Pixel} sub-interfaces that the
     * {@link #get(int, int, Pixel)} method accepts for the pixel argument.
     * <p>
     * Implementations are free to return an empty set, but they may not return
     * null.
     *
     * @return the interface types
     */
    Set<Class<?>> getPixelTypes();

    /**
     * Return a flag that indicates whether this implementation's
     * {@link #get(int, int, Pixel)} method can get data using pixels that
     * implement the specified type.
     *
     * @param pixelType the {@link Class} designating the type to check
     * @return true if this layer's {@link #get(int, int, Pixel)} method accepts
     *         pixels that have the type pixelType
     */
    default boolean isGetAccepts(Class<?> pixelType)
    {
        return getPixelTypes().contains(pixelType);
    }

    /**
     * Retrieve data associated with the specified indices, and use it to set
     * values in the specified {@link Pixel} instance. Implementations shall
     * always attempt to set all the values in the pixel, even if
     * {@link #isValid(int, int)} returns false for any reason.
     * <p>
     * If the specified pixel argument is null, implementations shall throw a
     * {@link NullPointerException}. If an implementation cannot or does not
     * know how to set the values of the specified {@link Pixel} instance using
     * its data, it shall throw an {@link IllegalArgumentException}.
     *
     * @param i the I index
     * @param j the J index
     * @param p the pixel, which will be mutated by this method
     * @param throws NullPointerException if p is null
     * @param throws IllegalArgumentException if the layer implementation does
     *            not know how to handle the specified instance of the pixel
     */
    void get(int i, int j, Pixel p);

    /**
     * Retrieve the smallest and largest values in the layer, and use them to
     * set the specified {@link Pixel} instances.
     * <p>
     * The default implementation assumes no range is defined, and simply calls
     * {@link Pixel#setIsValid(boolean)} passing a value of false, for both the
     * minimum and maximum pixel.
     * <p>
     * If either argument is null, implementations shall throw a
     * {@link NullPointerException}. If an implementation cannot or does not
     * know how to set values of the specified {@link Pixel} sub-type instance
     * using its data for either argument, it shall throw an
     * {@link IllegalArgumentException}.
     *
     * @param pMin the pixel used to return the minimum value in the layer
     * @param pMax the pixel used to return the maximum value in the layer
     * @param throws NullPointerException if pMin or pMax is null
     * @param throws IllegalArgumentException if the layer implementation does
     *            not know how to handle the specified instance of the pixel for
     *            either pMin or pMax
     */
    default void getRange(Pixel pMin, Pixel pMax)
    {
        if (pMin == null)
        {
            throw new NullPointerException();
        }
        if (pMax == null)
        {
            throw new NullPointerException();
        }

        pMin.setIsValid(false);
        pMax.setIsValid(false);
    }

}
