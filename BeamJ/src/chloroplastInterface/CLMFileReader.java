
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package chloroplastInterface;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.Channel1DStandard;
import atomicJ.data.FlexibleFlatChannel1DData;
import atomicJ.data.units.DimensionlessQuantity;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.utilities.IOUtilities;
import atomicJ.utilities.RomanNumeralConverter;
import chloroplastInterface.ExperimentDescriptionModel.PhotometricDescriptionImmutable;
import chloroplastInterface.optics.SimpleFilter;
import chloroplastInterface.optics.SliderMountedFilter;


public class CLMFileReader extends AbstractSourceReader<SimplePhotometricSource>
{
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"clm"};
    private static final String DESCRIPTION = "Photometric curve (.clm)";

    private static final CLMFileReader INSTANCE = new CLMFileReader();

    private CLMFileReader(){};

    public static final CLMFileReader getInstance()
    {
        return INSTANCE;
    }

    @Override
    public List<SimplePhotometricSource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {
        List<SimplePhotometricSource> sources = new ArrayList<>();

        try(ZipFile zipFile = new ZipFile(f))
        {             
            List<ZipEntry> dataEntries = new ArrayList<>();
            ZipEntry settingEntry = null;
            ZipEntry experimentDescriptionEntry = null;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while(entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                if(entry.isDirectory())
                {
                    continue;
                }
                String entryName = entry.getName();
                String entryExtension = IOUtilities.getExtension(entryName);
                if(CLMSaver.DATA_ENTRY_EXTENSION.equals(entryExtension))
                {
                    dataEntries.add(entry);
                }
                else if(CLMSaver.SETTINGS_ENTRY_NAME_WITH_EXTENSION.equals(entryName))
                {
                    settingEntry = entry;
                }     
                else if(CLMSaver.DESCRIPTION_ENTRY_NAME_WITH_EXTENSION.equals(entryName))
                {
                    experimentDescriptionEntry = entry;
                }      
            }


            if(settingEntry == null)
            {
                throw new UserCommunicableException("No settings entry found");
            }
            Map<String, Dataset1DDescriptionImmutable> datasetDescriptions = new LinkedHashMap<>();
            Map<String, Channel1D> channels = new LinkedHashMap<>();
            List<ActinicPhaseSettingsImmutable> actinicBeamPhaseSettings = new ArrayList<>();

            MeasuringBeamSettingsImmutable measuringBeamSettings = null;
            List<SignalSamplingSettingsImmutable> signalSamplingSettingsAll = new ArrayList<>();

            List<CalibrationSettingsImmutable> calibrationSettingsAll = new ArrayList<>();

            List<SignalSettingsImmutable> signalSettingsAll = new ArrayList<>();

            PhotometricDescriptionImmutable experimentDescription = null;

            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setIgnoringElementContentWhitespace(true);

                DocumentBuilder builder = factory.newDocumentBuilder();

                XPathFactory xpfactory = XPathFactory.newInstance();
                XPath path = xpfactory.newXPath();

                try(InputStream settingEntryIS = zipFile.getInputStream(settingEntry))
                {
                    Document settingsDoc = builder.parse(settingEntryIS);

                    NodeList nodesDatasets = (NodeList)path.evaluate(CLMSaver.getPathFromRootToDatasets(), settingsDoc, XPathConstants.NODESET);

                    for(int i = 0; i<nodesDatasets.getLength();i++)
                    {
                        Node datasetNode = nodesDatasets.item(i);
                        String datasetIdentifier = path.evaluate(CLMSaver.getPathFromDatasetToItsIdentifier(), datasetNode, XPathConstants.STRING).toString();
                        int sampleCount = ((Number)path.evaluate(CLMSaver.getPathFromDatasetToItsSampleCount(), datasetNode, XPathConstants.NUMBER)).intValue();

                        Node xQuantityNode = (Node)path.evaluate(CLMSaver.getPathFromDatasetToXQuantity(), datasetNode, XPathConstants.NODE);
                        
                        System.out.println("XQIAN "+(xQuantityNode == null));
                        
                        String xQuantityName = path.evaluate(CLMSaver.getPathFromDataQuantityToQuantityName(), xQuantityNode, XPathConstants.STRING).toString();
                        String xUnitName = path.evaluate(CLMSaver.getPathFromDataQuantityToQuantityUnit(), xQuantityNode, XPathConstants.STRING).toString();

                        Quantity xQuantity = (xUnitName.trim().isEmpty()) ? new DimensionlessQuantity(xQuantityName): new UnitQuantity(xQuantityName, UnitUtilities.getSIUnit(xUnitName));

                        Node yQuantityNode = (Node)path.evaluate(CLMSaver.getPathFromDatasetToYQuantity(), datasetNode, XPathConstants.NODE);
                        String yQuantityName = path.evaluate(CLMSaver.getPathFromDataQuantityToQuantityName(), yQuantityNode, XPathConstants.STRING).toString();
                        String yUnitName = path.evaluate(CLMSaver.getPathFromDataQuantityToQuantityUnit(), yQuantityNode, XPathConstants.STRING).toString();

                        Quantity yQuantity = (yUnitName.trim().isEmpty()) ? new DimensionlessQuantity(yQuantityName): new UnitQuantity(yQuantityName, UnitUtilities.getSIUnit(yUnitName));

                        Dataset1DDescriptionImmutable datasetDescription = new Dataset1DDescriptionImmutable(datasetIdentifier, sampleCount, xQuantity, yQuantity);
                        datasetDescriptions.put(datasetIdentifier, datasetDescription);    
                    }

                    NodeList nodesActinicBeamPhases = (NodeList)path.evaluate(CLMSaver.getPathFromRootToActinicBeamPhases(), settingsDoc, XPathConstants.NODESET);

                    for(int i = 0; i < nodesActinicBeamPhases.getLength();i++)
                    {
                        Node actinicBeamNode = nodesActinicBeamPhases.item(i);
                        double durationValue = ((Number)path.evaluate(CLMSaver.getPathFromActinicBeamPhaseToDurationValue(), actinicBeamNode, XPathConstants.NUMBER)).doubleValue();
                        String durationUnitName = path.evaluate(CLMSaver.getPathFromActinicBeamPhaseToDurationUnit(), actinicBeamNode, XPathConstants.STRING).toString();
                        StandardTimeUnit durationUnit = StandardTimeUnit.getUnit(durationUnitName);
                        double intensityInPercents = ((Number)path.evaluate(CLMSaver.getPathFromActinicBeamPhaseToIntensityInPercents(), actinicBeamNode, XPathConstants.NUMBER)).doubleValue();
                        String actinicBeamFilterDescription = path.evaluate(CLMSaver.getPathFromActinicBeamPhaseToSliderMountedFilter(), actinicBeamNode, XPathConstants.STRING).toString();

                        int sliderPositionIndex = ((Number)path.evaluate(CLMSaver.getPathFromActinicBeamPhaseToSliderPosition(), actinicBeamNode, XPathConstants.NUMBER)).intValue();

                        SliderMountedFilter currentlyMountedFilter = new SliderMountedFilter(sliderPositionIndex, new SimpleFilter(actinicBeamFilterDescription));

                        ActinicPhaseSettingsImmutable actinicPhaseSettings = new ActinicPhaseSettingsImmutable(durationValue, durationUnit, intensityInPercents, currentlyMountedFilter);
                        actinicBeamPhaseSettings.add(actinicPhaseSettings);
                    }

                    Node nodeMeasuringBeam = (Node)path.evaluate(CLMSaver.getPathFromRootToMeasuringBeam(), settingsDoc, XPathConstants.NODE);
                    double measuringBeamFrequencyInHertz = ((Number)path.evaluate(CLMSaver.getPathFromMeasuringBeamToFrequencyInHertz(), nodeMeasuringBeam, XPathConstants.NUMBER)).doubleValue();
                    double measuringBeamIntensityInPercents = ((Number)path.evaluate(CLMSaver.getPathFromMeasuringBeamToIntensityInPercents(), nodeMeasuringBeam, XPathConstants.NUMBER)).doubleValue();

                    measuringBeamSettings = new MeasuringBeamSettingsImmutable(measuringBeamFrequencyInHertz, measuringBeamIntensityInPercents, true);

                    NodeList signalSettingsNodes = (NodeList)path.evaluate(CLMSaver.getPathFromRootToSignalSettings(), settingsDoc, XPathConstants.NODESET);   

                    for(int i = 0; i < signalSettingsNodes.getLength(); i++)
                    {
                        Node signalSettingsNode = signalSettingsNodes.item(i) ;
                                                
                        String signalTypeName = path.evaluate(CLMSaver.getPathFromSignalSettingsToSignalType(), signalSettingsNode, XPathConstants.STRING).toString();
                        
                        System.err.println("signalTypeName "+signalTypeName);
                        LightSignalType signalType = LightSignalType.valueOf(signalTypeName);                      
                        
                        Node calibrationNode = (Node)path.evaluate(CLMSaver.getPathFromSignalSettingsToCalibration(), signalSettingsNode, XPathConstants.NODE);

                        double calibrationOffsetInVolts = ((Number)path.evaluate(CLMSaver.getPathFromCalibrationToOffsetInVolts(), calibrationNode, XPathConstants.NUMBER)).doubleValue();
                        double calibrationSlopeInPercentsPerVolt = ((Number)path.evaluate(CLMSaver.getPathFromCalibrationToSlopeInPercentsPerVolt(), calibrationNode, XPathConstants.NUMBER)).doubleValue();

                        CalibrationSettingsImmutable calibrationSettings = new CalibrationSettingsImmutable(calibrationSlopeInPercentsPerVolt, calibrationOffsetInVolts);               
                        
                        
                        Node signalSamplingNode = (Node)path.evaluate(CLMSaver.getPathFromSignalSettingsToSignalSampling(), signalSettingsNode, XPathConstants.NODE);
                        double transmittanceSamplesPerMinute = ((Number)path.evaluate(CLMSaver.getPathFromSignalSamplingToSamplesPerMinute(), signalSamplingNode, XPathConstants.NUMBER)).doubleValue();
                        String transmittanceFactoryIdentifier = path.evaluate(CLMSaver.getPathFromSignalSamplingToSourceIdentifier(), signalSamplingNode, XPathConstants.STRING).toString();

                        SignalSamplingSettingsImmutable signalSamplingSettings = new SignalSamplingSettingsImmutable(transmittanceSamplesPerMinute, transmittanceFactoryIdentifier);

                        
                        signalSettingsAll.add(new SignalSettingsImmutable(signalSamplingSettings, calibrationSettings, signalType));

                    }

                } 
                catch (SAXException | XPathExpressionException eSax) 
                {
                    eSax.printStackTrace();
                    throw new UserCommunicableException("Error occured during parsing the settings", eSax);
                } 

                if(experimentDescriptionEntry != null)
                {
                    builder.reset();
                    path.reset();

                    try(InputStream descriptionEntryIS = zipFile.getInputStream(experimentDescriptionEntry))
                    {
                        Document descriptionDoc = builder.parse(descriptionEntryIS);

                        Node plantMaterialNode = (Node)path.evaluate(CLMSaver.getPathFromRootToPlantMaterialElement(), descriptionDoc, XPathConstants.NODE);
                        String speciesName = (path.evaluate(CLMSaver.getPathFromPlantMaterialToSpeciesName(), plantMaterialNode, XPathConstants.STRING)).toString();
                        String lineName = (path.evaluate(CLMSaver.getPathFromPlantMaterialToLineName(), plantMaterialNode, XPathConstants.STRING)).toString();
                        boolean darkAdapted = Boolean.parseBoolean(path.evaluate(CLMSaver.getPathFromPlantMaterialToDarkAdapted(), plantMaterialNode, XPathConstants.STRING).toString());

                        Node experimentDescriptionNode = (Node)path.evaluate(CLMSaver.getPathFromRootToPhotometricExperimentElement(), descriptionDoc, XPathConstants.NODE);
                        IrradianceUnitType intensityUnitType = IrradianceUnitType.getValue(path.evaluate(CLMSaver.getPathFromPhotometricExperimentToLightIntensityUnit(), experimentDescriptionNode, XPathConstants.STRING).toString(),null);
                        NodeList nodesDatasets = (NodeList)path.evaluate(CLMSaver.getPathFromPhotometricExperimentToPhases(), experimentDescriptionNode, XPathConstants.NODESET);

                        List<Double> lightIntensities = new ArrayList<>();
                        for(int i = 0; i<nodesDatasets.getLength();i++)
                        {
                            Node phaseNode = nodesDatasets.item(i);
                            double intensity = ((Number)path.evaluate(CLMSaver.getPathFromDatasetToItsSampleCount(), phaseNode, XPathConstants.NUMBER)).doubleValue();
                            lightIntensities.add(intensity);
                        }

                        String comments = path.evaluate(CLMSaver.getPathFromRootToComments(), descriptionDoc, XPathConstants.STRING).toString(); 

                        experimentDescription = new PhotometricDescriptionImmutable(speciesName, lineName, lightIntensities, intensityUnitType, darkAdapted, comments);
                    } 
                    catch (SAXException | XPathExpressionException eSax) 
                    {
                        eSax.printStackTrace();
                        throw new UserCommunicableException("Error occured during parsing the settings", eSax);
                    } 
                }
            }
            catch (ParserConfigurationException eConfig) 
            {
                eConfig.printStackTrace();
                throw new UserCommunicableException("Error occured during preparations for parsing the settings", eConfig);
            } 

            for(ZipEntry dataEntry : dataEntries)
            {
                String dataEntryName = dataEntry.getName();
                String dataIdentifier = IOUtilities.getBareName(dataEntryName);
                Dataset1DDescriptionImmutable description = datasetDescriptions.get(dataIdentifier);

                int dataLengthInSampleCount = description.getLength();
                int dataLengthInBytes = Double.BYTES*dataLengthInSampleCount;

                try(ReadableByteChannel dataChannel = Channels.newChannel(zipFile.getInputStream(dataEntry))) 
                {
                    ByteBuffer bufferXs = FileInputUtilities.readBytesToBuffer(dataChannel, dataLengthInBytes, CLMSaver.getDataByteOrder());
                    ByteBuffer bufferYs = FileInputUtilities.readBytesToBuffer(dataChannel, dataLengthInBytes, CLMSaver.getDataByteOrder());

                    double[] xs = new double[dataLengthInSampleCount];
                    bufferXs.asDoubleBuffer().get(xs, 0, dataLengthInSampleCount);

                    double[] ys = new double[dataLengthInSampleCount];
                    bufferYs.asDoubleBuffer().get(ys, 0, dataLengthInSampleCount);

                    Channel1DData channelData = new FlexibleFlatChannel1DData(xs, ys, description.getXQuantity(), description.getYQuantity(), SortedArrayOrder.ASCENDING);
                    Channel1D channel = new Channel1DStandard(channelData, dataIdentifier);
                    channels.put(dataIdentifier, channel);
                } 
            }


            SimplePhotometricSource source = new StandardPhotometricSource(f, IOUtilities.getBareName(f), f.getAbsolutePath(), 
                    channels.values(), actinicBeamPhaseSettings, measuringBeamSettings, signalSettingsAll, experimentDescription);
            sources.add(source);
        } catch (IOException e)
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);
        }
        return sources;
    }   

    public List<ActinicPhaseSettingsImmutable> readActinicBeamSettings(File f) throws UserCommunicableException 
    {
        List<ActinicPhaseSettingsImmutable> actinicBeamPhaseSettings = new ArrayList<>();
        OpticsConfiguration currentOpticsConfiguration = ChloroplastJ.CURRENT_FRAME.getOpticsConfiguration();

        try(ZipFile zipFile = new ZipFile(f))
        {             
            ZipEntry settingEntry = null;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while(entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                if(entry.isDirectory())
                {
                    continue;
                }
                String entryName = entry.getName();
                if(CLMSaver.SETTINGS_ENTRY_NAME_WITH_EXTENSION.equals(entryName))
                {
                    settingEntry = entry;
                }        
            }

            if(settingEntry == null)
            {
                throw new UserCommunicableException("No settings entry found");
            }

            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setIgnoringElementContentWhitespace(true);

                DocumentBuilder builder = factory.newDocumentBuilder();

                XPathFactory xpfactory = XPathFactory.newInstance();
                XPath path = xpfactory.newXPath();

                try(InputStream settingEntryIS = zipFile.getInputStream(settingEntry))
                {
                    Document settingsDoc = builder.parse(settingEntryIS);

                    NodeList nodesActinicBeamPhases = (NodeList)path.evaluate(CLMSaver.getPathFromRootToActinicBeamPhases(), settingsDoc, XPathConstants.NODESET);

                    for(int i = 0; i < nodesActinicBeamPhases.getLength();i++)
                    {
                        Node actinicBeamNode = nodesActinicBeamPhases.item(i);
                        double durationValue = ((Number)path.evaluate(CLMSaver.getPathFromActinicBeamPhaseToDurationValue(), actinicBeamNode, XPathConstants.NUMBER)).doubleValue();
                        String durationUnitName = path.evaluate(CLMSaver.getPathFromActinicBeamPhaseToDurationUnit(), actinicBeamNode, XPathConstants.STRING).toString();
                        StandardTimeUnit durationUnit = StandardTimeUnit.getUnit(durationUnitName);
                        double intensityInPercents = ((Number)path.evaluate(CLMSaver.getPathFromActinicBeamPhaseToIntensityInPercents(), actinicBeamNode, XPathConstants.NUMBER)).doubleValue();

                        String actinicBeamFilterDescription = path.evaluate(CLMSaver.getPathFromActinicBeamPhaseToSliderMountedFilter(), actinicBeamNode, XPathConstants.STRING).toString();
                        int sliderPositionIndex = ((Number)path.evaluate(CLMSaver.getPathFromActinicBeamPhaseToSliderPosition(), actinicBeamNode, XPathConstants.NUMBER)).intValue();
                        SliderMountedFilter currentlyMountedFilter = currentOpticsConfiguration.getActinicBeamSliderFilter(sliderPositionIndex);

                        if(!currentlyMountedFilter.canFilterBeDescribedBy(actinicBeamFilterDescription))
                        {
                            Logger.getLogger(CLMFileReader.class.getName()).log(Level.SEVERE, "The filter currently installed in the position "+ RomanNumeralConverter.convertToRoman(sliderPositionIndex + 1)+" does not agree with the filter description found in the read-in file. The description is " + actinicBeamFilterDescription);
                        }

                        ActinicPhaseSettingsImmutable actinicPhaseSettings = new ActinicPhaseSettingsImmutable(durationValue, durationUnit, intensityInPercents, currentlyMountedFilter);
                        actinicBeamPhaseSettings.add(actinicPhaseSettings);
                    }
                } 
                catch (SAXException | XPathExpressionException eSax) 
                {
                    eSax.printStackTrace();
                    throw new UserCommunicableException("Error occured during parsing the settings", eSax);
                } 

            }
            catch (ParserConfigurationException eConfig) 
            {
                eConfig.printStackTrace();
                throw new UserCommunicableException("Error occured during preparations for parsing the settings", eConfig);
            } 

        } catch (IOException e)
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);
        }
        return actinicBeamPhaseSettings;
    }   

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    public static String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    @Override
    public boolean accept(File f) 
    {
        String[] acceptedExtensions = getAcceptedExtensions();

        FileExtensionPatternFilter filter = new FileExtensionPatternFilter(acceptedExtensions);
        boolean accept =  filter.accept(f); 
        return accept;
    }
}