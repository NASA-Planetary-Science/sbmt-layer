package edu.jhuapl.sbmt.layer.impl;

public class ValidityCheckerDoubleFactory
{

    private static final ValidityChecker2d AllValid = (i, j, value) -> {
        return true;
    };;

    public ValidityCheckerDoubleFactory()
    {
        super();
    }

    public ValidityChecker2d checker2d(double... invalidValues)
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

    public ValidityChecker2d allValid() {
        return AllValid;
    }

}
