package edu.jhuapl.sbmt.layer.impl;

import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Preconditions;

import edu.jhuapl.sbmt.layer.api.Layer;

/**
 * Extension of {@link BuilderBase} that is specific to {@link Layer} instances
 * whose underlying data is expressable as floating point doubles. Could be
 * scalar data (2-d layer indexed with just I, J) or vector data (3-d layer
 * indexed I, J, K). Subclasses can choose which protected methods they use,
 * and/or make accessible to the user of the subclass.
 * <p>
 * The builder provides options to allow subclasses to control access to the
 * data, validity checking for the data, and/or range-checking.
 *
 * @author James Peachey
 *
 */
public abstract class DoubleBuilderBase extends BuilderBase
{

    /**
     * Marker interface for abstractions that retrieve double values.
     */
    public interface DoubleGetter
    {

    }

    /**
     * Extension of {@link DoubleGetter} that gets double values indexed by two
     * indices.
     */
    @FunctionalInterface
    public interface DoubleGetter2d extends DoubleGetter
    {

        /**
         * Get the value associated with the specified indices as a double
         *
         * @param i the index in the I-th dimension
         * @param j the index in the J-th dimension
         * @return the value
         */
        double get(int i, int j);

    }

    /**
     * Extension of {@link DoubleGetter} that gets double values indexed by
     * three indices.
     */
    @FunctionalInterface
    public interface DoubleGetter3d extends DoubleGetter
    {

        /**
         * Get the value associated with the specified indices as a double
         *
         * @param i the index in the I-th dimension
         * @param j the index in the J-th dimension
         * @param k the index in the K-th dimension
         * @return the value
         */
        double get(int i, int j, int k);

    }

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

    /**
     * Checker for validity of 3-d layer double values, based on the location
     * (index triple) and/or the value at that location.
     */
    @FunctionalInterface
    public interface ValidityChecker3d extends ValidityChecker
    {

        /**
         * Return true if the value should be considered "valid" for display or
         * computation.
         *
         * @param i the index in the I-th dimension to check
         * @param j the index in the J-th dimension to check
         * @param k the index in the K-th dimension to check
         * @param value the value to check
         * @return true if the value located by the index triple is valid
         */
        boolean isValid(int i, int j, int k, double value);

    }

    /**
     * Extension of VectorRangeGetter and is guaranteed to use
     * {@link DoubleRangeGetter} for any (valid range) index.
     */
    public interface VectorDoubleRangeGetter extends VectorRangeGetter
    {

        /**
         * Override so the return type matches this class.
         */
        @Override
        DoubleRangeGetter get(int index);

    }

    private final AtomicReference<DoubleGetter> doubleGetter;

    protected DoubleBuilderBase()
    {
        super();

        this.doubleGetter = new AtomicReference<>();
    }

    /**
     * Extension of {@link RangeGetter} that has methods to get the minimum and
     * maximum values as doubles.
     */
    public interface DoubleRangeGetter extends RangeGetter
    {

        /**
         * Get the minimum value for the range. Implementations shall return
         * {@link Double#NaN} if the range has no defined minimum value.
         *
         * @return the minimum value in the range
         */
        double getMin();

        /**
         * Get the maximum value for the range. Implementations shall return
         * {@link Double#NaN} if the range has no defined maximum value.
         *
         * @return the maximum value in the range
         */
        double getMax();

    }

    /**
     * Use this method to tell a builder how to get double values from the
     * underlying layer implementation. This method may only be called once per
     * builder.
     *
     * @param getter the getter of double values
     * @throws NullPointerException if the getter is null
     * @throws IllegalStateException if the value was already set
     */
    protected void setGetter(DoubleGetter getter)
    {
        set(this.doubleGetter, getter, "Cannot change the value getter after it is set");
    }

    /**
     * Return this builder's {@link DoubleGetter2d} instance, if it has one, or
     * null if it does not.
     *
     * @return the 2-d getter
     */
    protected DoubleGetter2d getter2d()
    {
        DoubleGetter doubleGetter = this.doubleGetter.get();

        return doubleGetter instanceof DoubleGetter2d ? (DoubleGetter2d) doubleGetter : null;
    }

    /**
     * Return this builder's {@link DoubleGetter3d} instance, if it has one, or
     * null if it does not.
     *
     * @return the 3-d getter
     */
    protected DoubleGetter3d getter3d()
    {
        DoubleGetter doubleGetter = this.doubleGetter.get();

        return doubleGetter instanceof DoubleGetter3d ? (DoubleGetter3d) doubleGetter : null;
    }

    /**
     * If the builder has been configured with a {@link ValidityChecker2d},
     * return it. If no validity checker of any kind has been specified, return
     * null. The checker is configured using the method
     * {@link BuilderBase#setChecker(ValidityChecker)}.
     *
     * @return the checker
     * @throws IllegalStateException if the builder was configured with some
     *             other (non-scalar) validity checker
     */
    protected ValidityChecker2d validityChecker2d()
    {
        ValidityChecker checker = this.checker.get();

        Preconditions.checkState(checker == null || checker instanceof ValidityChecker2d);

        return (ValidityChecker2d) checker;
    }

    /**
     * If the builder has been configured with a {@link ValidityChecker3d},
     * return it. If no validity checker of any kind has been specified, return
     * null. The checker is configured using the method
     * {@link BuilderBase#setChecker(ValidityChecker)}.
     *
     * @return the checker
     * @throws IllegalStateException if the builder was configured with some
     *             other (non-3d) validity checker
     */
    protected ValidityChecker3d validityChecker3d()
    {
        ValidityChecker checker = this.checker.get();

        Preconditions.checkState(checker == null || checker instanceof ValidityChecker3d);

        return (ValidityChecker3d) checker;
    }

    /**
     * If the builder has been configured with a {@link DoubleRangeGetter},
     * return it. If no range getter of any kind has been specified, return
     * null. The range getter is configured using the method
     * {@link BuilderBase#setRangeGetter(RangeGetter)}.
     *
     * @return the checker
     * @throws IllegalStateException if the builder was configured with some
     *             other (non-scalar) range getter
     */
    protected DoubleRangeGetter scalarRangeGetter()
    {
        RangeGetter getter = this.rangeGetter.get();

        Preconditions.checkState(getter == null | getter instanceof DoubleRangeGetter);

        return (DoubleRangeGetter) getter;
    }

    /**
     * If the builder has been configured with a {@link VectorRangeGetter},
     * return it. If no range getter of any kind has been specified, return
     * null. The range getter is configured using the method
     * {@link BuilderBase#setRangeGetter(RangeGetter)}.
     *
     * @return the checker
     * @throws IllegalStateException if the builder was configured with some
     *             other (non-vector) range getter
     */
    protected VectorRangeGetter vectorRangeGetter()
    {
        RangeGetter getter = this.rangeGetter.get();

        Preconditions.checkState(getter == null | getter instanceof VectorRangeGetter);

        return (VectorRangeGetter) getter;
    }

    /**
     * General utility that returns an updated range based on the input range
     * and value. The output minimum range value will be the smaller of the
     * input minimum value range[0] and the specified value argument. Similarly,
     * the output maximum range value will be the larger of the input maximum
     * value range[1] and the specified value argument.
     * <p>
     * If the specified input range argument is null, the range returned will
     * have both minimum and maximum equal to the specified value.
     *
     * @param range 2-element array holding the minimum/maximum range values in
     *            the 0-th/1-st element, respectively
     * @param value the potentially new minimum/maximum value
     * @return the updated (possibly expanded) range
     */
    public double[] updateRange(double[] range, double value)
    {
        if (range == null)
        {
            range = initRange(value, value);
        }
        else
        {
            if (Double.isFinite(value))
            {
                // Finite value replaces non-finite min or min that is > value.
                if (!Double.isFinite(range[0]) || value < range[0])
                {
                    range[0] = value;
                }

                // Finite value replaces non-finite max or max that is < value.
                if (!Double.isFinite(range[1]) || value > range[1])
                {
                    range[1] = value;
                }
            }
            else
            {
                // Non-finite value only replaces non-finite range entries.
                if (!Double.isFinite(range[0]))
                {
                    range[0] = value;
                }

                if (!Double.isFinite(range[1]))
                {
                    range[1] = value;
                }
            }
        }

        return range;
    }

    /**
     * General utility that returns a range with updated minimum value, based on
     * the input range and value. The output minimum range value will be the
     * smaller of the input minimum value range[0] and the specified value
     * argument. The output maximum value range[1] will be unaffected.
     * <p>
     * If the specified minimum value is null, this method simply returns the
     * original range.
     *
     * @param range
     * @param min
     * @return
     */
    public double[] updateMin(double[] range, Double min)
    {
        if (min != null && Double.isFinite(min.doubleValue()))
        {
            if (range == null)
            {
                range = initRange(min, Double.NaN);
            }
            else if (!Double.isFinite(range[0]) || min.doubleValue() < range[0])
            {
                range[0] = min.doubleValue();
            }
        }

        return range;
    }

    public double[] updateMax(double[] range, Double max)
    {
        if (max != null && Double.isFinite(max.doubleValue()))
        {
            if (range == null)
            {
                range = initRange(Double.NaN, max);
            }
            else if (!Double.isFinite(range[1]) || max.doubleValue() > range[1])
            {
                range[1] = max.doubleValue();
            }
        }

        return range;
    }

    public double[] initRange(double min, double max)
    {
        return new double[] { min, max };
    }

    private static final ValidityChecker2d AllValid2d = (i, j, value) -> {
        return true;
    };

    private static final ValidityChecker3d AllValid3d = (i, j, k, value) -> {
        return true;
    };

    public static ValidityChecker2d allValid2d()
    {
        return AllValid2d;
    }

    public static ValidityChecker3d allValid3d()
    {
        return AllValid3d;
    }

}
