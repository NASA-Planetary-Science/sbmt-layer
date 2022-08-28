package edu.jhuapl.sbmt.layer.gdal;

import edu.jhuapl.sbmt.layer.api.Layer;

/**
 * Extension of {@link RuntimeException} that is thrown if the data type in a
 * GDAL band/data set cannot be used to create a {@link Layer}.
 *
 * @author James Peachey
 *
 */
public class UnsupportedDataTypeException extends RuntimeException
{

    public UnsupportedDataTypeException()
    {
        super();
    }

    public UnsupportedDataTypeException(String message)
    {
        super(message);
    }

    public UnsupportedDataTypeException(Throwable cause)
    {
        super(cause);
    }

    public UnsupportedDataTypeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public UnsupportedDataTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
