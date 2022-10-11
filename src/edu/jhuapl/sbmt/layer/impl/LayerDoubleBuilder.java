package edu.jhuapl.sbmt.layer.impl;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.layer.api.KeyValueCollection;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.api.Pixel;
import edu.jhuapl.sbmt.layer.api.PixelDouble;
import edu.jhuapl.sbmt.layer.api.PixelVector;

/**
 * Builder of {@link Layer} instances that return their contents as floating
 * point double values, indexed by I, J (scalar) or I, J, K (vector).
 *
 * @author James Peachey
 *
 */
public class LayerDoubleBuilder extends DoubleBuilderBase
{
    public LayerDoubleBuilder()
    {
        super();
    }

    public LayerDoubleBuilder doubleGetter(DoubleGetter2d getter, int iSize, int jSize)
    {
        setGetter(getter);
        setISize(iSize);
        setJSize(jSize);

        return this;
    }

    public LayerDoubleBuilder doubleGetter(DoubleGetter3d getter, int iSize, int jSize, int kSize)
    {
        setGetter(getter);
        setISize(iSize);
        setJSize(jSize);
        setKSize(kSize);

        return this;
    }

    public LayerDoubleBuilder checker(ValidityChecker checker)
    {
        setChecker(checker);

        return this;
    }

    public LayerDoubleBuilder rangeGetter(DoubleRangeGetter rangeGetter)
    {
        setRangeGetter(rangeGetter);

        return this;
    }

    public LayerDoubleBuilder rangeGetter(VectorRangeGetter rangeGetter)
    {
        setRangeGetter(rangeGetter);

        return this;
    }

    public LayerDoubleBuilder keyValueCollection(KeyValueCollection keyValueCollection)
    {
        setKeyValueCollection(keyValueCollection);

        return this;
    }

    public Layer build()
    {
        DoubleGetter2d getter2d = getter2d();
        DoubleGetter3d getter3d = getter3d();

        Preconditions.checkState(getter2d != null || getter3d != null, //
                "Set the getter of double values before calling the build method");

        int iSize = iSize();
        int jSize = jSize();

        KeyValueCollection keyValueCollection = this.keyValueCollection.get();
        if (keyValueCollection == null)
        {
            keyValueCollection = ImmutableKeyValueCollection.of();
        }

        Layer layer = null;
        if (getter2d != null)
        {
            layer = create(getter2d, iSize, jSize, keyValueCollection);
        }
        else if (getter3d != null)
        {
            int kSize = kSize();

            layer = create(getter3d, iSize, jSize, kSize, keyValueCollection);
        }
        else
        {
            throw new AssertionError("This can't happen due to the check state above");
        }

        return layer;
    }

    protected Layer create(DoubleGetter2d getter, int iSize, int jSize, KeyValueCollection keyValueCollection)
    {
        ValidityChecker2d checker = validityChecker2d();

        RangeGetter rangeGetter = this.rangeGetter.get();

        return new BasicLayerOfDouble(iSize, jSize) {

            @Override
            protected double doGetDouble(int i, int j)
            {
                return getter.get(i, j);
            }

            @Override
            public boolean isValid(int i, int j, double value)
            {
                if (checker == null)
                {
                    return super.isValid(iSize, jSize, value);
                }

                return checker == null || checker.isValid(i, j, value);
            }

            @Override
            public void getRange(Pixel pMin, Pixel pMax)
            {
                if (rangeGetter == null)
                {
                    super.getRange(pMin, pMax);
                    return;
                }

                Preconditions.checkNotNull(pMin);
                Preconditions.checkNotNull(pMax);

                LayerDoubleBuilder.this.getRange(rangeGetter, pMin, pMax);
            }

            @Override
            public KeyValueCollection getKeyValueCollection()
            {
                return keyValueCollection;
            }

            @Override
            public String toString()
            {
                String toString = super.toString();
                if (rangeGetter != null)
                {
                    toString += ", range " + rangeGetter;
                }

                return toString;
            }

        };
    }

    protected Layer create(DoubleGetter3d getter, int iSize, int jSize, int kSize, KeyValueCollection keyValueCollection)
    {
        List<Integer> dataSizes = ImmutableList.of(Integer.valueOf(kSize));

        ValidityChecker3d checker = validityChecker3d();

        RangeGetter rangeGetter = this.rangeGetter.get();

        return new BasicLayerOfVectorDouble(iSize, jSize) {

            @Override
            public List<Integer> dataSizes()
            {
                return dataSizes;
            }

            @Override
            protected double doGetDouble(int i, int j, int k)
            {
                return getter.get(i, j, k);
            }

            @Override
            protected boolean isValid(int i, int j, int k, double value)
            {
                return checker == null || checker.isValid(i, j, k, value);
            }

            @Override
            public void getRange(Pixel pMin, Pixel pMax)
            {
                if (rangeGetter == null)
                {
                    super.getRange(pMin, pMax);
                    return;
                }

                Preconditions.checkNotNull(pMin);
                Preconditions.checkNotNull(pMax);

                LayerDoubleBuilder.this.getRange(rangeGetter, pMin, pMax);
            }

            @Override
            public String toString()
            {
                String toString = super.toString();
                if (rangeGetter != null)
                {
                    toString += ", range " + rangeGetter;
                }

                return toString;
            }

            @Override
            public KeyValueCollection getKeyValueCollection()
            {
                return keyValueCollection;
            }

        };
    }

    protected void getRange(RangeGetter g, Pixel pMin, Pixel pMax)
    {
        Preconditions.checkArgument((pMin instanceof PixelDouble && pMax instanceof PixelDouble) //
                || (pMin instanceof PixelVector && pMax instanceof PixelVector), //
                "Min/max range pixels must be the same pixel type");

        if (pMin instanceof PixelVector pv)
        {
            Preconditions.checkArgument(pv.size() == ((PixelVector) pMax).size(), //
                    "Min/max range vector pixels must have the same number of elements");
        }

        if (g instanceof DoubleRangeGetter sg)
        {
            if (pMin instanceof PixelDouble)
            {
                getScalarRange(sg, (PixelDouble) pMin, (PixelDouble) pMax);
            }
            else
            {
                getScalarRange(sg, (PixelVector) pMin, (PixelVector) pMax);
            }
        }
        else if (g instanceof VectorRangeGetter vg)
        {
            if (pMin instanceof PixelDouble)
            {
                getVectorRange(vg, (PixelDouble) pMin, (PixelDouble) pMax);
            }
            else
            {
                getVectorRange(vg, (PixelVector) pMin, (PixelVector) pMax);
            }
        }
        else
        {
            throw new AssertionError("RangeGetter invariant was violated");
        }
    }

    protected void getScalarRange(DoubleRangeGetter sg, PixelDouble pMin, PixelDouble pMax)
    {
        setPixel(pMin, sg.getMin());
        setPixel(pMax, sg.getMax());
    }

    protected void getScalarRange(DoubleRangeGetter sg, PixelVector pMin, PixelVector pMax)
    {
        for (int i = 0; i < pMin.size(); ++i)
        {
            getRange(sg, pMin.get(i), pMax.get(i));
        }
    }

    protected void getVectorRange(VectorRangeGetter vg, PixelDouble pMin, PixelDouble pMax)
    {
        if (vg.size() > 0)
        {
            RangeGetter g = vg.get(0);
            if (g instanceof DoubleRangeGetter sg)
            {

                getScalarRange(sg, pMin, pMax);

                return;
            }
        }

        pMin.setIsValid(false);
        pMax.setIsValid(false);
    }

    protected void getVectorRange(VectorRangeGetter vg, PixelVector pMin, PixelVector pMax)
    {
        if (vg.size() > 0)
        {
            boolean atLeastOneValidMin = false;
            boolean atLeastOneValidMax = false;
            for (int i = 0; i < pMin.size(); ++i)
            {
                if (vg.size() > i)
                {

                    getRange(vg.get(i), pMin.get(i), pMax.get(i));
                    if (pMin.isValid())
                    {
                        atLeastOneValidMin = true;
                    }
                    if (pMax.isValid())
                    {
                        atLeastOneValidMax = true;
                    }
                }
                else
                {

                    setPixelIsValid(pMin.get(i), false);
                    setPixelIsValid(pMax.get(i), false);
                }
            }

            pMin.setIsValid(atLeastOneValidMin);
            pMax.setIsValid(atLeastOneValidMax);

            return;
        }

        setPixelIsValid(pMin, false);
        setPixelIsValid(pMax, false);
    }

    protected void setPixel(Pixel p, double value)
    {
        if (p instanceof PixelDouble pd)
        {
            pd.set(value);
            pd.setIsValid(true);
        }
        else if (p instanceof PixelVector pv)
        {
            for (int i = 0; i < pv.size(); ++i)
            {
                Pixel pd = pv.get(i);
                setPixel(pd, value);
            }
        }
        else
        {
            // This can't happen in the base implementation, but there's no
            // reason not to handle it this way.
            p.setIsValid(false);
        }
    }

    protected void setPixelIsValid(Pixel p, boolean isValid)
    {
        if (p instanceof PixelVector pv)
        {
            for (int i = 0; i < pv.size(); ++i)
            {
                setPixelIsValid(pv.get(i), isValid);
            }
        }

        p.setIsValid(isValid);
    }

}
