package edu.jhuapl.sbmt.layer;

import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.api.Pixel;
import edu.jhuapl.sbmt.layer.impl.PixelDoubleFactory;
import edu.jhuapl.sbmt.layer.impl.PixelVectorDoubleFactory;

public class LayerDebug
{
	protected static final double TestOOBValue = -100.0;
	protected static final PixelDoubleFactory PixelScalarFactory = new PixelDoubleFactory();
	protected static final PixelVectorDoubleFactory PixelVectorFactory = new PixelVectorDoubleFactory();


	public static void displayLayer(String message, Layer layer, int displayKsize, Double invalidValueSubstitute)
    {
        System.out.println("************************************************");
        System.out.println(message);
        System.out.println("1D Fits Test");
        System.out.println("************************************************");

        Pixel pixel = displayKsize == 0 ? //
                PixelScalarFactory.of(0.0, TestOOBValue, invalidValueSubstitute) : //
                PixelVectorFactory.of(displayKsize, TestOOBValue, invalidValueSubstitute);
        display("Loaded layer:", layer, pixel);
        System.out.println();
    }

    /**
     * Display the state of a {@link Layer}, prefaced by a message, and using
     * the specified {@link Pixel} instance to retrieve data from the layer.
     *
     * @param message the message used as a preface
     * @param layer the layer whose state to display
     * @param pixel the pixel used to get data from the layer
     */
    protected static final void display(String message, Layer layer, Pixel pixel)
    {
        System.out.println(message);
        for (int row = -1; row <= layer.jSize(); ++row)
        {
            StringBuilder builder = new StringBuilder();
            String delim = "";
            for (int column = -1; column <= layer.iSize(); ++column)
            {
                layer.get(column, row, pixel);
                builder.append(delim);
                builder.append(pixel);
                delim = ", ";
            }
            System.out.println(builder.toString());
        }
    }
}
