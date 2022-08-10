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

    public PixelDouble of(double value, double outOfBoundsValue, Double invalidValue)
    {
        return new BasicPixelDouble(value, true, true) {

            @Override
            public double get()
            {
                if (invalidValue != null && !isValid() && isInBounds())
                {
                    return invalidValue.doubleValue();
                }

                return super.get();
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

        double outOfBoundsValue = pixel.getOutOfBoundsValue();
        double value = pixel.getStoredValue();
        boolean isValid = pixel.isValid();
        boolean inBounds = pixel.isInBounds();

        pixel = new BasicPixelDouble(value, isValid, inBounds) {

            @Override
            public double getOutOfBoundsValue()
            {
                return outOfBoundsValue;
            }

        };

        return pixel;
    }
}
