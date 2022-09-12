package chloroplastInterface;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import java.beans.PropertyChangeEvent;
import java.beans.IndexedPropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import atomicJ.gui.AbstractModel;
import atomicJ.utilities.StringUtilities;

public class ExperimentDescriptionModel extends AbstractModel implements PhotometricDescription
{
    private final static Preferences PREF = Preferences.userNodeForPackage(ExperimentDescriptionModel.class).node(ExperimentDescriptionModel.class.getName());

    private final static List<String> DEFAULT_PLANT_NAMES = Arrays.asList("", "Arabidopsis thaliana", "Nicotiana tabacum", "Marchantia polymorpha", "Adiantum capillus-veneris","Physcomitrella patens");
    private final static List<String> DEFAULT_ARABIDOPSIS_LINES = Arrays.asList("","WT","phot1","phot2","phot1phot2","jac1","nch1","rpt2","nch1rpt2");
    private final static List<String> DEFAULT_NICOTIANA_TABACUM_LINES = Arrays.asList("","WT");
    private final static List<String> DEFAULT_MARCHANTIA_POLYMORPHA_LINES = Arrays.asList("","WT", "MPphot1KO");
    private final static List<String> DEFAULT_ADIANTUM_CAPILLUS_VENERIS_LINES = Arrays.asList("","WT","phot1","phot2", "phot1phot2","neo1");
    private final static List<String> DEFAULT_PHYSCOMITRELLA_PATENS = Arrays.asList("","WT");

    private final static Map<String, List<String>> DEFAULT_LINES_MAP = new HashMap<>();
    static {
        DEFAULT_LINES_MAP.put("Arabidopsis thaliana", DEFAULT_ARABIDOPSIS_LINES);
        DEFAULT_LINES_MAP.put("Nicotiana tabacum", DEFAULT_NICOTIANA_TABACUM_LINES);
        DEFAULT_LINES_MAP.put("Marchantia polymorpha", DEFAULT_MARCHANTIA_POLYMORPHA_LINES);
        DEFAULT_LINES_MAP.put("Adiantum capillus-veneris", DEFAULT_ADIANTUM_CAPILLUS_VENERIS_LINES);
        DEFAULT_LINES_MAP.put("Physcomitrella patens", DEFAULT_PHYSCOMITRELLA_PATENS);
    }

    public static final String SPECIES_NAME = "SpeciesName";
    public static final String LINE_NAME = "LineName";
    public static final String DARK_ADAPTED = "DarkAdapted";
    public static final String UNIT_TYPE = "UnitType";
    public static final String COMMENTS = "Comments";
    public static final String DESCRIPTION_TEXT = "DescriptionText";


    public static final String ACTINIC_BEAM_IRRADIANCE = "ActinicBeamIrradiance";
    public static final String ACTINIC_BEAM_PHASE_COUNT = "ActinicBeamPhaseCount";

    public static final String NECESSARY_INPUT_PROVIDED = "NecessaryInputProvided";

    private String speciesName = "";
    private String lineName = "";
    private List<Double> lightIntensities;
    private IrradianceUnitType unitType;
    private boolean darkAdapted = true;

    private String comments = "";

    private boolean necessaryInputProvided;

    public ExperimentDescriptionModel()
    {
        this(0);
    }

    public ExperimentDescriptionModel(int actinicBeamPhaseCount)
    {
        this.lightIntensities = new ArrayList<>(Collections.nCopies(actinicBeamPhaseCount, Double.NaN));//we need to copy the list through the constructor, as the nCopies method returns an immutable list

        initDefaults();
    }

    private void initDefaults()
    {
        this.speciesName = PREF.get(SPECIES_NAME, this.speciesName);
        String preferredUnitTypeName = PREF.get(UNIT_TYPE, IrradianceUnitType.MICROMOLES_PER_SQUARE_METER_PER_SECOND.name());
        this.unitType = IrradianceUnitType.getValue(preferredUnitTypeName, IrradianceUnitType.MICROMOLES_PER_SQUARE_METER_PER_SECOND);
    }

    @Override
    public String getSpeciesName()
    {
        return speciesName;
    }

    public void setSpeciesName(String speciesNameNew)
    {        
        if(!Objects.equals(speciesNameNew, this.speciesName))
        {
            String descriptionOld = getDescription();

            String speciesNameOld = this.speciesName;
            this.speciesName = speciesNameNew;

            String descriptionNew = getDescription();

            firePropertyChange(SPECIES_NAME, speciesNameOld, speciesNameNew);
            firePropertyChange(DESCRIPTION_TEXT, descriptionOld, descriptionNew);
            updateWhetherNecessaryInputProvided();

            PREF.put(SPECIES_NAME, this.speciesName);       
            try {
                PREF.flush();
            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
        }                
    }

    public List<String> getSuggestedPlantNames()
    {
        return DEFAULT_PLANT_NAMES;
    }

    public List<String> getSuggestedPlantLineNames()
    {
        if(DEFAULT_LINES_MAP.containsKey(speciesName))
        {
            return Collections.unmodifiableList(DEFAULT_LINES_MAP.get(speciesName));
        }

        return Arrays.asList("","WT");
    }

    @Override
    public String getLineName()
    {
        return lineName;
    }

    public void setLineName(String lineNew)
    {
        if(!Objects.equals(lineNew, this.lineName))
        {
            String descriptionOld = getDescription();

            String lineOld = this.lineName;
            this.lineName = lineNew;
            String descriptionNew = getDescription();

            firePropertyChange(LINE_NAME, lineOld, lineNew);
            firePropertyChange(DESCRIPTION_TEXT, descriptionOld, descriptionNew);

            updateWhetherNecessaryInputProvided();
        }                
    }

    @Override
    public boolean isDarkAdapted()
    {
        return darkAdapted;
    }

    public void setDarkAdapted(boolean darkAdaptedNew)    
    {
        if(this.darkAdapted != darkAdaptedNew)
        {
            String descriptionOld = getDescription();

            boolean darkAdaptedOld = this.darkAdapted;
            this.darkAdapted = darkAdaptedNew;

            String descriptionNew = getDescription();

            firePropertyChange(DARK_ADAPTED, darkAdaptedOld, darkAdaptedNew);
            firePropertyChange(DESCRIPTION_TEXT, descriptionOld, descriptionNew);
        }
    }

    @Override
    public IrradianceUnitType getUnitType()
    {
        return unitType;
    }

    public void setUnitType(IrradianceUnitType unitTypeNew)
    {
        if(!Objects.equals(this.unitType, unitTypeNew))
        {
            String descriptionOld = getDescription();

            IrradianceUnitType unitTypeOld = this.unitType;
            this.unitType = unitTypeNew;

            String descriptionNew = getDescription();

            firePropertyChange(UNIT_TYPE, unitTypeOld, unitTypeNew);
            firePropertyChange(DESCRIPTION_TEXT, descriptionOld, descriptionNew);

            updateWhetherNecessaryInputProvided();

            PREF.put(UNIT_TYPE, this.unitType.name());       
            try {
                PREF.flush();
            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkIfIrradianceValuesSpecified()
    {        
        for(Double intensity : lightIntensities)
        {
            if(Double.isNaN(intensity))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public double getActinicBeamIrradianceValue(int phaseIndex)
    {
        if(phaseIndex >= lightIntensities.size())
        {
            throw new IllegalArgumentException("Value of phase was greater than the maximal index of " + Integer.toString(lightIntensities.size() - 1));
        }

        double intensity = lightIntensities.get(phaseIndex);
        return intensity;
    }

    public void setActinicBeamIrradianceValue(double lightIntensityNew, int phaseIndex)
    {
        if(phaseIndex >= lightIntensities.size())
        {
            throw new IllegalArgumentException("Value of phase was greater than the maximal index of " + Integer.toString(lightIntensities.size() - 1));
        }

        double lightIntensityOld = lightIntensities.get(phaseIndex);
        if(Double.compare(lightIntensityOld, lightIntensityNew) != 0)
        {
            String descriptionOld = getDescription();

            lightIntensities.set(phaseIndex, lightIntensityNew);

            String descriptionNew = getDescription();

            PropertyChangeEvent evt = new IndexedPropertyChangeEvent(this, ACTINIC_BEAM_IRRADIANCE, lightIntensityOld, lightIntensityNew, phaseIndex);
            firePropertyChange(evt);

            firePropertyChange(DESCRIPTION_TEXT, descriptionOld, descriptionNew);

            updateWhetherNecessaryInputProvided();
        }
    }

    protected boolean checkIfNecessaryInputProvided()
    {
        boolean necessaryInputProvided = true;
        necessaryInputProvided = necessaryInputProvided && checkIfIrradianceValuesSpecified();
        necessaryInputProvided = necessaryInputProvided && !StringUtilities.isNullOrEmpty(speciesName);
        necessaryInputProvided = necessaryInputProvided && !StringUtilities.isNullOrEmpty(lineName);

        return necessaryInputProvided;
    }

    @Override
    public String getComments()
    {
        return comments;
    }

    public void setComments(String commentsNew)
    {
        if(!Objects.equals(this.comments, commentsNew))
        {
            String descriptionOld = getDescription();
            String commentsOld = this.comments;
            this.comments = commentsNew;

            String descriptionNew = getDescription();

            firePropertyChange(COMMENTS, commentsOld, commentsNew);
            firePropertyChange(DESCRIPTION_TEXT, descriptionOld, descriptionNew);           
        }
    }

    public boolean isNecessaryInputProvided()
    {
        return necessaryInputProvided;
    }

    public void updateWhetherNecessaryInputProvided()
    {
        boolean necessaryInputProvidedNew = checkIfNecessaryInputProvided();
        if(this.necessaryInputProvided != necessaryInputProvidedNew)
        {
            boolean necessaryInputProvidedOld = this.necessaryInputProvided;
            this.necessaryInputProvided = necessaryInputProvidedNew;

            firePropertyChange(NECESSARY_INPUT_PROVIDED, necessaryInputProvidedOld, necessaryInputProvidedNew);
        }
    }

    @Override
    public int getActinicBeamPhaseCount()
    {
        int phaseCount = lightIntensities.size();
        return phaseCount;
    }

    public void setActinicBeamPhaseCount(int phaseCountNew)
    {
        if(phaseCountNew < 0)
        {
            throw new IllegalArgumentException("Actinic beam phase count cannot be negative");
        }

        int phaseCountOld = lightIntensities.size();

        if(phaseCountOld != phaseCountNew)
        {
            String descriptionOld = getDescription();

            if(phaseCountNew < phaseCountOld)
            {
                this.lightIntensities = this.lightIntensities.subList(0, phaseCountNew);
            }
            else
            {
                this.lightIntensities.addAll(Collections.nCopies(phaseCountNew - phaseCountOld, Double.NaN));
            }
            String descriptionNew = getDescription();            
            firePropertyChange(ACTINIC_BEAM_PHASE_COUNT, phaseCountOld, phaseCountNew);
            firePropertyChange(DESCRIPTION_TEXT, descriptionOld, descriptionNew);

            updateWhetherNecessaryInputProvided();
        }
    }

    public PhotometricDescriptionImmutable getMemento()
    {
        PhotometricDescriptionImmutable memento = new PhotometricDescriptionImmutable(this);
        return memento;
    }

    public void setStateFromMemento(PhotometricDescriptionImmutable memento)
    {
        setSpeciesName(memento.speciesName);
        setLineName(memento.lineName);

        setDarkAdapted(memento.darkAdapted);

        int numberOfIntensitiesToRead = Math.min(lightIntensities.size(), memento.lightIntensities.size());

        for(int i = 0; i<numberOfIntensitiesToRead; i++)
        {
            setActinicBeamIrradianceValue(memento.lightIntensities.get(i), i);
        }

        setComments(memento.comment);
    }

    public static class PhotometricDescriptionImmutable implements PhotometricDescription
    {                
        private final String speciesName;
        private final String lineName;
        private final List<Double> lightIntensities;
        private final IrradianceUnitType unitType;
        private final boolean darkAdapted;
        private final String comment;

        protected PhotometricDescriptionImmutable(ExperimentDescriptionModel model)
        {
            this.speciesName = model.speciesName;
            this.lineName = model.lineName;
            this.lightIntensities = new ArrayList<>(model.lightIntensities);
            this.unitType = model.unitType;
            this.darkAdapted = model.darkAdapted;
            this.comment = model.comments;
        }

        protected PhotometricDescriptionImmutable(String speciesName, String lineName, List<Double> lightIntensities, IrradianceUnitType unitType, boolean darkAdapted, String comments)
        {
            this.speciesName = speciesName;
            this.lineName = lineName;
            this.lightIntensities = new ArrayList<>(lightIntensities);
            this.unitType = unitType;
            this.darkAdapted = darkAdapted;
            this.comment = comments;
        }

        @Override
        public final String getSpeciesName()
        {
            return speciesName;
        }

        @Override
        public final String getLineName()
        {
            return lineName;
        }

        @Override
        public final boolean isDarkAdapted()
        {
            return darkAdapted;
        }

        @Override
        public final IrradianceUnitType getUnitType()
        {
            return unitType;
        }

        @Override
        public final String getComments()
        {
            return comment;
        }

        @Override
        public final int getActinicBeamPhaseCount()
        {
            return lightIntensities.size();
        }

        @Override
        public final double getActinicBeamIrradianceValue(int phaseIndex)
        {
            if(phaseIndex >= lightIntensities.size())
            {
                throw new IllegalArgumentException("Value of phase was greater than the maximal index of " + Integer.toString(lightIntensities.size() - 1));
            }

            double intensity = lightIntensities.get(phaseIndex);
            return intensity;
        }
    }
}
