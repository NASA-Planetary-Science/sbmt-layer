package edu.jhuapl.sbmt.layer.impl;

import com.google.common.base.Preconditions;

import edu.jhuapl.sbmt.layer.api.PixelVector;

/**
 * Factory for creatng {@link PixelVector} instances. The base implementation
 * does this by extending {@link BasicPixelVectorDouble}.
 *
 * @author James Peachey
 *
 */
public class PixelVectorDoubleFactory
{

    public PixelVectorDoubleFactory()
    {
        super();
    }

    public PixelVector of(int size, double outOfBoundsValue)
    {
        return new BasicPixelVectorDouble(size, Double.NaN, true, true) {

            @Override
            protected double getOutOfBoundsValue()
            {
                return outOfBoundsValue;
            }
        };
    }

    public PixelVector of(int size, double outOfBoundsValue, Double invalidValue)
    {
        Preconditions.checkArgument(size >= 0);

        if (invalidValue == null)
        {
            return of(size, outOfBoundsValue);
        }

        return new BasicPixelVectorDouble(size, Double.NaN, true, true) {

            @Override
            protected ScalarPixel createPixelDouble(double initialValue)
            {
                return new ScalarPixel(initialValue, true, true) {

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

                };
            }

            @Override
            public double getOutOfBoundsValue()
            {
                return outOfBoundsValue;
            }

        };
    }

}
