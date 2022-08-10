package edu.jhuapl.sbmt.layer.impl;

import edu.jhuapl.sbmt.layer.impl.DoubleBuilderBase.ScalarValidityChecker;

public class ValidityCheckerDoubleFactory
{

    private static final ScalarValidityChecker AllValid = (i, j, value) -> {
        return true;
    };;

    public ValidityCheckerDoubleFactory()
    {
        super();
    }

    public ScalarValidityChecker scalar(double... invalidValues)
    {

        return (i, j, value) -> {
            for (double invalidValue : invalidValues)
            {
                if (Double.compare(value, invalidValue) == 0)
                {
                    return false;
                }
            }

            return true;
        };

    }

    public ScalarValidityChecker allValid() {
        return AllValid;
    }

}
