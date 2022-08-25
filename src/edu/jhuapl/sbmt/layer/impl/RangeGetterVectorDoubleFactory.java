package edu.jhuapl.sbmt.layer.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.layer.impl.BuilderBase.RangeGetter;
import edu.jhuapl.sbmt.layer.impl.BuilderBase.VectorRangeGetter;
import edu.jhuapl.sbmt.layer.impl.DoubleBuilderBase.DoubleGetter2d;
import edu.jhuapl.sbmt.layer.impl.DoubleBuilderBase.DoubleGetter3d;
import edu.jhuapl.sbmt.layer.impl.DoubleBuilderBase.DoubleRangeGetter;
import edu.jhuapl.sbmt.layer.impl.DoubleBuilderBase.ValidityChecker2d;
import edu.jhuapl.sbmt.layer.impl.DoubleBuilderBase.ValidityChecker3d;

public class RangeGetterVectorDoubleFactory
{

    public RangeGetterVectorDoubleFactory()
    {
        super();
    }

    public VectorRangeGetter of(int kSize, Double min, Double max)
    {
        Preconditions.checkArgument(kSize >= 0);

        RangeGetterDoubleBuilder b = new RangeGetterDoubleBuilder();

        if (min != null)
        {
            b.min(min.doubleValue());
        }

        if (max != null)
        {
            b.max(max.doubleValue());
        }

        return of(kSize, b.build());
    }

    public VectorRangeGetter of(int kSize, DoubleRangeGetter rangeGetter)
    {
        Preconditions.checkArgument(kSize >= 0);
        Preconditions.checkNotNull(rangeGetter);

        return new VectorRangeGetter() {

            @Override
            public int size()
            {
                return kSize;
            }

            @Override
            public RangeGetter get(int index)
            {
                checkRange(index, size());

                return rangeGetter;
            }

        };
    }

    public VectorRangeGetter of(Iterable<? extends RangeGetter> ranges)
    {
        ImmutableList<RangeGetter> rangeList = ImmutableList.copyOf(ranges);

        return new VectorRangeGetter() {

            @Override
            public int size()
            {
                return rangeList.size();
            }

            @Override
            public RangeGetter get(int index)
            {
                checkRange(index, size());

                return rangeList.get(index);
            }

        };
    }

    public VectorRangeGetter of(DoubleGetter3d doubleGetter, ValidityChecker3d checker, DoubleRangeGetter overallRange, int iSize, int jSize, int kSize)
    {
        List<DoubleRangeGetter> ranges = new ArrayList<>(kSize);
        for (int k = 0; k < kSize; ++k)
        {
            DoubleGetter2d sGetter = kSlice(doubleGetter, k);
            ValidityChecker2d sChecker = slice(checker, k);

            RangeGetterDoubleBuilder b = new RangeGetterDoubleBuilder();

            if (overallRange != null)
            {
                b.min(overallRange.getMin());
                b.max(overallRange.getMax());
            }

            b.getter(sGetter, iSize, jSize);

            if (sChecker != null)
            {
                b.checker(sChecker);
            }

            ranges.add(b.build());
        }

        return of(ranges);
    }

    protected DoubleGetter2d kSlice(DoubleGetter3d doubleGetter, int k)
    {
        return (i, j) -> {
            return doubleGetter.get(i, j, k);
        };
    }

    protected ValidityChecker2d slice(ValidityChecker3d checker, int k)
    {
        ValidityChecker2d sChecker = checker != null ? (i, j, value) -> {
            return checker.isValid(i, j, k, value);
        } : null;

        return sChecker;
    }

    protected void checkRange(int k, int kSize)
    {
        if (k < 0 || k >= kSize)
        {
            throw new IndexOutOfBoundsException();
        }
    }
}
