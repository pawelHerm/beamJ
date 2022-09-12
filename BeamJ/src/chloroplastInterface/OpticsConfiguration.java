package chloroplastInterface;

import java.util.List;

import chloroplastInterface.optics.SliderMountedFilter;

public interface OpticsConfiguration 
{
    public int getActinicBeamSliderMountedFilterCount();
    public SliderMountedFilter getActinicBeamSliderFilter(int filterIndex);
    public List<SliderMountedFilter> getAvailableActinicBeamSliderMountedFilters();
}
