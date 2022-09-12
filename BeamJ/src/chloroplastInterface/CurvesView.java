
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


import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import atomicJ.analysis.PreviewDestination;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.Channel1DView;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.FileOpeningWizard;
import atomicJ.gui.ModifiableResourceDialogModel;
import atomicJ.gui.OpeningModelStandard;


public class CurvesView extends Channel1DView<PhotometricResource>
{
    private static final long serialVersionUID = 1L;	

    private final PreviewDestination<PhotometricResource, ChannelChart<?>> previewDestination = new PhotometricDialogPreviewDestination();
    private final FileOpeningWizard<SimplePhotometricSource> openingWizard = new FileOpeningWizard<>(new OpeningModelStandard<SimplePhotometricSource>(new PhotometricSourcePreviewerHandle(previewDestination)), PhotometricCurveReadingModel.getInstance());

    public CurvesView(final Window parent)
    {
        super(parent, new  ModifiableResourceDialogModel<Channel1D,Channel1DData,String, PhotometricResource> ());	
    }

    @Override
    public void startPreview()
    {
        openingWizard.setVisible(true);
    }

    @Override
    protected void startProcessingAllSources() 
    {
        List<SimplePhotometricSource> sourcesSelected = getSources();
        List<SimplePhotometricSource> sourcesSelectedCopies = new ArrayList<>();
        for(SimplePhotometricSource source : sourcesSelected)
        {
            sourcesSelectedCopies.add(source.copy());
        }
        startProcessing(sourcesSelectedCopies);
    }

    @Override
    protected void startProcessingSelectedSources()
    {
        List<SimplePhotometricSource> sourcesSelected = getAllSelectedSources();
        List<SimplePhotometricSource> sourcesSelectedCopies = new ArrayList<>();
        for(SimplePhotometricSource source : sourcesSelected)
        {
            sourcesSelectedCopies.add(source.copy());
        }
        startProcessing(sourcesSelectedCopies);
    }

    private void startProcessing(List<SimplePhotometricSource> sources) 
    {
        MainFrame parent = AtomicJ.currentFrame;;

        //        int batchNumber = parent.getResultBatchesCoordinator().getPublishedBatchCount();
        //        String name = Integer.toString(batchNumber);
        //        ProcessingBatchModel model = new ProcessingBatchModel(parent, sources, name, batchNumber);
        //
        //        List<ProcessingBatchModel> models = Collections.singletonList(model);
        //        parent.startProcessing(models);
    }

    public List<SimplePhotometricSource> getSources()
    {
        List<SimplePhotometricSource> sources = new ArrayList<>();
        List<PhotometricResource> resources = getResources();

        for(PhotometricResource resource : resources)
        {
            sources.add(resource.getSource());
        }

        return sources;
    }

    public List<SimplePhotometricSource> getAllSelectedSources()
    {
        List<SimplePhotometricSource> sources = new ArrayList<>();
        List<PhotometricResource> resources = getAllSelectedResources();

        for(PhotometricResource resource : resources)
        {
            sources.add(resource.getSource());
        }

        return sources;
    }


    private class PhotometricDialogPreviewDestination implements PreviewDestination<PhotometricResource, ChannelChart<?>>
    {
        @Override
        public Window getPublicationSite() 
        {
            return CurvesView.this;
        }

        @Override
        public void publishPreview(Map<PhotometricResource, Map<String, ChannelChart<?>>> charts) 
        {
            if(!charts.isEmpty())
            {
                int previousCount = getResourceCount();
                addResources(charts);
                selectResource(previousCount);
            }
        }

        @Override
        public void requestPreviewEnd() 
        {
            openingWizard.endPreview();            
        }     
    }
}
