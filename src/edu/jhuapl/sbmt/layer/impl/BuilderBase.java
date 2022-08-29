package edu.jhuapl.sbmt.layer.impl;

import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Preconditions;

import edu.jhuapl.sbmt.layer.api.Layer;

/**
 * Base class for builders of all sorts used to create abstractions related to
 * {@link Layer} instances. Defines and enforces invariants for all the
 * properties used by any of the builders. The base implementation has only
 * protected methods and fields; subclasses can pick and choose which features
 * they use, and/or make public to the user of the subclass.
 * <p>
 * This class is completely independent of the type of data associated with
 * indices in the {@link Layer}.
 *
 * @author James Peachey
 *
 */
public abstract class BuilderBase
{

    // Using AtomicReference mainly for convenience, not concurrency. This class
    // is not thread-safe.
    protected final AtomicReference<Integer> iSize;
    protected final AtomicReference<Integer> jSize;
    protected final AtomicReference<Integer> kSize;
    protected final AtomicReference<ValidityChecker> checker;
    protected final AtomicReference<RangeGetter> rangeGetter;

    protected BuilderBase()
    {
        super();

        this.iSize = new AtomicReference<>();
        this.jSize = new AtomicReference<>();
        this.kSize = new AtomicReference<>();
        this.checker = new AtomicReference<>();
        this.rangeGetter = new AtomicReference<>();
    }

    /**
     * Set the size in the I-th dimension. Ensure specified size argument is
     * non-negative. Only allow this method to be called once for each builder
     * instance.
     *
     * @param size the size
     */
    protected void setISize(int size)
    {
        Preconditions.checkArgument(size >= 0);

        set(this.iSize, Integer.valueOf(size), "Cannot change size of I-th dimension after it is set");
    }

    protected int iSize()
    {
        return get(this.iSize, "Set the size of the I-th dimension first").intValue();
    }

    /**
     * Set the size in the J-th dimension. Ensure specified size argument is
     * non-negative. Only allow this method to be called once for each builder
     * instance.
     *
     * @param size the size
     */
    protected void setJSize(int size)
    {
        Preconditions.checkArgument(size > +0);

        set(this.jSize, Integer.valueOf(size), "Cannot change size of J-th dimension after it is set");
    }

    protected int jSize()
    {
        return get(this.jSize, "Set the size of the J-th dimension first");
    }

    /**
     * Set the size in the K-th dimension. Ensure specified size argument is
     * non-negative. Only allow this method to be called once for each builder
     * instance.
     *
     * @param size the size
     */
    protected void setKSize(int size)
    {
        Preconditions.checkArgument(size >= 0);

        set(this.kSize, Integer.valueOf(size), "Cannot change size of K-th dimension after it is set");
    }

    protected int kSize()
    {
        return get(this.kSize, "Set the size of the K-th dimension first");
    }

    /**
     * Set the validity checker to use when interpreting the content of the
     * layer.
     *
     * @param checker
     */
    protected void setChecker(ValidityChecker checker)
    {
        set(this.checker, checker, "Cannot change validity checker after it is set");
    }

    /**
     * Set the range getter used to determine the range of content in the layer.
     *
     * @param rangeGetter
     */
    protected void setRangeGetter(RangeGetter rangeGetter)
    {
        set(this.rangeGetter, rangeGetter, "Cannot change range getter after it is set");
    }

    /**
     * General method to make property setters more succinct. Enforces non-null
     * and set-only-once invariants.
     *
     * @param <T> generic type of the property
     * @param r the reference that holds the property
     * @param t the new property value, which may not be null
     * @param m the message to display if the caller attempts to set the same
     *            property twice
     * @throws NullPointerException if the new property is null
     * @throws IllegalStateException of the property was already set
     */
    protected <T> void set(AtomicReference<T> r, T t, String m)
    {
        Preconditions.checkNotNull(t);

        boolean setSucceeded = r.compareAndSet(null, t);

        Preconditions.checkState(setSucceeded, m);
    }

    /**
     * Get a value contained in an {@link AtomicReference}, but never return
     * null.
     *
     * @param <T> generic type of the property
     * @param r the reference that holds the property
     * @param m the message to display if the property in the reference is null
     * @return the property
     * @throws IllegalStateException if the reference holds a null
     */
    protected <T> T get(AtomicReference<T> r, String m)
    {
        T t = r.get();

        Preconditions.checkState(t != null, m);

        return t;
    }

}
