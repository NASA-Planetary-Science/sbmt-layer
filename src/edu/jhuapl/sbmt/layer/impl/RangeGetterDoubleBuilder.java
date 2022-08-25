package edu.jhuapl.sbmt.layer.impl;

import java.util.concurrent.atomic.AtomicReference;

public class RangeGetterDoubleBuilder extends DoubleBuilderBase
{

    protected final AtomicReference<Double> min;
    protected final AtomicReference<Double> max;

    public RangeGetterDoubleBuilder()
    {
        super();

        this.min = new AtomicReference<>();
        this.max = new AtomicReference<>();
    }

    public RangeGetterDoubleBuilder min(double min)
    {
        set(this.min, Double.valueOf(min), "Cannot change minimum value after it is set");

        return this;
    }

    protected Double min()
    {
        return min.get();
    }

    public RangeGetterDoubleBuilder max(double max)
    {
        set(this.max, Double.valueOf(max), "Cannot change maximum value after it is set");

        return this;
    }

    protected Double max()
    {
        return max.get();
    }

    public RangeGetterDoubleBuilder getter(DoubleGetter2d getter, int iSize, int jSize)
    {
        setGetter(getter);
        setISize(iSize);
        setJSize(jSize);

        return this;
    }

    public RangeGetterDoubleBuilder checker(ValidityChecker2d checker)
    {
        setChecker(checker);

        return this;
    }

    public DoubleRangeGetter build()
    {
        DoubleGetter2d getter = getter2d();

        double[] range = null;

        if (getter != null)
        {
            ValidityChecker2d checker = validityChecker2d();

            range = getRange(getter, checker);
        }

        range = updateMin(range, min());
        range = updateMax(range, max());

        return create(range);
    }

    protected double[] getRange(DoubleGetter2d doubleGetter, ValidityChecker2d checker)
    {
        double[] range = null;

        for (int i = 0; i < iSize(); ++i)
        {
            for (int j = 0; j < jSize(); ++j)
            {
                double value = doubleGetter.get(i, j);
                if (checker == null || checker.isValid(i, j, value))
                {
                    range = updateRange(range, value);
                }
            }
        }

        return range;
    }


    // MOve this somewhere else.
    protected DoubleRangeGetter create(double[] range)
    {

        double min = range != null ? range[0] : Double.NaN;
        double max = range != null ? range[1] : Double.NaN;

        return new DoubleRangeGetter() {

            @Override
            public double getMin()
            {
                return min;
            }

            @Override
            public double getMax()
            {
                return max;
            }

            @Override
            public String toString()
            {
                return "[" + min + ", " + max + "]";
            }
        };
    }

}
