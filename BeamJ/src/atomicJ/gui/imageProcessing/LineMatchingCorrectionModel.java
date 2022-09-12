package atomicJ.gui.imageProcessing;

import org.jfree.util.ObjectUtilities;

import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.LineMatchingCorrection;
import atomicJ.imageProcessing.LocationMeasure;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;


public class LineMatchingCorrectionModel extends ImageBatchROIProcessingModel
{
    public static final String LOCATION_MEASURE = "LocationMeasure";
    public static final String IMAGE_LINE_ORIENTATION = "ImageLineOrientation";
    public static final String MINIMAL_LINE_LENGTH_PERCENT = "MinimalLineWidthPercent";

    private LocationMeasure locationMeasure = LocationMeasure.MEDIAN;

    private double minimalLineLengthPercent = 5;
    private ImageLineOrientation lineOrientation = ImageLineOrientation.HORIZONTAL;

    public LineMatchingCorrectionModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter)
    {
        super(manager, channelFilter, true, false);
    }

    public LocationMeasure getLocationMeasure()
    {
        return locationMeasure;
    }

    public void setLocationMeasure(LocationMeasure measureNew)
    {
        if(!ObjectUtilities.equal(locationMeasure, measureNew))
        {
            LocationMeasure measureOld = this.locationMeasure;
            this.locationMeasure = measureNew;

            firePropertyChange(LOCATION_MEASURE, measureOld, measureNew);

            checkIfApplyEnabled();

            updatePreview();
        }
    }

    public ImageLineOrientation getLineOrientation()
    {
        return lineOrientation;
    }

    public void setLineOrientation(ImageLineOrientation orientationNew)
    {
        if(!ObjectUtilities.equal(this.lineOrientation, orientationNew))
        {
            ImageLineOrientation orientationOld = this.lineOrientation;
            this.lineOrientation = orientationNew;

            firePropertyChange(IMAGE_LINE_ORIENTATION, orientationOld, orientationNew);

            checkIfApplyEnabled();         
            updatePreview();
        }
    }

    public double getMinimalLineLengthPercent()
    {
        return minimalLineLengthPercent;
    }

    public void setMinimalLineLengthPercent(double minimalLineLengthPercentNew)
    {
        if(minimalLineLengthPercentNew < 0 || minimalLineLengthPercentNew >100)
        {
            throw new IllegalArgumentException("minimalLineLengthPercentNew' must be a real number between 0 and 100 inclusive");
        }

        if(Double.compare(this.minimalLineLengthPercent, minimalLineLengthPercentNew) != 0)
        {
            double minimalLineLengthPercentOld = this.minimalLineLengthPercent;
            this.minimalLineLengthPercent = minimalLineLengthPercentNew;

            firePropertyChange(MINIMAL_LINE_LENGTH_PERCENT, minimalLineLengthPercentOld, minimalLineLengthPercentNew);

            checkIfApplyEnabled();
            updatePreview();
        }
    }

    @Override
    protected Channel2DDataInROITransformation buildTransformation()
    {
        if(!isApplyEnabled())
        {
            return null;
        }

        boolean columnWiseCorrection = ImageLineOrientation.VERTICAL.equals(lineOrientation);
        Channel2DDataInROITransformation tr = new LineMatchingCorrection(locationMeasure, 0.01*minimalLineLengthPercent, columnWiseCorrection);

        return tr;
    }

    protected boolean calculateIfApplyEnabled()
    {
        boolean applyEnabled = super.calculateApplyEnabled() && ((locationMeasure != null) && (lineOrientation != null)
                && !Double.isNaN(minimalLineLengthPercent));

        return applyEnabled;
    }
}
