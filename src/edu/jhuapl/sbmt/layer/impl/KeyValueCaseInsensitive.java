package edu.jhuapl.sbmt.layer.impl;

import java.util.Objects;

import edu.jhuapl.sbmt.layer.api.KeyValue;

public abstract class KeyValueCaseInsensitive implements KeyValue
{

    protected KeyValueCaseInsensitive()
    {
        super();
    }

    @Override
    public final int hashCode()
    {
        return Objects.hash(key().toLowerCase(), value().toLowerCase());
    }

    @Override
    public final boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof KeyValueCaseInsensitive other)
        {
            return key().equalsIgnoreCase(other.key()) && value().equalsIgnoreCase(other.value());
        }
        return false;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(key()).append(" = ").append(value());

        return sb.toString();
    }

}
