package edu.jhuapl.sbmt.layer.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.layer.api.Pixel;
import edu.jhuapl.sbmt.layer.api.PixelDouble;
import edu.jhuapl.sbmt.layer.api.PixelVector;

/**
 * Implementation of {@link PixelVector} that inherits its general {@link Pixel}
 * functionality from {@link BasicPixel}.
 *
 * @author James Peachey
 *
 */
public abstract class BasicPixelVectorDouble extends BasicPixel implements PixelVector
{

    private final ImmutableList<ScalarPixel> pixels;

    protected BasicPixelVectorDouble(int size, double initialValue, boolean isValid, boolean inBounds)
    {
        super(isValid, inBounds);

        this.pixels = initializePixels(size, initialValue);
    }

    protected ImmutableList<ScalarPixel> initializePixels(int size, double initialValue)
    {
        ImmutableList.Builder<ScalarPixel> builder = ImmutableList.builder();

        for (int i = 0; i < size; ++i)
        {
            builder.add(createPixelDouble(initialValue));
        }

        return builder.build();
    }

    protected ScalarPixel createPixelDouble(double initialValue)
    {
        return new ScalarPixel(initialValue, true, true);
    }

    @Override
    public boolean isValid()
    {
        if (!super.isValid())
        {
            return false;
        }

        for (int k = 0; k < size(); ++k)
        {
            if (((ScalarPixel) get(k)).isThisPixelValid())
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isInBounds()
    {
        if (!super.isInBounds())
        {
            return false;
        }

        for (int k = 0; k < size(); ++k)
        {
            if (((ScalarPixel) get(k)).isThisPixelInBounds())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Return the value that the {@link #getDouble(int)} method returns if this
     * pixel is out of bounds in its layer, that is, if {@link #isInBounds()}
     * returns false.
     * <p>
     * This value shall ALSO returned if the argument to {@link #getDouble(int)}
     * is out of bounds within the pixel.
     *
     * @return the out-of-bounds value
     */
    protected abstract double getOutOfBoundsValue();

    protected Double getInvalidValueToReturn()
    {
        return null;
    }

    @Override
    public int size()
    {
        return pixels.size();
    }

    @Override
    public PixelDouble get(int index)
    {
        if (!checkIndex(index, 0, size()))
        {
            throw new IndexOutOfBoundsException();
        }

        return pixels.get(index);
    }

    @Override
    public void assignFrom(Pixel source)
    {
        Preconditions.checkNotNull(source);

        if (source instanceof PixelVector pv)
        {
            for (int k = 0; k < size(); ++k)
            {
                get(k).assignFrom(pv.get(k));
            }
        }
        else if (source instanceof PixelDouble pd)
        {
            get(0).assignFrom(pd);
            for (int k = 1; k < size(); ++k)
            {
                PixelDouble sp = get(k);
                sp.set(sp.getOutOfBoundsValue());
                sp.setIsValid(isValid());
                sp.setInBounds(false);
            }
        }
        else
        {
            throw new IllegalArgumentException("Cannot assign to a vector double pixel from pixel of type " + source.getClass());
        }
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

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        if (!isInBounds())
        {
            builder.append("O (");
        }
        else if (!isValid())
        {
            builder.append("I (");
        }
        else
        {
            builder.append("  (");
        }

        String delim = "";
        for (int k = 0; k < size(); ++k)
        {
            builder.append(delim);
            builder.append(get(k));

            delim = ", ";
        }
        builder.append(")");

        return builder.toString();
    }

    protected class ScalarPixel extends BasicPixelDouble
    {

        protected ScalarPixel(double initialValue, boolean isValid, boolean inBounds)
        {
            super(initialValue, isValid, inBounds);
        }

        @Override
        public boolean isValid()
        {
            return BasicPixelVectorDouble.this.isValid() && isThisPixelValid();
        }

        protected boolean isThisPixelValid()
        {
            return super.isValid();
        }

        @Override
        public boolean isInBounds()
        {
            return BasicPixelVectorDouble.this.isInBounds() && isThisPixelInBounds();
        }

        protected boolean isThisPixelInBounds()
        {
            return super.isInBounds();
        }

        @Override
        public double getOutOfBoundsValue()
        {
            return BasicPixelVectorDouble.this.getOutOfBoundsValue();
        }

    }

}
