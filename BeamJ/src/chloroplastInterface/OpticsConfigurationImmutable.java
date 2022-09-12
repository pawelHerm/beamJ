package chloroplastInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import atomicJ.utilities.Validation;
import chloroplastInterface.optics.SliderMountedFilter;

public class OpticsConfigurationImmutable implements OpticsConfiguration
{
    private final List<SliderMountedFilter> sliderFilters;

    public OpticsConfigurationImmutable(List<SliderMountedFilter> sliderFilters)
    {
        this.sliderFilters = new ArrayList<>(sliderFilters);
    }

    @Override
    public int getActinicBeamSliderMountedFilterCount() 
    {
        return sliderFilters.size();
    }

    @Override
    public SliderMountedFilter getActinicBeamSliderFilter(int filterIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(filterIndex, 0, sliderFilters.size() - 1, "filterIndex");
        return sliderFilters.get(filterIndex);
    }

    @Override
    public List<SliderMountedFilter> getAvailableActinicBeamSliderMountedFilters()
    {
        List<SliderMountedFilter> filters = Collections.unmodifiableList(sliderFilters);
        return filters;
    }
}
