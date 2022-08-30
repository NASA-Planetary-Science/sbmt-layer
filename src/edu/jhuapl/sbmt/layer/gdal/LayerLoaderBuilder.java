package edu.jhuapl.sbmt.layer.gdal;

import java.util.concurrent.atomic.AtomicReference;

import org.gdal.gdal.Dataset;

import com.google.common.base.Preconditions;

import edu.jhuapl.sbmt.layer.impl.BuilderBase;
import edu.jhuapl.sbmt.layer.impl.ValidityChecker;
import edu.jhuapl.sbmt.layer.impl.ValidityChecker2d;
import edu.jhuapl.sbmt.layer.impl.ValidityChecker3d;

public class LayerLoaderBuilder extends BuilderBase
{

    protected final AtomicReference<Dataset> dataSet;

    public LayerLoaderBuilder()
    {
        super();
        this.dataSet = new AtomicReference<>();
    }

    public LayerLoaderBuilder dataSet(Dataset dataSet)
    {
        set(this.dataSet, dataSet, "Cannot change data set after it is set");

        return this;
    }

    /**
     * This builder accepts either {@link ValidityChecker2d} or
     * {@link ValidityChecker3d} instances. 2-d checkers are converted into 3-d
     * checkers that ignore the k value.
     *
     * @param vc the checker
     * @return the builder
     */
    public LayerLoaderBuilder checker(ValidityChecker vc)
    {
        if (vc instanceof ValidityChecker2d vc2d)
        {
            ValidityChecker3d vc3d = (i, j, k, value) -> {
                return vc2d.isValid(i, j, value);
            };

            vc = vc3d;
        }

        super.setChecker(vc);

        return this;
    }

    public LayerLoader build()
    {
        Dataset dataSet = this.dataSet.get();

        Preconditions.checkState(dataSet != null, "Call setDataSet(...) method before calling build");

        ValidityChecker3d vc = (ValidityChecker3d) checker.get();

        LayerLoader ll;
        if (vc != null)
        {

            ll = new LayerLoader() {

                @Override
                protected Dataset getDataSet()
                {
                    return dataSet;
                }

                @Override
                protected ValidityChecker3d getValidityChecker()
                {
                    return vc;
                }
            };
        }
        else
        {
            ll = new LayerLoader() {

                @Override
                protected Dataset getDataSet()
                {
                    return dataSet;
                }
            };
        }

        return ll;
    }
}
