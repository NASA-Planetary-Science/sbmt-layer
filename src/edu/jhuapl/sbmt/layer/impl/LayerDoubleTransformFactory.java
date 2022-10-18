package edu.jhuapl.sbmt.layer.impl;

import java.util.function.Function;

import com.google.common.base.Preconditions;

import edu.jhuapl.sbmt.layer.api.KeyValueCollection;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.api.Pixel;
import edu.jhuapl.sbmt.layer.api.PixelDouble;
import edu.jhuapl.sbmt.layer.api.PixelOperator;
import edu.jhuapl.sbmt.layer.api.PixelVector;
import edu.jhuapl.sbmt.layer.impl.LayerTransformFactory.ForwardingLayer;

/**
 * Factory class for creating {@link Layer} transforms (that is,
 * {@link Function} instances that operate on a layer and return a new layer).
 * <p>
 * This factory provides transforms that involve changes to the data associated
 * with a pixel. It can operate on layers that support pixels of type
 * {@link PixelDouble} and {@link PixelVector}.
 * <p>
 * The {@link Function#apply(Layer)} methods for all the functions returned by
 * this factory will return null if called with a null layer argument.
 *
 * @author James Peachey
 *
 */
public class LayerDoubleTransformFactory
{
    protected static final PixelDoubleFactory PixelScalarFactory = new PixelDoubleFactory();

    protected static final PixelVectorDoubleFactory PixelVectorFactory = new PixelVectorDoubleFactory();

    protected static final LayerTransformFactory TransformFactory = new LayerTransformFactory();

    /**
     * {@link PixelOperator} instance that only sets the pixel out-of-bounds.
     */
    protected static final PixelOperator OutOfBoundsOperator = p -> p.setInBounds(false);

    @FunctionalInterface
    public interface DoubleTransform
    {
        double apply(double value);
    }

    public static final DoubleTransform DoubleIdentity = value -> {
        return value;
    };

    public LayerDoubleTransformFactory()
    {
        super();
    }

    /**
     * Convert the kernel of a transform, (a {@link DoubleTransform}, which
     * operates on a scalar double value) into a {@link Function} that operates
     * on a {@link Layer} by transforming values within the pixel.
     * <p>
     * The base implementation handles {@link PixelDouble} and
     * {@link PixelVector} pixels. When overriding this method, take care to
     * ensure that out-of-bounds values are not used in computations, and that
     * the correct {@link DoubleTransform} instance is used for valid and
     * invalid values.
     *
     * @param valueTransform the transform to use on valid values
     * @param invalidValueTransform the transform to use on invalid values (if
     *            null, the regular valueTransform will be used for invalid
     *            values as well).
     * @return the layer-to-layer transform
     */
    public Function<Layer, Layer> toLayerTransform(DoubleTransform valueTransform, DoubleTransform invalidValueTransform)
    {
        Preconditions.checkNotNull(valueTransform);

        DoubleTransform finalInvalidValueTransform;
        if (invalidValueTransform == null)
        {
            finalInvalidValueTransform = valueTransform;
        }
        else
        {
            finalInvalidValueTransform = invalidValueTransform;
        }

        Function<Layer, Layer> function = layer -> {
            Preconditions.checkNotNull(layer);

            return new ForwardingLayer(layer) {

                @Override
                public void get(int i, int j, Pixel p)
                {
                    if (p instanceof PixelDouble)
                    {
                        PixelDouble pd = (PixelDouble) p;

                        // Create a pixel in the same state as the input.
                        PixelDouble tmpPd = PixelScalarFactory.of(pd);
                        layer.get(i, j, tmpPd);

                        boolean valid = tmpPd.isValid();
                        boolean inBounds = tmpPd.isInBounds();
                        double outOfBoundsValue = tmpPd.getOutOfBoundsValue();

                        // Handle all the special cases.
                        double value;
                        if (!inBounds)
                        {
                            // Do not transform an out-of-bounds value, ever.
                            // Pass through the canonical value.
                            value = outOfBoundsValue;
                        }
                        else if (valid)
                        {
                            // Value is in-bounds and valid, so use the regular
                            // value transform.
                            value = valueTransform.apply(tmpPd.getStoredValue());
                        }
                        else
                        {
                            // Value is not valid, so use the invalid value
                            // transform.
                            value = finalInvalidValueTransform.apply(tmpPd.getStoredValue());
                        }

                        pd.set(value);
                        pd.setIsValid(valid);
                        pd.setInBounds(inBounds);
                    }
                    else if (p instanceof PixelVector pv)
                    {
                        // Make a copy of the pixel, and get its state from the
                        // layer.
                        PixelVector tmpPv = PixelVectorFactory.of(pv.size(), Double.NaN, null);
                        layer.get(i, j, tmpPv);

                        boolean valid = tmpPv.isValid();
                        boolean inBounds = tmpPv.isInBounds();
                        int kSize = tmpPv.size();

                        for (int k = 0; k < kSize; ++k)
                        {
                            Pixel pk = pv.get(k);
                            if (pk instanceof PixelDouble pd)
                            {
                                // Handle all the special cases.
                                double value;
                                if (!inBounds || !checkIndex(k, 0, kSize))
                                {
                                    // Do not transform an out-of-bounds value,
                                    // ever. Pass through the canonical value.
                                    value = pd.getOutOfBoundsValue();
                                }
                                else if (valid)
                                {
                                    // Value is in-bounds and valid, so use the
                                    // regular value transform.
                                    value = valueTransform.apply(pd.getStoredValue());
                                }
                                else
                                {
                                    // Value is not valid, so use the invalid
                                    // value
                                    // transform.
                                    value = finalInvalidValueTransform.apply(pd.getStoredValue());
                                }
                                pd.set(value);
                            }

                            pv.setIsValid(valid);
                            pv.setInBounds(inBounds);
                        }
                    }
                    else
                    {
                        throw new IllegalArgumentException();
                    }
                }

            };

        };

        return function;
    }

    /**
     * Returns a function that resamples a layer to produce a layer with
     * different index dimensions. With the exceptions of edges, corners, and
     * "invalid" pixels, as explained below, most pixels in the new layer are
     * interpolated from the 4 pixels in the original layer that lie closest to
     * the new pixel.
     * <p>
     * "Edge" pixels (those with index == 0 or index == size - 1, where "size"
     * is either iSize or jSize, respectively) are matched up exactly between
     * old and new indices. Most edge pixels will thus be interpolated between
     * only two pixels.
     * <p>
     * Similarly, "corner" pixels (those with both indices either 0 or size - 1)
     * will usually agree exactly between the two layers, since each output
     * corner is equal to its corresponding input corner.
     * <p>
     * The interpolation may usefully be considered a weighted average of the 4
     * nearest pixel values. If an input pixel is marked as "invalid" in the
     * input layer, it is treated as if that particular pixel has no weight,
     * i.e., only the valid pixels will contribute to the output pixel in the
     * calculation.
     * <p>
     * It follows that:
     * <ol>
     * <li>Output layers must have size >= 2 in each dimension so that the
     * output layer will have 4 edges.
     * <li>Output pixels are considered to be invalid only if all the
     * contributing input pixels are marked as invalid. Thus, even if the input
     * layer and output layer have exactly the same size, the output layer will
     * likely have fewer invalid pixels. One could thus use a linear
     * interpolation to do a minimal clean-up of invalid (e.g. "hot") pixels
     * without otherwise affecting the layer.
     * </ol>
     *
     * @param iNewSize the size of the output layer in the I dimension
     * @param jNewSize the size of the output layer in the J dimension
     * @return the function
     */
    public Function<Layer, Layer> linearInterpolate(int iNewSize, int jNewSize)
    {
        Preconditions.checkArgument(iNewSize > 1);
        Preconditions.checkArgument(jNewSize > 1);

        return layer -> {
            Preconditions.checkNotNull(layer);
            Preconditions.checkArgument(layer.isGetAccepts(PixelDouble.class));

            int iOrigSize = layer.iSize();
            int jOrigSize = layer.jSize();

            PixelDouble tmpPd = PixelScalarFactory.of(0., Double.NaN, Double.NaN);

            return new ResampledLayer(iNewSize, jNewSize) {

                @Override
                protected Layer getInputLayer()
                {
                    return layer;
                }

                @Override
                protected void getElement(int iNew, int jNew, int k, Pixel d)
                {
                    if (d instanceof PixelDouble pd)
                    {
                        // Get coordinates of the new pixel in the old pixel
                        // index space.
                        double x = (double) (iNew * (iOrigSize - 1)) / (iNewSize - 1);
                        double y = (double) (jNew * (jOrigSize - 1)) / (jNewSize - 1);

                        // Lower bounds in index space.
                        int i0 = (int) Math.floor(x);
                        int j0 = (int) Math.floor(y);

                        // Upper bounds in index space.
                        int i1 = i0 + 1;
                        int j1 = j0 + 1;

                        // Cast x and y into the range [0.0, 1.0).
                        x -= i0;
                        y -= j0;

                        // Get the 4 corner pixel values.
                        layer.get(i0, j0, tmpPd);
                        double pd00 = tmpPd.get();

                        layer.get(i0, j1, tmpPd);
                        double pd01 = tmpPd.get();

                        layer.get(i1, j0, tmpPd);
                        double pd10 = tmpPd.get();

                        layer.get(i1, j1, tmpPd);
                        double pd11 = tmpPd.get();

                        // Interpolate the two j0 corners, then the two j1
                        // corners.
                        double fy0 = interpolate(x, pd00, pd10);
                        double fy1 = interpolate(x, pd01, pd11);

                        // Interplate the two functions of y.
                        double fxy = interpolate(y, fy0, fy1);

                        if (Double.isFinite(fxy))
                        {
                            pd.set(fxy);
                            pd.setIsValid(true);
                        }
                        else
                        {
                            pd.setIsValid(false);
                        }
                    }
                    else
                    {
                        Preconditions.checkArgument(d instanceof PixelVector);
                        throw new UnsupportedOperationException("need to implement interpolation for vector case");
                    }

                }

                /**
                 * Interpolate function values at either end of a function. If
                 * the x position, or either end point is not finite, it is not
                 * used.
                 *
                 * @param x position within a pixel, in the range [0.0, 1.0)
                 * @param fx0 value of the function when x == 0.0
                 * @param fx1 value of the function when y == 1.0
                 * @return fx interpolated at x position
                 */
                double interpolate(double x, double fx0, double fx1)
                {
                    if (Double.isFinite(x))
                    {
                        if (Double.isFinite(fx0) && Double.isFinite(fx1))
                        {
                            return (1.0 - x) * fx0 + x * fx1;
                        }
                        else if (Double.isFinite(fx0))
                        {
                            return fx0;
                        }
                        else if (Double.isFinite(fx1))
                        {
                            return fx1;
                        }
                    }

                    return Double.NaN;
                }

                @Override
                public KeyValueCollection getKeyValueCollection()
                {
                    return layer.getKeyValueCollection();
                }

            };
        };
    }

    /**
     * Return a flag that indicates whether the specified index is in the
     * half-open range [minValue, maxValue).
     *
     * @param index the index value to check
     * @param minValid the minimum valid value for the index
     * @param maxValid one-past the maximum valid value for the index
     * @return true if the specified index is valid (in-bounds), false
     *         otherwise.
     */
    protected boolean checkIndex(int index, int minValid, int maxValid)
    {
        return index >= minValid && index < maxValid;
    }

    /**
     * Return a function that expands layers by padding them out with the
     * specified constant value.
     *
     * @param iLowerOffset number of pixels to add before index == 0 in the X/I
     *            dimension
     * @param iUpperOffset number of pixels to add after index == size - 1 in
     *            the X/I dimension
     * @param jLowerOffset number of pixels to add before index == 0 in the Y/J
     *            dimension
     * @param jUpperOffset number of pixels to add after index == size - 1 in
     *            the Y/J dimension
     * @param expandValue value to punch into output pixels that are created by
     *            expansion
     * @return the function
     * @see LayerTransformFactory#expand(int, int, int, int, PixelOperator) for
     *      a more general expansion utility
     */
    public Function<Layer, Layer> expand(int iLowerOffset, int iUpperOffset, int jLowerOffset, int jUpperOffset, double expandValue)
    {
        // The out-of-bounds value of this pixel will not normally be used.
        PixelDouble expandPixel = new PixelDoubleFactory().of(expandValue, Double.NaN);

        return new LayerTransformFactory().expand(iLowerOffset, iUpperOffset, jLowerOffset, jUpperOffset, p -> {
            p.assignFrom(expandPixel);
        });
    }

    /**
     * Utility method put an angle in degrees into the range [ 0.0, 360.0 ).
     *
     * @param angle in degrees
     * @return angle in degrees
     */
    public double putInRange0to360(double angle)
    {
        while (angle < 0.0)
        {
            angle += 360.0;
        }
        while (angle >= 360.0)
        {
            angle -= 360.0;
        }

        return angle;
    }

    /**
     * Return a function that rotates a layer about its center by the specified
     * number of degrees, preserving the dimensions of the original layer. Any
     * pixels in the output layer that cannot be associated with one or more
     * pixels in the input layer will be set to out-of-bounds.
     *
     * @param rotation the rotation in degrees to apply to a layer
     * @return the function
     * @see #rotatePreservingSize(double, double) for a rotation that allows the
     *      user to specify a value to place in expanded pixels
     * @see #rotatePreservingSize(double, PixelOperator) for a more general
     *      rotation utility
     */
    public Function<Layer, Layer> rotatePreservingSize(double rotation)
    {
        return rotatePreservingSize(rotation, OutOfBoundsOperator);
    }

    /**
     * Return a function that rotates a layer about its center by the specified
     * number of degrees, preserving the dimensions of the original layer. Any
     * pixels in the output layer that cannot be associated with one or more
     * pixels in the input layer will be set to the specified expansion value.
     *
     * @param rotation the rotation in degrees to apply to a layer
     * @param expandValue value to punch into output pixels that are created by
     *            rotation
     * @return the function
     * @see #rotatePreservingSize(double) for a rotation that simply marks
     *      created pixels as out-of-bounds
     * @see #rotatePreservingSize(double, PixelOperator) for a more general
     *      rotation utility
     */
    public Function<Layer, Layer> rotatePreservingSize(double rotation, double expandValue)
    {
        // The out-of-bounds value will not be used.
        PixelDouble expandPixel = new PixelDoubleFactory().of(expandValue, Double.NaN);

        return rotatePreservingSize(rotation, p -> {
            p.assignFrom(expandPixel);
        });
    }

    /**
     * Return a function that rotates a layer about its center by the specified
     * number of degrees, preserving the dimensions of the original layer. Any
     * pixels in the output layer that cannot be associated with one or more
     * pixels in the input layer will be operated upon by the specified
     * expansion operator.
     * <p>
     * The base implementation of this method only handles multiples of 90
     * degrees: 0, 90, 180 and so on. For all such rotations, square layers are
     * truly rotated, with each pixel in the input layer mapping to a pixel in
     * the output layer. Rectangular images are expanded, rotated, and then
     * cropped in such cases, so that only pixels in the square central region
     * of the input layer will be present in the output layer and portions of
     * the output layer will have been set up using the expand operator.
     *
     * @param rotation the rotation in degrees to apply to a layer
     * @param expandOperator that will act upon pixels not associated with input
     *            pixels
     * @return the function
     * @see #rotatePreservingSize(double) for a rotation that simply marks
     *      created pixels as out-of-bounds
     * @see #rotatePreservingSize(double, double) for a rotation that punches a
     *      single constant value into pixels in the output layer that are
     *      created by the rotation
     */
    public Function<Layer, Layer> rotatePreservingSize(double rotation, PixelOperator expandOperator)
    {
        Preconditions.checkNotNull(expandOperator);

        rotation = putInRange0to360(rotation);

        Function<Layer, Layer> function;
        if (rotation == 0.0)
        {
            function = TransformFactory.identity();
        }
        else if (rotation == 180.0)
        {
            function = TransformFactory.invertIJ();
        }
        else
        {
            Function<Layer, Layer> rotationOperator;
            if (rotation == 90.0)
            {
                rotationOperator = TransformFactory.rotateCCW();
            }
            else if (rotation == 270.0)
            {
                rotationOperator = TransformFactory.rotateCW();
            }
            else
            {
                throw new IllegalArgumentException("Rotation by angle of " + rotation + " is not (yet) supported.");
            }

            function = layer -> {
                int iSize = layer.iSize();
                int jSize = layer.jSize();

                int delta = Math.abs(iSize - jSize);
                int lowOffset = delta / 2;
                int highOffset = delta - lowOffset;

                Function<Layer, Layer> r;
                if (iSize > jSize)
                {
                    r = TransformFactory.expand(0, 0, lowOffset, highOffset, expandOperator) //
                            .andThen(rotationOperator) //
                            .andThen(TransformFactory.trimJ(lowOffset, highOffset));
                }
                else if (iSize < jSize)
                {
                    r = TransformFactory.expand(lowOffset, highOffset, 0, 0, expandOperator) //
                            .andThen(rotationOperator) //
                            .andThen(TransformFactory.trimI(lowOffset, highOffset));
                }
                else
                {
                    r = rotationOperator;
                }

                return r.apply(layer);
            };
        }

        return function;
    }

}
