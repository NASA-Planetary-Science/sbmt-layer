package edu.jhuapl.sbmt.layer.impl;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.api.Pixel;
import edu.jhuapl.sbmt.layer.api.PixelDouble;
import edu.jhuapl.sbmt.layer.api.PixelVector;

public class LayerUtility
{
    protected static final int UndefinedIndex = -1;

    protected LayerUtility()
    {
        super();
    }

    public Layer append(Layer[] layers)
    {
        Preconditions.checkNotNull(layers);

        return append(ImmutableList.copyOf(layers));
    }

    public Layer append(Iterable<? extends Layer> layers)
    {
        Preconditions.checkNotNull(layers);

        int iSize = UndefinedIndex;
        int jSize = UndefinedIndex;
        int tmpKsize = 0;

        // This is to hold the first layer encountered in the input list for
        // which layer.isGetAccepts(PixelDouble.class) returns true.
        Layer tmpDefaultDoubleLayer = null;

        ImmutableList.Builder<Layer> builder = ImmutableList.builder();

        // This loop does two things: checks for layer compatibility
        // and finds the first input layer that could handle a scalar
        // pixel.
        for (Layer layer : layers)
        {
            // First check X, Y dims.
            int layerIsize = layer.iSize();
            int layerJsize = layer.jSize();

            if (iSize == UndefinedIndex)
            {
                iSize = layerIsize;
                jSize = layerJsize;
            }
            else
            {
                // Size compatibility checks.
                if (iSize != layerIsize)
                {
                    throw new IllegalArgumentException(String.format("Cannot append layer(s) with X size %d with a layer with X size %d", //
                            iSize, layerIsize));
                }
                else if (jSize != layerJsize)
                {
                    throw new IllegalArgumentException(String.format("Cannot append layer(s) with Y size %d with a layer with Y size %d", //
                            jSize, layerJsize));
                }
            }

            List<Integer> layerDataSizes = layer.dataSizes();

            // It is legal to have an empty layer, but just skip those.
            if (layerDataSizes.isEmpty())
            {
                continue;
            }

            // Enforce limitation to scalar or vector layers.
            if (layerDataSizes.size() > 1)
            {
                throw new IllegalArgumentException("Only can append layers that contain scalar or vector dimensions");
            }

            if (tmpDefaultDoubleLayer == null)
            {
                // Detect the first layer that can handle a PixelDouble, should
                // one
                // be passed to the get(...) method.
                if (layer.isGetAccepts(PixelDouble.class))
                {
                    tmpDefaultDoubleLayer = layer;
                }
            }

            // Accumulate the total number of elements in the output layer.
            tmpKsize += layerDataSizes.get(0);

            builder.add(layer);
        }

        ImmutableList<Layer> layerList = builder.build();

        // Handle corner cases: empty list or list with one layer.
        if (layerList.isEmpty())
        {
            return BasicLayer.emptyLayer();
        }
        else if (layerList.size() == 1)
        {
            return layerList.get(0);
        }

        // Invariants at this point should be:
        // a) layerList has > 1 layer.
        // b) all layers in list have the same x/i and y/j sizes.
        // c) all layers are valid scalar or vector.
        // d) tmpKsize is the depth of all the input layers' depth summed.
        // e) tmpDefaultDoubleLayer holds the first input layer that can handle
        // PixelDouble, or null if none can.
        Layer defaultDoubleLayer = tmpDefaultDoubleLayer;
        // This is the total size of the output layer.
        int kSize = tmpKsize;

        ImmutableList<Integer> dataSizes = ImmutableList.of(kSize);

        // The 0th layer will be used as the starting point for building the
        // resultant (appended) layer.
        Layer layer0 = layerList.get(0);

        return new LayerTransformFactory.ForwardingLayer(layer0) {

            @Override
            public List<Integer> dataSizes()
            {
                return dataSizes;
            }

            @Override
            public boolean isGetAccepts(Class<?> pixelType)
            {
                Preconditions.checkNotNull(pixelType);

                if (pixelType.isAssignableFrom(PixelDouble.class))
                {
                    return defaultDoubleLayer != null;
                }
                else if (pixelType.isAssignableFrom(PixelVector.class))
                {
                    return true;
                }

                return false;
            }

            @Override
            public void get(int i, int j, Pixel p)
            {
                if (p instanceof PixelDouble)
                {
                    if (defaultDoubleLayer != null)
                    {
                        defaultDoubleLayer.get(i, j, p);
                    }
                    else
                    {
                        throw new IllegalArgumentException();
                    }
                }
                else if (p instanceof PixelVector pv)
                {
                    int layerK = 0;
                    int pixelKsize = pv.size();
                    for (Layer layer : layerList)
                    {
                        if (layerK >= pixelKsize)
                        {
                            // Pixel is full, nothing more to do here.
                            break;
                        }
                        // All layers in this list are guaranteed to contain
                        // scalars or vectors, so this is safe.
                        int layerKsize = layer.dataSizes().get(0);

                        if (layerKsize == 1)
                        {
                            // Scalar case, pull out the relevant pixel from the
                            // vector.
                            layer.get(i, j, pv.get(layerK));
                            ++layerK;
                        }
                        else if (layer.isGetAccepts(PixelVector.class))
                        {
                            PixelVector subVector = createSubPixel(pv, layerK, layerKsize);
                            if (subVector == null)
                            {
                                // Means the whole pixel has been filled, so
                                // we're done.
                                break;
                            }
                            layer.get(i, j, subVector);
                            layerK += subVector.size();
                        }
                        else
                        {
                            throw new IllegalArgumentException();
                        }
                    }

                }
            }

        };
    }

    protected PixelVector createSubPixel(PixelVector pv, int startIndex, int subPixelSize)
    {
        return new ForwardingPixelVector(pv) {

            @Override
            public int size()
            {
                return subPixelSize;
            }

            @Override
            public Pixel get(int index)
            {
                return pv.get(index + startIndex);
            }
        };
    }

    protected static class ForwardingPixelVector implements PixelVector
    {
        private final PixelVector target;

        protected ForwardingPixelVector(PixelVector target)
        {
            super();

            this.target = target;
        }

        @Override
        public boolean isValid()
        {
            return target.isValid();
        }

        @Override
        public void setIsValid(boolean valid)
        {
            target.setIsValid(valid);
        }

        @Override
        public boolean isInBounds()
        {
            return target.isInBounds();
        }

        @Override
        public void setInBounds(boolean inBounds)
        {
            target.setInBounds(inBounds);
        }

        @Override
        public void assignFrom(Pixel source)
        {
            target.assignFrom(source);
        }

        @Override
        public int size()
        {
            return target.size();
        }

        @Override
        public Pixel get(int index)
        {
            return target.get(index);
        }

        @Override
        public String toString()
        {
            return target.toString();
        }

    }
}
