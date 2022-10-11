package edu.jhuapl.sbmt.layer.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.layer.api.KeyValue;
import edu.jhuapl.sbmt.layer.api.KeyValueCollection;

public class ImmutableKeyValueCollection implements KeyValueCollection
{
    private static final ImmutableKeyValueCollection EmptyCollection = new ImmutableKeyValueCollection(ImmutableList.of());

    public static ImmutableKeyValueCollection of()
    {
        return EmptyCollection;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private final List<KeyValue> allItemsBuilder;

        private Builder()
        {
            this.allItemsBuilder = new ArrayList<>();
            new LinkedHashMap<>();
        }

        public Builder add(String key, String value)
        {
            return add(new KeyValueCaseInsensitive() {

                @Override
                public String key()
                {
                    return key;
                }

                @Override
                public String value()
                {
                    return value;
                }

            });
        }

        public Builder add(KeyValue item)
        {
            if (item != null)
            {
                this.allItemsBuilder.add(item);
            }

            return this;
        }

        public Builder addAll(Iterable<? extends KeyValue> items)
        {
            if (items != null)
            {
                for (KeyValue item : items)
                {
                    if (item != null)
                    {
                        this.allItemsBuilder.add(item);
                    }
                }
            }

            return this;
        }

        public ImmutableKeyValueCollection build()
        {
            return new ImmutableKeyValueCollection(ImmutableList.copyOf(allItemsBuilder));
        }

        @Override
        public String toString()
        {
            return build().toString();
        }

    }

    private final ImmutableList<KeyValue> allItems;

    public ImmutableKeyValueCollection(ImmutableList<KeyValue> allItems)
    {
        super();

        this.allItems = allItems;
    }

    @Override
    public final int size()
    {
        return allItems.size();
    }

    @Override
    public final KeyValue get(int i)
    {
        return allItems.get(i);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        String delim = "";
        for (KeyValue item : allItems)
        {
            sb.append(delim);
            sb.append(item);
            delim = "\n";
        }

        return sb.toString();
    }

}
