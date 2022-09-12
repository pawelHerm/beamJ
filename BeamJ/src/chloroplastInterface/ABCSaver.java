package chloroplastInterface;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import atomicJ.gui.save.SavingException;
import chloroplastInterface.ActinicBeamManualCalibrationModel.ActinicBeamCalibrationImmutable;

public class ABCSaver implements Saver<ActinicBeamCalibrationImmutable>
{    
    private static final String ACTINIC_BEAM_CALIBRATION_ROOT = "ActnicBeamCalibration";

    private static final String LIGHT_INTENSITY_UNIT_TYPE_ELEMENT = "LightIntensityUnitType";

    private static final String ACTINIC_BEAM_CALIBRATION_POINT_LIST_ELEMENT = "CalibrationPointList";
    private static final String ACTINIC_BEAM_CALIBRATION_POINT_ELEMENT = "CalibrationPoint";

    private static final String ACTINIC_BEAM_SLIDER_MOUNTED_FILTER = "SelectedSliderMountedFilter";
    private static final String ACTINIC_BEAM_SELECTED_SLIDER_FILTER_POSITION = "FilterSliderPosition";

    private static final String INTENSITY_IN_ABSOLUTE_UNIT = "IntensityInAbsoluteUnit";
    private static final String INTENSITY_IN_PERCENTS = "IntensityInPercents";

    private static final ABCSaver SAVER_INSTANCE = new ABCSaver();

    private ABCSaver(){};

    public static ABCSaver getInstance()
    {
        return SAVER_INSTANCE;
    }

    public static String getPathFromRootToIrradianceUnitTypeElement()
    {
        StringBuilder builder = new StringBuilder("/").append(ACTINIC_BEAM_CALIBRATION_ROOT).append("/").append(LIGHT_INTENSITY_UNIT_TYPE_ELEMENT);
        String path = builder.toString();
        return path;
    }

    public static String getPathFromRootToCalibrationPoint()
    {
        StringBuilder builder = new StringBuilder("/").append(ACTINIC_BEAM_CALIBRATION_ROOT).append("/").append(ACTINIC_BEAM_CALIBRATION_POINT_LIST_ELEMENT).append("/").append(ACTINIC_BEAM_CALIBRATION_POINT_ELEMENT);
        String path = builder.toString();
        return path;
    }

    public static String getPathFromCalibrationPointToLightIntensityInPercents()
    {
        return INTENSITY_IN_PERCENTS;
    }

    public static String getPathFromCalibrationPointToLightIntensityInAbsoluteUnit()
    {
        return INTENSITY_IN_ABSOLUTE_UNIT;
    }

    public static String getPathFromCalibrationPointToSliderMountedFilter()
    {
        return ACTINIC_BEAM_SLIDER_MOUNTED_FILTER;
    }

    public static String getPathFromCalibrationPointToSliderPosition()
    {
        return ACTINIC_BEAM_SELECTED_SLIDER_FILTER_POSITION;
    }

    @Override
    public void save(ActinicBeamCalibrationImmutable actnicBeamCalibration, File f) throws SavingException
    {   
        try(OutputStream fs = new FileOutputStream(f))
        {
            saveCalibrationInXML(fs, actnicBeamCalibration);
        } catch (IOException | ParserConfigurationException | XMLStreamException e) 
        {
            throw new SavingException("Exception encountered by ABCSaver", e);
        }
    }

    @Override
    public void writeToStream(ActinicBeamCalibrationImmutable actnicBeamCalibration, OutputStream out) throws SavingException 
    { 
        try(BufferedOutputStream zipStream = new BufferedOutputStream(out)) 
        {           
            saveCalibrationInXML(zipStream, actnicBeamCalibration);          
        } catch (IOException | ParserConfigurationException | XMLStreamException e) 
        {
            throw new SavingException("Exception encountered by ABCSaver", e);
        }       
    }

    private void saveCalibrationInXML(OutputStream out, ActinicBeamCalibrationImmutable actnicBeamCalibration) throws ParserConfigurationException, XMLStreamException
    {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(out,  StandardCharsets.UTF_8.name());
        writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");

        writer.writeStartElement(ACTINIC_BEAM_CALIBRATION_ROOT);        

        writer.writeStartElement(LIGHT_INTENSITY_UNIT_TYPE_ELEMENT);
        writer.writeCharacters(actnicBeamCalibration.getAbsoluteLightIntensityUnit().name());
        writer.writeEndElement();

        writer.writeStartElement(ACTINIC_BEAM_CALIBRATION_POINT_LIST_ELEMENT);
        for(ActinicCalibrationPointImmutable phase : actnicBeamCalibration.getActinicBeamCalibrationPoints())
        {
            writeCalibrationPoint(writer, phase);
        }

        writer.writeEndElement();//end of ACTINIC_BEAM_PHASES_LIST_ELEMENT

        writer.writeEndElement(); //end of ACTINIC_BEAM_CALIBRATION_ROOT

        writer.writeEndDocument();
        writer.close();//does not close the underlying output stream
    }

    private void writeCalibrationPoint(XMLStreamWriter writer, ActinicCalibrationPointImmutable phaseCalibration) throws XMLStreamException
    {
        writer.writeStartElement(ACTINIC_BEAM_CALIBRATION_POINT_ELEMENT);

        writer.writeStartElement(INTENSITY_IN_PERCENTS);
        writer.writeCharacters(Double.toString(phaseCalibration.getLightIntensityInPercent()));
        writer.writeEndElement();

        writer.writeStartElement(INTENSITY_IN_ABSOLUTE_UNIT);
        writer.writeCharacters(Double.toString(phaseCalibration.getLightIntensityInAbsoluteUnits()));
        writer.writeEndElement();

        writer.writeStartElement(ACTINIC_BEAM_SELECTED_SLIDER_FILTER_POSITION);
        writer.writeCharacters(Integer.toString(phaseCalibration.getFilter().getPositionIndex()));
        writer.writeEndElement();

        writer.writeStartElement(ACTINIC_BEAM_SLIDER_MOUNTED_FILTER);
        writer.writeCharacters(phaseCalibration.getFilter().getDescription());
        writer.writeEndElement();

        writer.writeEndElement();
    }
}
