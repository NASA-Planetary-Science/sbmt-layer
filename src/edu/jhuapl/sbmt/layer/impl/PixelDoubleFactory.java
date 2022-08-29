package edu.jhuapl.sbmt.layer.impl;

import com.google.common.base.Preconditions;

import edu.jhuapl.sbmt.layer.api.PixelDouble;

/**
 * Factory for creatng {@link PixelDouble} instances. The base implementation
 * does this by extending {@link BasicPixelDouble}.
 *
 * @author James Peachey
 *
 */
public class PixelDoubleFactory
{

    public PixelDoubleFactory()
    {
        super();
    }

    public PixelDouble of(double value, double outOfBoundsValue)
    {
        return new BasicPixelDouble(value, true, true) {

            @Override
            public double getOutOfBoundsValue()
            {
                return outOfBoundsValue;
            }

        };
    }

    public PixelDouble of(double value, double outOfBoundsValue, Double invalidValue)
    {
        if (invalidValue == null)
        {
            return of(value, outOfBoundsValue);
        }

        return new BasicPixelDouble(value, true, true) {

            @Override
            public double get()
            {
                if (!isInBounds())
                {
                    return outOfBoundsValue;
                }
                else if (!isValid())
                {
                    return invalidValue.doubleValue();
                }

                return getStoredValue();
            }

            @Override
            public double getOutOfBoundsValue()
            {
                return outOfBoundsValue;
            }

        };
    }

    public PixelDouble of(PixelDouble pixel)
    {
        Preconditions.checkNotNull(pixel);

        double value = pixel.getStoredValue();
        double outOfBoundsValue = pixel.getOutOfBoundsValue();
        boolean inBounds = pixel.isInBounds();
        boolean isValid = pixel.isValid();

        pixel = of(value, outOfBoundsValue);
        pixel.setInBounds(inBounds);
        pixel.setIsValid(isValid);

        return pixel;
    }
}
