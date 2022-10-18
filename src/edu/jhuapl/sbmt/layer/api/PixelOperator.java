package edu.jhuapl.sbmt.layer.api;

/**
 * Functional operator that acts upon pixels. May be used to inject specific
 * behaviors into more generic layer/pixel manipulations.
 *
 * @author James Peachey
 *
 */
@FunctionalInterface
public interface PixelOperator
{

    /**
     * Perform an operation on the specified pixel, such as marking it
     * out-of-bounds or invalid, or setting it to some value or set of values.
     *
     * @param p the pixel
     */
    void operate(Pixel p);

}
