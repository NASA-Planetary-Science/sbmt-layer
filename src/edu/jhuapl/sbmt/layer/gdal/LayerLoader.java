package edu.jhuapl.sbmt.layer.gdal;

import java.util.List;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdalconst.gdalconst;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.api.PixelDouble;
import edu.jhuapl.sbmt.layer.impl.DoubleGetter2d;
import edu.jhuapl.sbmt.layer.impl.LayerDoubleBuilder;

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
    public List<Layer> load()
    {
        Dataset dataSet = getDataSet();
        int numBands = dataSet.GetRasterCount();

        ImmutableList.Builder<Layer> builder = ImmutableList.builder();
        for (int bandIndex = 0; bandIndex < numBands; ++bandIndex)
        {
            Band band = dataSet.GetRasterBand(bandIndex + 1);

            if (band != null)
            {
                builder.add(createLayer(band));
            }
        }

        return builder.build();
    }

    /**
     * Load one or more {@link Layer}s from the specified GDAL {@link Band}.
     * <p>
     * The base implementation in {@link LayerLoader} uses a
     * {@link LayerDoubleBuilder} to create a scalar implementation of
     * {@link Layer} from the (entire) band, which is read at one time into an
     * array of the smallest Java primitve data type that can accommodate the
     * native data type contained in the band, as specified by the return value
     * of {@link Band#getDataType()}. The sign of the resulting values will be
     * correct, but 64-bit integer types will suffer a loss of precision if they
     * are converted/read using {@link PixelDouble}.
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
     * @param band the band used to create the layer
     * @return the loaded layer
     * @throws UnsupportedDataTypeException if the underlying type in the GDAL
     *             {@link Band} cannot be read into the layer
     */
    protected Layer createLayer(Band band)
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

        LayerDoubleBuilder b = new LayerDoubleBuilder();
        b.doubleGetter(dg, xSize, ySize);

        return b.build();
    }

}
