package edu.jhuapl.sbmt.layer.gdal;

import java.nio.file.Paths;
import java.util.List;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.api.PixelDouble;
import edu.jhuapl.sbmt.layer.impl.PixelDoubleFactory;

public class SampleGdalUseCase
{
    public static void main(String[] args)
    {
        gdal.AllRegister();

        String sampleFile = Paths.get(System.getProperty("user.home"), //
                "jhuapl/dev/sbmt/redmine-2356", //
                "liciacube_luke_l0_717506291_294_01.fits").toString();

        Dataset dataSet = gdal.Open(sampleFile);
        if (dataSet != null)
        {
            List<Layer> layers = new LayerLoader() {

                @Override
                protected Dataset getDataSet()
                {
                    return dataSet;
                }

            }.load();

            Layer layer = layers.get(0);
            PixelDouble pd = new PixelDoubleFactory().of(Double.NaN, Double.NaN, Double.NaN);
            for (int j = 0; j < layer.jSize(); ++j)
            {
                boolean printRow = false;
                StringBuilder sb = new StringBuilder();
                String delim = "";
                for (int i = 0; i < layer.iSize(); ++i)
                {
                    layer.get(i, layer.jSize() - 1 - j, pd);
                    double value = pd.get();
                    if ((byte) value < 0)
                    {
                        sb.append(delim);
                        sb.append(String.format("col %4d = %4d", i + 1, (short) value));
                        delim = ", ";
                        printRow = true;
                    }
                }

                if (printRow)
                {
                    System.out.printf("Row %4d: %s\n", j + 1, sb.toString());
                }
            }
        }
        else
        {
            System.err.printf("Data set returned by gdal.Open(%s) is null\n", sampleFile);
        }
    }

}
