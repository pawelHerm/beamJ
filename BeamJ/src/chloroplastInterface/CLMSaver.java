package chloroplastInterface;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import atomicJ.data.Channel1D;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.Quantity;
import atomicJ.gui.save.SavingException;
import atomicJ.utilities.StringUtilities;
import chloroplastInterface.ExperimentDescriptionModel.PhotometricDescriptionImmutable;

public class CLMSaver implements Saver<PhotometricResource>
{
    public static final String SETTINGS_ENTRY_NAME_WITH_EXTENSION = "settings.xml";  
    public static final String DESCRIPTION_ENTRY_NAME_WITH_EXTENSION = "description.xml";

    public static final String DATA_ENTRY_EXTENSION = "dat";

    private static final String PHOTOMETRIC_ROOT = "PhotometricRecording";

    private static final String ACTINIC_BEAM_ELEMENT = "ActinicBeam";
    private static final String MEASURING_BEAM_ELEMENT = "MeasuringBeam";

    private static final String FREQUENCY_IN_HERTZ = "FrequencyInHertz";

    private static final String ACTINIC_BEAM_PHASE_ELEMENT = "Phase";
    private static final String DURATION_VALUE_ELEMENT = "DurationValue";
    private static final String DURATION_UNIT_ELEMENT = "DurationUnit";
    private static final String INTENSITY_IN_PERCENTS = "IntensityInPercents";
    private static final String ACTINIC_BEAM_SLIDER_MOUNTED_FILTER = "SelectedSliderMountedFilter";
    private static final String ACTINIC_BEAM_SELECTED_SLIDER_FILTER_POSITION = "FilterSliderPosition";

    private static final String ALL_SIGNAL_SETTINGS_ELEMENT = "AllSignalSettings";
    private static final String SIGNAL_SETTINGS_ELEMENT = "SignalSettings";
    private static final String SIGNAL_TYPE = "SignalType";

    private static final String CALIBRATION_ELEMENT = "CalibrationElement";
    private static final String OFFSET_IN_VOLTS = "OffsetInVolts";
    private static final String SLOPE_PERCENT_PER_VOLT = "OffsetInPercentPerVolt";

    private static final String SIGNAL_SAMPLING_ELEMENT = "SignalSampling";
    private static final String FREQUENCY_PER_MINUTE = "FrequencyPerMinute";
    private static final String SOURCE_IDENTIFIER = "SourceDevice";
        
    private static final String CHANNEL_ELEMENT = "Channel";
    
    private static final String DATA_X_QUANTITY_ELEMENT = "XQuantity";    
    private static final String DATA_Y_QUANTITY_ELEMENT = "YQuantity";
    private static final String DATA_QUANTITY_NAME = "QuantityName";
    private static final String DATA_UNIT = "UnitName";
    
    private static final String DATASET = "Dataset";
    private static final String DATASET_SAMPLE_COUNT = "SampleCount";
    private static final String DATASET_IDENTIFIER = "Identifier";

    private static final String DESCRIPTION_ROOT = "Description";

    private static final String PLANT_MATERIAL_ELEMENT = "PlantMaterial";

    private static final String SPECIES_NAME_ELEMENT = "Species";
    private static final String LINE_NAME_ELEMENT = "Line";
    private static final String DARK_ADAPTED_ELEMENT = "DarkAdapted";

    private static final String PHOTOMETRIC_EXPERIMENT_ELEMENT = "PhotometricExperiment";

    private static final String PHASE_LIST_ELEMENT = "LightIntensityPhases";
    private static final String LIGHT_INTENSITY_UNIT_TYPE_ELEMENT = "LightIntensityUnitType";
    private static final String PHASE_ELEMENT = "Phase";
    private static final String LIGHT_INTENSITY = "LightIntensity";

    public static final String COMMENT_ELEMENT = "Comment";

    private static final NumberFormat FORMAT = NumberFormat.getInstance(Locale.US);


    private static final ByteOrder DATA_BYTE_ORDER = ByteOrder.BIG_ENDIAN;

    private static final CLMSaver SAVER_INSTANCE = new CLMSaver();


    private CLMSaver(){};

    public static CLMSaver getInstance()
    {
        return SAVER_INSTANCE;
    }

    public static ByteOrder getDataByteOrder()
    {
        return DATA_BYTE_ORDER;
    }

    public static String getPathFromRootToDatasets()
    {
        String path = "/"  + PHOTOMETRIC_ROOT + "/" +  DATASET;
        return path;
    }

    public static String getPathFromDatasetToItsIdentifier()
    {
        String path = DATASET_IDENTIFIER;
        return path;
    }

    public static String getPathFromDatasetToItsSampleCount()
    {
        String path =  DATASET_SAMPLE_COUNT;
        return path;
    }

    public static String getPathFromDatasetToXQuantity()
    {
        StringBuilder builder= new StringBuilder(CHANNEL_ELEMENT);
        builder.append("/").append(DATA_X_QUANTITY_ELEMENT);
        
        String path = builder.toString();
        return path;
    }
    
    public static String getPathFromDatasetToYQuantity()
    {
        StringBuilder builder = new StringBuilder(CHANNEL_ELEMENT);
        builder.append("/").append(DATA_Y_QUANTITY_ELEMENT);
        
        String path = builder.toString();
        return path;
    }
    
    public static String getPathFromDataQuantityToQuantityName()
    {
        return DATA_QUANTITY_NAME;
    }
    
    public static String getPathFromDataQuantityToQuantityUnit()
    {
        return DATA_UNIT;
    }
    
    public static String getPathFromRootToActinicBeamPhases()
    {
        StringBuilder builder = new StringBuilder("/");
        builder.append(PHOTOMETRIC_ROOT).append("/")
        .append(ACTINIC_BEAM_ELEMENT).append("/").append(ACTINIC_BEAM_PHASE_ELEMENT);

        String path = builder.toString();

        return path;
    }

    public static String getPathFromActinicBeamPhaseToDurationValue()
    {
        String path = DURATION_VALUE_ELEMENT;

        return path;
    }

    public static String getPathFromActinicBeamPhaseToDurationUnit()
    {
        String path = DURATION_UNIT_ELEMENT;      
        return path;
    }

    public static String getPathFromActinicBeamPhaseToIntensityInPercents()
    {
        String path = INTENSITY_IN_PERCENTS;
        return path;
    }

    public static String getPathFromActinicBeamPhaseToSliderMountedFilter()
    {
        String path = ACTINIC_BEAM_SLIDER_MOUNTED_FILTER;
        return path;
    }

    public static String getPathFromActinicBeamPhaseToSliderPosition()
    {
        String path = ACTINIC_BEAM_SELECTED_SLIDER_FILTER_POSITION;
        return path;
    }

    public static String getPathFromRootToMeasuringBeam()
    {
        StringBuilder builder = new StringBuilder("/");
        builder.append(PHOTOMETRIC_ROOT).append("/").append(MEASURING_BEAM_ELEMENT);

        String path = builder.toString();

        return path;
    }

    public static String getPathFromMeasuringBeamToFrequencyInHertz()
    {
        String path = FREQUENCY_IN_HERTZ;
        return path;
    }

    public static String getPathFromMeasuringBeamToIntensityInPercents()
    {
        String path = INTENSITY_IN_PERCENTS;
        return path;
    }

    public static String getPathFromSignalSamplingToSamplesPerMinute()
    {
        String path = FREQUENCY_PER_MINUTE;
        return path;
    }

    public static String getPathFromSignalSamplingToSourceIdentifier()
    {
        String path = SOURCE_IDENTIFIER;
        return path;
    }

    public static String getPathFromRootToSignalSettings()
    {
        StringBuilder builder = new StringBuilder("/").append(PHOTOMETRIC_ROOT).append("/").append(ALL_SIGNAL_SETTINGS_ELEMENT).append("/").append(SIGNAL_SETTINGS_ELEMENT);

        String path = builder.toString();
        return path;
    }

    public static String getPathFromSignalSettingsToCalibration()
    {
        return CALIBRATION_ELEMENT;
    }
    
    public static String getPathFromSignalSettingsToSignalSampling()
    {
        return SIGNAL_SAMPLING_ELEMENT;
    }
    
    public static String getPathFromSignalSettingsToSignalType()
    {
        return SIGNAL_TYPE;
    }
    
    public static String getPathFromCalibrationToOffsetInVolts()
    {
        String path = OFFSET_IN_VOLTS;
        return path;
    }

    public static String getPathFromCalibrationToSlopeInPercentsPerVolt()
    {
        String path = SLOPE_PERCENT_PER_VOLT;
        return path;
    }

    public static String getPathFromRootToPlantMaterialElement()
    {
        StringBuilder builder = new StringBuilder("/").append(DESCRIPTION_ROOT).append("/").append(PLANT_MATERIAL_ELEMENT);
        String path = builder.toString();
        return path;
    }

    public static String getPathFromPlantMaterialToSpeciesName()
    {
        return SPECIES_NAME_ELEMENT;
    }

    public static String getPathFromPlantMaterialToLineName()
    {
        return LINE_NAME_ELEMENT;
    }

    public static String getPathFromPlantMaterialToDarkAdapted()
    {
        return DARK_ADAPTED_ELEMENT;
    }

    public static String getPathFromRootToPhotometricExperimentElement()
    {
        StringBuilder builder = new StringBuilder("/").append(DESCRIPTION_ROOT).append("/").append(PHOTOMETRIC_EXPERIMENT_ELEMENT);
        String path = builder.toString();
        return path;
    }

    public static String getPathFromPhotometricExperimentToLightIntensityUnit()
    {
        StringBuilder builder = new StringBuilder(PHASE_LIST_ELEMENT).append("/").append(LIGHT_INTENSITY_UNIT_TYPE_ELEMENT);

        String path = builder.toString();
        return path;
    }

    public static String getPathFromPhotometricExperimentToPhases()
    {
        StringBuilder builder = new StringBuilder(PHASE_LIST_ELEMENT).append("/").append(PHASE_ELEMENT);

        String path = builder.toString();
        return path;
    }

    public static String getPathFromRootToComments()
    {
        StringBuilder builder = new StringBuilder("/").append(DESCRIPTION_ROOT).append("/").append(COMMENT_ELEMENT);
        String path = builder.toString();
        return path;
    }

    @Override
    public void save(PhotometricResource resource, File f) throws SavingException
    {   
        SimplePhotometricSource sourceToSave = resource.getSource();
        
        Map<String, String> env = new HashMap<>(); 
        env.put("create", "true");

        URI uri = URI.create("jar:" + f.toURI());
        try(FileSystem fs = FileSystems.newFileSystem(uri, env);)
        {
            List<? extends Channel1D> channels = sourceToSave.getChannels();

            for(Channel1D dataChannel : channels)
            {
                OutputStream outData = Files.newOutputStream(fs.getPath(dataChannel.getIdentifier() + "." + DATA_ENTRY_EXTENSION), StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                saveDataAsBinary(outData, dataChannel);
                outData.close();
            }
            
            OutputStream outSettings = Files.newOutputStream(fs.getPath(SETTINGS_ENTRY_NAME_WITH_EXTENSION), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            saveSettingsInXML(outSettings, sourceToSave);
            outSettings.close();

            OutputStream outDecription = Files.newOutputStream(fs.getPath(DESCRIPTION_ENTRY_NAME_WITH_EXTENSION), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            saveDescriptionInXML(outDecription, sourceToSave.getDescription());
            outDecription.close();

        } catch (IOException | ParserConfigurationException | XMLStreamException e) 
        {
            throw new SavingException("Exception encountered by CLMSaver", e);
        }
    }

    @Override
    public void writeToStream(PhotometricResource resource, OutputStream out) throws SavingException 
    { 
        try(ZipOutputStream zipStream = new ZipOutputStream(new BufferedOutputStream(out))) 
        {           
            SimplePhotometricSource sourceToSave = resource.getSource();
            List<? extends Channel1D> channels = sourceToSave.getChannels();

            for(Channel1D dataChannel : channels)
            {
                ZipEntry dataChannelEntry = new ZipEntry(dataChannel.getIdentifier() + "." + DATA_ENTRY_EXTENSION);
                zipStream.putNextEntry(dataChannelEntry);
                saveDataAsBinary(zipStream, dataChannel);
                zipStream.closeEntry();
            }

            ZipEntry settingsEntry = new ZipEntry(SETTINGS_ENTRY_NAME_WITH_EXTENSION);
            zipStream.putNextEntry(settingsEntry);
            saveSettingsInXML(zipStream, sourceToSave);
            zipStream.closeEntry();

            PhotometricDescriptionImmutable description = sourceToSave.getDescription();

            ZipEntry descriptionEntry = new ZipEntry(DESCRIPTION_ENTRY_NAME_WITH_EXTENSION);
            zipStream.putNextEntry(descriptionEntry);
            saveDescriptionInXML(zipStream, description);
            zipStream.closeEntry();

        } catch (IOException | ParserConfigurationException | XMLStreamException e) 
        {
            throw new SavingException("Exception encountered by CLMSaaver", e);
        }       
    }

    private void saveDescriptionInXML(OutputStream out, PhotometricDescriptionImmutable description) throws XMLStreamException
    {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(out,  StandardCharsets.UTF_8.name());
        writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");

        writer.writeStartElement(DESCRIPTION_ROOT);

        writer.writeStartElement(PLANT_MATERIAL_ELEMENT);

        writer.writeStartElement(SPECIES_NAME_ELEMENT);
        writer.writeCharacters(StringUtilities.convertToEmptyIfNull(description.getSpeciesName()));
        writer.writeEndElement();

        writer.writeStartElement(LINE_NAME_ELEMENT);
        writer.writeCharacters(StringUtilities.convertToEmptyIfNull(description.getLineName()));
        writer.writeEndElement();

        writer.writeStartElement(DARK_ADAPTED_ELEMENT);
        writer.writeCharacters(Boolean.toString(description.isDarkAdapted()));
        writer.writeEndElement();

        writer.writeEndElement(); //end of PLANT_MATERIAL_ELEMENT

        writer.writeStartElement(PHOTOMETRIC_EXPERIMENT_ELEMENT);

        writer.writeStartElement(PHASE_LIST_ELEMENT);
        writer.writeStartElement(LIGHT_INTENSITY_UNIT_TYPE_ELEMENT);
        writer.writeCharacters(description.getUnitType().name());
        writer.writeEndElement();

        int phaseCount = description.getActinicBeamPhaseCount();
        for(int phaseIndex = 0; phaseIndex<phaseCount;phaseIndex++)
        {
            writer.writeStartElement(PHASE_ELEMENT);

            writer.writeStartElement(LIGHT_INTENSITY);
            writer.writeCharacters(FORMAT.format(description.getActinicBeamIrradianceValue(phaseIndex)));
            writer.writeEndElement();

            writer.writeEndElement();//end of PHASE_ELEMENT
        }

        writer.writeEndElement();//end of PHASE_LIST_ELEMENT

        writer.writeEndElement();//end of PHOTOMETRIC_EXPERIMENT_ELEMENT            

        writer.writeStartElement(COMMENT_ELEMENT);
        writer.writeCharacters(description.getComments());
        writer.writeEndElement();

        writer.writeEndElement(); //end of DESCRIPTION_ELEMENT

        writer.writeEndDocument();
        writer.close();//does not close the underlying output stream
    }

    private void saveDataAsBinary(OutputStream out, Channel1D data) throws IOException
    {
        WritableByteChannel writeChannel = Channels.newChannel(out);

        double[][] xyPointsView = data.getChannelData().getXYView();
        ByteBuffer byteBufferX = ByteBuffer.allocate(Double.BYTES*data.getItemCount());
        byteBufferX.order(DATA_BYTE_ORDER);

        byteBufferX.asDoubleBuffer().put(xyPointsView[0]);
        while(byteBufferX.hasRemaining())
        {
            writeChannel.write(byteBufferX);
        }

        ByteBuffer byteBufferY = ByteBuffer.allocate(Double.BYTES*data.getItemCount());
        byteBufferY.order(DATA_BYTE_ORDER);
        byteBufferY.asDoubleBuffer().put(xyPointsView[1]);
        while(byteBufferY.hasRemaining())
        {
            writeChannel.write(byteBufferY);
        }
    }

    private void saveSettingsInXML(OutputStream out, SimplePhotometricSource source) throws ParserConfigurationException, XMLStreamException
    {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(out,  StandardCharsets.UTF_8.name());
        writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");

        writer.writeStartElement(PHOTOMETRIC_ROOT);        
        
        writeDataChannelDescriptions(writer, source.getChannels());
        writeActinicBeamElement(writer, source.getActinicBeamPhaseSettings());
        writeMeasuringBeamElement(writer, source.getMeasuringBeamSettings());
        writeSignalSettingsElements(writer, source);

        writer.writeEndElement();

        writer.writeEndDocument();
        writer.close();//does not close the underlying output stream
    }

    private void writeDataChannelDescriptions(XMLStreamWriter writer, List<? extends Channel1D> dataChannels) throws XMLStreamException
    {
        for(Channel1D dataChannel : dataChannels)
        {
            writer.writeStartElement(DATASET);

            writer.writeStartElement(DATASET_IDENTIFIER);
            writer.writeCharacters(dataChannel.getIdentifier());
            writer.writeEndElement();

            writer.writeStartElement(DATASET_SAMPLE_COUNT);
            writer.writeCharacters(Integer.toString(dataChannel.getItemCount()));
            writer.writeEndElement();
            
            writeSingleChannelDataQuantities(writer, dataChannel);
            
            writer.writeEndElement();
        }
    }

    private void writeActinicBeamElement(XMLStreamWriter writer, List<ActinicPhaseSettingsImmutable> actinicBeamPhases) throws XMLStreamException
    {
        writer.writeStartElement(ACTINIC_BEAM_ELEMENT);
        for(ActinicPhaseSettingsImmutable phase : actinicBeamPhases)
        {
            writeActinicBeamPhaseElement(writer, phase);
        }

        writer.writeEndElement();
    }

    private void writeActinicBeamPhaseElement(XMLStreamWriter writer, ActinicPhaseSettingsImmutable phase) throws XMLStreamException
    {
        writer.writeStartElement(ACTINIC_BEAM_PHASE_ELEMENT);

        writer.writeStartElement(DURATION_VALUE_ELEMENT);
        writer.writeCharacters(Double.toString(phase.getDuration()));
        writer.writeEndElement();

        writer.writeStartElement(DURATION_UNIT_ELEMENT);
        writer.writeCharacters(phase.getDurationUnit().name());
        writer.writeEndElement();

        writer.writeStartElement(INTENSITY_IN_PERCENTS);
        writer.writeCharacters(Double.toString(phase.getBeamIntensityInPercent()));
        writer.writeEndElement();

        writer.writeStartElement(ACTINIC_BEAM_SELECTED_SLIDER_FILTER_POSITION);
        writer.writeCharacters(Integer.toString(phase.getFilter().getPositionIndex()));
        writer.writeEndElement();

        writer.writeStartElement(ACTINIC_BEAM_SLIDER_MOUNTED_FILTER);
        writer.writeCharacters(phase.getFilter().getDescription());
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void writeMeasuringBeamElement(XMLStreamWriter writer, MeasuringBeamSettingsImmutable measuringBeamSettings) throws XMLStreamException
    {
        writer.writeStartElement(MEASURING_BEAM_ELEMENT);

        writer.writeStartElement(FREQUENCY_IN_HERTZ);
        writer.writeCharacters(Double.toString(measuringBeamSettings.getMeasuringBeamFrequencyInHertz()));
        writer.writeEndElement();

        writer.writeStartElement(INTENSITY_IN_PERCENTS);
        writer.writeCharacters(Double.toString(measuringBeamSettings.getMeasuringBeamMaxIntensityInPercent()));
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void writeSignalSettingsElements(XMLStreamWriter writer, SimplePhotometricSource source) throws XMLStreamException
    {
        int signalCount = source.getRecordedSignalCount();
        
        writer.writeStartElement(ALL_SIGNAL_SETTINGS_ELEMENT);

        for(int signalIndex = 0; signalIndex < signalCount; signalIndex++)
        {
            writer.writeStartElement(SIGNAL_SETTINGS_ELEMENT);

            writeSignalTypeElement(writer, source.getSignalType(signalIndex));
            writeCalibrationElement(writer, source.getCalibrationSettings(signalIndex));
            writeSignalSamplingElement(writer, source.getSignalSettings(signalIndex));
            
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private void writeSignalTypeElement(XMLStreamWriter writer, LightSignalType signalType) throws XMLStreamException
    {
        writer.writeStartElement(SIGNAL_TYPE);
        
        writer.writeCharacters(signalType.name());

        writer.writeEndElement();
    }
    
    private void writeCalibrationElement(XMLStreamWriter writer, CalibrationSettingsImmutable calibrationSettings) throws XMLStreamException
    {
        writer.writeStartElement(CALIBRATION_ELEMENT);

        writer.writeStartElement(OFFSET_IN_VOLTS);
        writer.writeCharacters(Double.toString(calibrationSettings.getCalibrationOffsetInVolts()));
        writer.writeEndElement();

        writer.writeStartElement(SLOPE_PERCENT_PER_VOLT);
        writer.writeCharacters(Double.toString(calibrationSettings.getCalibrationSlopeInPercentsPerVolt()));
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void writeSignalSamplingElement(XMLStreamWriter writer, SignalSamplingSettingsImmutable signalSamplingSettings) throws XMLStreamException
    {
        writer.writeStartElement(SIGNAL_SAMPLING_ELEMENT);

        writer.writeStartElement(FREQUENCY_PER_MINUTE);
        writer.writeCharacters(Double.toString(signalSamplingSettings.getSignalSamplesPerMinute()));
        writer.writeEndElement();

        writer.writeStartElement(SOURCE_IDENTIFIER);
        writer.writeCharacters(signalSamplingSettings.getSignalFactoryIdentifier());
        writer.writeEndElement();

        writer.writeEndElement();
    }

    
    private void writeSingleChannelDataQuantities(XMLStreamWriter writer, Channel1D channel)  throws XMLStreamException
    {
        writer.writeStartElement(CHANNEL_ELEMENT);
              
        Quantity xQuantity = channel.getXQuantity();
        PrefixedUnit xUnit = xQuantity.getUnit();

        writer.writeStartElement(DATA_X_QUANTITY_ELEMENT);
        
        writer.writeStartElement(DATA_QUANTITY_NAME);
        writer.writeCharacters(xQuantity.getName());
        writer.writeEndElement();

        writer.writeStartElement(DATA_UNIT);
        writer.writeCharacters(xUnit.getFullName());
        writer.writeEndElement();

        writer.writeEndElement();       
        
               
        Quantity yQuantity = channel.getYQuantity();
        PrefixedUnit yUnit = yQuantity.getUnit();
        
        writer.writeStartElement(DATA_Y_QUANTITY_ELEMENT);
        
        writer.writeStartElement(DATA_QUANTITY_NAME);
        writer.writeCharacters(yQuantity.getName());
        writer.writeEndElement();

        writer.writeStartElement(DATA_UNIT);
        writer.writeCharacters(yUnit.getFullName());
        writer.writeEndElement();
        
        writer.writeEndElement();
        
        
        
        writer.writeEndElement();
    }
}
