package edu.jhuapl.sbmt.layer.gdal;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdalconst.gdalconst;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.api.PixelDouble;
import edu.jhuapl.sbmt.layer.impl.BasicLayer;
import edu.jhuapl.sbmt.layer.impl.DoubleBuilderBase.DoubleRangeGetter;
import edu.jhuapl.sbmt.layer.impl.DoubleGetter2d;
import edu.jhuapl.sbmt.layer.impl.DoubleGetter3d;
import edu.jhuapl.sbmt.layer.impl.LayerDoubleBuilder;
import edu.jhuapl.sbmt.layer.impl.RangeGetter;
import edu.jhuapl.sbmt.layer.impl.ValidityChecker3d;
import edu.jhuapl.sbmt.layer.impl.VectorRangeGetter;

/**
 * Load a list of {@link Layer}s from a file using GDAL.
 *
 * @author James Peachey
 *
 */
public abstract class LayerLoader
{

    protected LayerLoader()
    {
        super();
    }

    protected abstract Dataset getDataSet();

    protected ValidityChecker3d getValidityChecker()
    {
        return null;
    }

    /**
     * Load one or more {@link Layer}s from the GDAL {@link Dataset} returned by
     * the {@link #getDataSet()} method.
     * <p>
     * The base implementation in {@link LayerLoader} iterates over the
     * {@link Band} instances returend by the {@link Dataset#GetRasterBand(int)}
     * method, calling {@link #createLayer(Band)} to create a {@link Layer} of
     * scalar values from each band.
     * <p>
     * For example, if the {@link Dataset} being loaded were from a FITS image
     * of dimension 2048 x 1088 x 3, the base implementation would return a list
     * of 3 {@link Layer}s, each of dimension 2048 x 1088. See
     * {@link #createLayer(Band)} for more information about how each layer is
     * handled.
     *
     * @return the loaded layers
     * @throws UnsupportedDataTypeException if the underlying type in the GDAL
     *             {@link Dataset} cannot be read into the layer
     */
    public Layer load()
    {
        Dataset dataSet = getDataSet();
        int numBands = dataSet.GetRasterCount();

        ImmutableList.Builder<DoubleGetter2d> dataBuilder = ImmutableList.builder();
        ImmutableList.Builder<DoubleRangeGetter> rangeBuilder = ImmutableList.builder();

        Integer iSize = null;
        Integer jSize = null;

        for (int bandIndex = 0; bandIndex < numBands; ++bandIndex)
        {
            Band band = dataSet.GetRasterBand(bandIndex + 1);

            if (band != null)
            {
                if (iSize == null)
                {
                    iSize = Integer.valueOf(band.GetXSize());
                }
                else
                {
                    iSize = Math.max(iSize.intValue(), band.GetXSize());
                }

                if (jSize == null)
                {
                    jSize = Integer.valueOf(band.GetYSize());
                }
                else
                {
                    jSize = Math.max(jSize.intValue(), band.GetYSize());
                }

                dataBuilder.add(loadData(band));

                Double[] min = new Double[1];
                Double[] max = new Double[1];

                band.GetMinimum(min);
                band.GetMaximum(max);

                double finalMin = min[0] != null ? min[0].doubleValue() : Double.NEGATIVE_INFINITY;
                double finalMax = max[0] != null ? max[0].doubleValue() : Double.POSITIVE_INFINITY;

                rangeBuilder.add(new DoubleRangeGetter() {

                    @Override
                    public double getMin()
                    {
                        return finalMin;
                    }

                    @Override
                    public double getMax()
                    {
                        return finalMax;
                    }

                    @Override
                    public String toString()
                    {
                        return "range [" + finalMin + ", " + finalMax + "]";
                    }
                });
            }
        }

        ImmutableList<DoubleGetter2d> data = dataBuilder.build();

        if (data.isEmpty())
        {
            return BasicLayer.emptyLayer();
        }

        LayerDoubleBuilder layerBuilder = new LayerDoubleBuilder();

        DoubleGetter3d dg3d = (i, j, k) -> {
            return data.get(k).get(i, j);
        };
        layerBuilder.doubleGetter(dg3d, iSize.intValue(), jSize.intValue(), data.size());

        layerBuilder.checker(getValidityChecker());

        ImmutableList<DoubleRangeGetter> ranges = rangeBuilder.build();

        VectorRangeGetter vrg = new VectorRangeGetter() {

            @Override
            public int size()
            {
                return ranges.size();
            }

            @Override
            public RangeGetter get(int index)
            {
                return ranges.get(index);
            }

            @Override
            public String toString()
            {
                return ranges.get(0).toString() + "...";
            }

        };

        layerBuilder.rangeGetter(vrg);

        return layerBuilder.build();
    }

    /**
     * Load one layer's worth of data as a {@link DoubleGetter2d} from a single
     * GDAL {@link Band}.
     * <p>
     * The base implementation in {@link LayerLoader} uses the return value of
     * {@link Band#getDataType()} to determine the smallest Java primitve data
     * type that can accommodate the native data type contained in the band.
     * Most types can be accurately converted to double, but 64-bit integer
     * types, while converted accurately, will suffer a loss of precision.
     * <p>
     * The base implementation handles all GDAL data types that do not represent
     * complex values.
     * <p>
     * For example, if the band contained an array of 2048 x 1088 pixels of type
     * {@link gdalconst.GDT_Byte}, which signifies a data type of unsigned 8-bit
     * bytes, the base implementation would return a layer with dimensions 2048
     * x 1088 of the Java primitve type short, which at 16 bits (including a
     * sign bit) is the smallest (signed) type that can accurately represent
     * unsigned 8-bit values. If values from such a layer were unpacked using a
     * {@link PixelDouble}, these unsigned byte values would be accurately
     * converted to doubles in the range [0.0, 255.0] without loss of precision.
     *
     * @param band the band from which to load the data
     * @return the data accessor
     * @throws UnsupportedDataTypeException if the underlying type in the GDAL
     *             {@link Band} cannot be read into the layer
     */
    protected DoubleGetter2d loadData(Band band)
    {
        int dt = band.getDataType();
        int xSize = band.getXSize();
        int ySize = band.getYSize();

        DoubleGetter2d dg = null;
        if (dt == gdalconst.GDT_Byte || dt == gdalconst.GDT_Int16)
        {
            short[] array = new short[xSize * ySize];
            band.ReadRaster(0, 0, xSize, ySize, array);
            dg = (x, y) -> {
                return array[y * xSize + x];
            };
        }
        else if (dt == gdalconst.GDT_UInt16 || dt == gdalconst.GDT_Int32)
        {
            int[] array = new int[xSize * ySize];
            band.ReadRaster(0, 0, xSize, ySize, array);
            dg = (x, y) -> {
                return array[y * xSize + x];
            };
        }
        else if (dt == gdalconst.GDT_Float32)
        {
            float[] array = new float[xSize * ySize];
            band.ReadRaster(0, 0, xSize, ySize, array);
            dg = (x, y) -> {
                return array[y * xSize + x];
            };
        }
        else if (dt == gdalconst.GDT_Float64 || dt == gdalconst.GDT_UInt32 || dt == gdalconst.GDT_UInt64 || dt == gdalconst.GDT_Int64)
        {
            double[] array = new double[xSize * ySize];
            band.ReadRaster(0, 0, xSize, ySize, array);
            dg = (x, y) -> {
                return array[y * xSize + x];
            };
        }
        else
        {
            throw new UnsupportedDataTypeException("Cannot represent GDAL data type " + dt + " as a double");
        }

        return dg;
    }

    @Override
    public String toString()
    {
        return getDataSet() != null ? "GDAL layer loader ready" : "GDAL layer loader -- no dataset";
    }
}
