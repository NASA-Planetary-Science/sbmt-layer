package edu.jhuapl.sbmt.layer.impl;

import java.util.List;
import java.util.Set;

import edu.jhuapl.sbmt.layer.api.Layer;

public abstract class ResampledLayer extends BasicLayer
{
    protected ResampledLayer(int iSize, int jSize)
    {
        super(iSize, jSize);
    }

    protected abstract Layer getInputLayer();

    @Override
    public List<Integer> dataSizes()
    {
        return getInputLayer().dataSizes();
    }

    @Override
    public Set<Class<?>> getPixelTypes()
    {
        return getInputLayer().getPixelTypes();
    }

    @Override
    public String toString()
    {
        return super.toString() + " resampled from " + getInputLayer().toString();
    }

}
