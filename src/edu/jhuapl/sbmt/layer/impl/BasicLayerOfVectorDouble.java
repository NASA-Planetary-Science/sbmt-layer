package edu.jhuapl.sbmt.layer.impl;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.sbmt.layer.api.Pixel;
import edu.jhuapl.sbmt.layer.api.PixelDouble;
import edu.jhuapl.sbmt.layer.api.PixelVector;

/**
 * Abstract extension of {@link BasicLayer} that assumes the native form of the
 * underlying data can be expressed as a double value that is looked up using a
 * set of 3 indices (I, J, K).
 *
 * @author James Peachey
 *
 */
public abstract class BasicLayerOfVectorDouble extends BasicLayer
{

    private static final Set<Class<?>> AcceptedPixelTypes = ImmutableSet.of(PixelDouble.class, PixelVector.class);

    /**
     * Constructor that creates a layer using the specified dimensions. The
     * caller must ensure iSize and jSize are non-negative.
     *
     * @param iSize the number of in-bounds values of the I index
     * @param jSize the number of in-bounds values of the J index
     */
    protected BasicLayerOfVectorDouble(int iSize, int jSize)
    {
        super(iSize, jSize);
    }

    @Override
    public boolean isGetAccepts(Class<?> pixelType)
    {
        return AcceptedPixelTypes.contains(pixelType);
    }

    /**
     * Handle all scalars whether in-or-out of bounds in the K index.
     */
    @Override
    protected void getElement(int i, int j, int k, Pixel p)
    {
        int kSize = kSize(i, j);

        if (p instanceof PixelDouble pd)
        {
            // Handle the case in which the number of elements in the pixel
            // exceeds the number in this layer at this position.
            boolean inBounds = checkIndex(k, 0, kSize);

            double value = inBounds ? doGetDouble(i, j, k) : pd.getOutOfBoundsValue();
            pd.set(value);
            pd.setIsValid(isValid(i, j, 0, value));
            pd.setInBounds(inBounds);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /*
     * Override to circumvent bounds checking at this level -- getScalar handles
     * that for each sub-pixel in this implementation.
     */
    @Override
    protected void getVector(int i, int j, PixelVector pv)
    {
        for (int k = 0; k < pv.size(); ++k)
        {
            getElement(i, j, k, pv.get(k));
        }
    }

    protected abstract double doGetDouble(int i, int j, int k);

    protected abstract boolean isValid(int i, int j, int k, double value);

    @Override
    public String toString()
    {
        return "Vector Double Layer(" + iSize() + ", " + jSize() + ") x " + dataSizes().get(0);
    }

}
