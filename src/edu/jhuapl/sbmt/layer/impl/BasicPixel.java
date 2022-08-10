package edu.jhuapl.sbmt.layer.impl;

import edu.jhuapl.sbmt.layer.api.Pixel;

/**
 * Basic implementation of the {@link Pixel} interface that uses simple fields
 * to store the relevant flags.
 *
 * @author James Peachey
 *
 */
public abstract class BasicPixel implements Pixel
{

    private volatile boolean isValid;
    private volatile boolean inBounds;

    protected BasicPixel(boolean isValid, boolean inBounds)
    {
        super();

        this.isValid = isValid;
        this.inBounds = inBounds;
    }

    @Override
    public boolean isValid()
    {
        return isValid && isInBounds();
    }

    @Override
    public void setIsValid(boolean isValid)
    {
        this.isValid = isValid;
    }

    @Override
    public boolean isInBounds()
    {
        return inBounds;
    }

    @Override
    public void setInBounds(boolean inBounds)
    {
        this.inBounds = inBounds;
    }

}
