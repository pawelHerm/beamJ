package chloroplastInterface;

import java.util.List;

import atomicJ.geometricSets.RealSet;

public interface FrequencyDependentController 
{
    public boolean isFunctional();
    public boolean requiresDevice(String deviceIdentifier);
    public boolean shouldBeReplacedWhenOtherControllerFound();
    public String getUniqueDescription();

    public boolean isFrequencySupported(double desiredFrequencyInHertz);
    public boolean isSetOfSupportedFrequenciesDiscrete();
    //returns an empty list if the set of supported frequencies is not discrete
    public List<Double> getListOfSupportedDiscreteFrequenciesInHertzInAscendingOrder();
    //returns a new list, containing those elements of the list passed as the parameter which correspond to the supported frequencies
    public List<Double> selectSupportedDiscreteFrequencies(List<Double> proposedFrequencies);

    public double getClosestSupportedFrequency(double desiredFrequencyInHertz);
    public double getMaximalSupportedFrequencyInHertz();
    public RealSet getSupportedFrequencies();
    public double getPreferredFrequencyDecrement(double currentFrequency);
    public double getPreferredFrequencyIncrement(double currentFrequency);
}
