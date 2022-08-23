package edu.jhuapl.sbmt.layer.impl;

import java.util.List;

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
    public boolean isGetAccepts(Class<?> pixelType)
    {
        return getInputLayer().isGetAccepts(pixelType);
    }

    @Override
    public String toString()
    {
        return super.toString() + " resampled from " + getInputLayer().toString();
    }

}
