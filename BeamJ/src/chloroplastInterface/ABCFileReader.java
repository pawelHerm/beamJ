
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.filechooser.FileNameExtensionFilter;
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

import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.RomanNumeralConverter;
import chloroplastInterface.ActinicBeamManualCalibrationModel.ActinicBeamCalibrationImmutable;
import chloroplastInterface.optics.SliderMountedFilter;

public class ABCFileReader 
{

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"abc"};
    private static final String DESCRIPTION = "Actinic beam calibration (.abc)";

    private static final FileFilter EXTENSION_FILTER = new FileExtensionPatternFilter(ACCEPTED_EXTENSIONS);

    private static final ABCFileReader INSTANCE = new ABCFileReader();

    private ABCFileReader(){};

    public static final ABCFileReader getInstance()
    {
        return INSTANCE;
    }

    public ActinicBeamCalibrationImmutable readActinicBeamSettings(File f) throws UserCommunicableException 
    {             
        OpticsConfiguration currentOpticsConfiguration = ChloroplastJ.CURRENT_FRAME.getOpticsConfiguration();

        try(InputStream inputStream = new BufferedInputStream(new FileInputStream(f)))
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);

            DocumentBuilder builder = factory.newDocumentBuilder();

            XPathFactory xpfactory = XPathFactory.newInstance();
            XPath path = xpfactory.newXPath();
            Document doc = builder.parse(inputStream);

            NodeList nodesActinicBeamPhases = (NodeList)path.evaluate(ABCSaver.getPathFromRootToCalibrationPoint(), doc, XPathConstants.NODESET);

            List<ActinicCalibrationPointImmutable> actinicCalibrationPhases = new ArrayList<>();
            for(int i = 0; i < nodesActinicBeamPhases.getLength();i++)
            {
                Node actinicBeamNode = nodesActinicBeamPhases.item(i);
                double intensityInAbsoluteUnit = ((Number)path.evaluate(ABCSaver.getPathFromCalibrationPointToLightIntensityInAbsoluteUnit(), actinicBeamNode, XPathConstants.NUMBER)).doubleValue();
                String actinicBeamFilterDescription = path.evaluate(ABCSaver.getPathFromCalibrationPointToSliderMountedFilter(), actinicBeamNode, XPathConstants.STRING).toString();
                int sliderPositionIndex = ((Number)path.evaluate(ABCSaver.getPathFromCalibrationPointToSliderPosition(), actinicBeamNode, XPathConstants.NUMBER)).intValue();
                SliderMountedFilter currentlyMountedFilter = currentOpticsConfiguration.getActinicBeamSliderFilter(sliderPositionIndex);

                if(!currentlyMountedFilter.canFilterBeDescribedBy(actinicBeamFilterDescription))
                {
                    Logger.getLogger(CLMFileReader.class.getName()).log(Level.SEVERE, "The filter currently installed in the position "+ RomanNumeralConverter.convertToRoman(sliderPositionIndex + 1)+" does not agree with the filter description found in the read-in file. The description is " + actinicBeamFilterDescription);
                }

                double intensityInPercents = ((Number)path.evaluate(ABCSaver.getPathFromCalibrationPointToLightIntensityInPercents(), actinicBeamNode, XPathConstants.NUMBER)).doubleValue();
                ActinicCalibrationPointImmutable actinicCalibrationPhase = new ActinicCalibrationPointImmutable(intensityInPercents, intensityInAbsoluteUnit, currentlyMountedFilter);
                actinicCalibrationPhases.add(actinicCalibrationPhase);
            }

            String absoluteLightIntensityUnitName = path.evaluate(ABCSaver.getPathFromRootToIrradianceUnitTypeElement(), doc, XPathConstants.STRING).toString();
            IrradianceUnitType absoluteLightIntensityUnit = IrradianceUnitType.valueOf(absoluteLightIntensityUnitName);

            ActinicBeamCalibrationImmutable calibration = new ActinicBeamCalibrationImmutable(actinicCalibrationPhases, absoluteLightIntensityUnit);
            return calibration;
        } 
        catch (SAXException | XPathExpressionException eSax) 
        {
            eSax.printStackTrace();
            throw new UserCommunicableException("Error occured during parsing the settings", eSax);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured during parsing the settings", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured during parsing the settings", e);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured during parsing the settings", e);
        } 
    }   

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    public static String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    public boolean accept(File f) 
    {
        boolean accept = EXTENSION_FILTER.accept(f); 
        return accept;
    }

    public javax.swing.filechooser.FileFilter getFileFilter() 
    {
        return new FileNameExtensionFilter("Actinic beam calibration file", "abc");
    }
}