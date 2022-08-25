package edu.jhuapl.sbmt.layer.impl;

import com.google.common.base.Preconditions;

public class DoubleGetterAdaptor
{
    @FunctionalInterface
    public interface DoubleGetter1d
    {

        double get(int i);

    }

    /**
     * Re-indexing abstraction that goes from a pair of indices to a single
     * index.
     */
    @FunctionalInterface
    public interface IJtoSingleIndex
    {
        /**
         * Map the specified inputs to a single index that identifies the data
         * location in the layer implementation.
         *
         * @param i index in the I-th dimension
         * @param j index in the J-th dimenion
         * @return the equivalent single index
         */
        int getIndex(int i, int j);

    }

    /**
     * Re-indexing abstraction that goes from a triple of indices to a single
     * index.
     */
    @FunctionalInterface
    public interface IJKtoSingleIndex
    {

        /**
         * Map the specified inputs to a single index that identifies the data
         * location in the layer implementation.
         *
         * @param i index in the I-th dimension
         * @param j index in the J-th dimenion
         * @param k index in the K-th dimenion
         * @return the equivalent single index
         */
        int getIndex(int i, int j, int k);

    }

    public DoubleGetterAdaptor()
    {
        super();
    }

    /**
     * Return a reindexer that assumes the I-th diemension speacifies rows while
     * the J-th specifies columns.
     */
    public IJtoSingleIndex rowIcolumnJ(int jSize)
    {
        Preconditions.checkArgument(jSize >= 0);

        return (i, j) -> {
            return i * jSize + j;
        };
    }

    /**
     * Return a reindexer that assumes the I-th diemension speacifies columns
     * while the J-th specifies rows.
     */
    public IJtoSingleIndex columnIrowJ(int iSize)
    {
        Preconditions.checkArgument(iSize >= 0);

        return (i, j) -> {
            return j * iSize + i;
        };
    }

    public DoubleGetter2d adapt(DoubleGetter1d getter, IJtoSingleIndex reindexer)
    {
        Preconditions.checkNotNull(getter);
        Preconditions.checkNotNull(reindexer);

        return (i, j) -> {
            return getter.get(reindexer.getIndex(i, j));
        };
    }

    public DoubleGetter3d adapt(DoubleGetter1d getter, IJKtoSingleIndex reindexer)
    {
        Preconditions.checkNotNull(getter);
        Preconditions.checkNotNull(reindexer);

        return (i, j, k) -> {
            return getter.get(reindexer.getIndex(i, j, k));
        };
    }

}
