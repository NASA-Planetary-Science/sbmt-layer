package edu.jhuapl.sbmt.layer.gdal;

import java.nio.file.Paths;
import java.util.function.Function;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.image2.pipelineComponents.VTKDebug;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.api.PixelDouble;
import edu.jhuapl.sbmt.layer.api.PixelVector;
import edu.jhuapl.sbmt.layer.impl.LayerTransformFactory;
import edu.jhuapl.sbmt.layer.impl.PixelVectorDoubleFactory;
import edu.jhuapl.sbmt.layer.impl.ValidityChecker2d;

/**
 * This class just demonstrates how to use GDAL to read a data set from a file
 * and turn it into a {@link Layer} instance.
 */
public class SampleGdalUseCase
{
    public static void main(String[] args)
    {
    	NativeLibraryLoader.loadAllVtkLibraries();
        gdal.AllRegister();
        // This is a DART/LICIA/LUKE test image, which is a 3-band UNSIGNED byte
        // image that has both Didymos and Dimorphos visible and fairly large.
        // For a given (i, j), all 3 k-bands have the same pixel value.
        String sampleFile = Paths.get(System.getProperty("user.home"), //
                "Downloads", //
                "liciacube_luke_l0_717506291_294_01.fits").toString();
        String sampleFile2 = Paths.get(System.getProperty("user.home"), //
                "Desktop/SBMT Example Data files/", //
                "Global_20181213_20181201_Shape14_NatureEd.png").toString();

        // Start by pulling the data set out of the file.
        Dataset dataSet = gdal.Open(sampleFile2);
        if (dataSet != null)
        {
            // Create a fake validity checker just to show how to use it. A real
            // checker would most likely check whether value == one or more
            // "invalid" pixel codes, rather than arbitrarily considering pixel
            // (2047, 0) to be invalid.
            ValidityChecker2d vc = (i, j, value) -> {
                return !(i == 2047 && j == 0);
            };

            // This is the main step -- it makes a loader builder, configures it
            // with the data set and validity checker, builds the loader and
            // loads the layer. The layer returned matches the native
            // architecture of the image.
            Layer layer = new LayerLoaderBuilder().dataSet(dataSet).checker(vc).build().load();

            PixelVector factory = new PixelVectorDoubleFactory().of(3, Double.NaN);
            Function<Layer, Layer> transform = new LayerTransformFactory().slice(factory, 2);
            Layer singleLayer = transform.apply(layer);

            try
			{
				VTKDebug.previewLayer(singleLayer, "GDAL Layer test");
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            // See above -- we know this is 3 in this case.
            int kSize = layer.dataSizes().get(0);

            // This is just to demonstrate a feature: it is possible to specify
            // a value to substitute for ALL the invalid/out-of-bounds values.
            // By setting this to NaN, calling code can be simpler because one
            // can get all values and just check whether they are finite before
            // using them to compute anything. VTK knows how to render NaN, so
            // this is probably a good thing to do in general.
            Double indef = Double.NaN;

            // Create a pixel used to read out the values from the layer. Return
            // the indef value for both out-of-bounds and invalid pixels.
            PixelVector pvd = new PixelVectorDoubleFactory().of(kSize, indef, indef);

            System.out.println("Using vector pixel " + pvd + " to retrieve the value");

            // Iterate over rows first.
            for (int j = 0; j < layer.jSize(); ++j)
            {
                boolean printRow = false;
                StringBuilder sb = new StringBuilder();
                String delim = "";

                // Now iterate over columns.
                for (int i = 0; i < layer.iSize(); ++i)
                {
                    // Get the pixel from the layer at this location. The Ftools
                    // and FV seem to show images from bottom-left to top-right,
                    // whereas GDAL goes from top-left to bottom-right. For this
                    // reason, invert the j index to make the results easier to
                    // compare.
                    layer.get(i, layer.jSize() - 1 - j, pvd);
                    double value = ((PixelDouble) pvd.get(kSize - 1)).get();

                    // Only print "bright" pixels: those > 127, to make it
                    // easier to see/compare results. Note that such values
                    // would appear as negative values if they were read using
                    // Java's SIGNED byte type. However, the loader uses shorts
                    // for these values, which have more than enough bits to
                    // handle unsigned 8-bit quantities.
                    if (Double.isFinite(value))
                    {
                        if (value > 127.0)
                        {
                            sb.append(delim);
                            sb.append(String.format("col %4d = %4d", i + 1, (short) value));
                            delim = ", ";
                            printRow = true;
                        }
                    }
                    else
                    {
                        sb.append(delim);
                        sb.append(String.format("col %4d = %4f", i + 1, value));
                        delim = ", ";
                        printRow = true;
                    }
                }

                if (printRow)
                {
                    System.out.printf("Row %4d: %s\n", j + 1, sb.toString());
                }
            }
            layer.get(-1, 0, pvd);
        }
        else
        {
            System.err.printf("Data set returned by gdal.Open(%s) is null\n", sampleFile);
        }
    }

}
