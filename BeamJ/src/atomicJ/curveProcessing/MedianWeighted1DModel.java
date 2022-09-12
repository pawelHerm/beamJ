package atomicJ.curveProcessing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import atomicJ.data.Channel1D;
import atomicJ.gui.ArrayTextExporter;
import atomicJ.gui.Array2DTextReader;
import atomicJ.gui.imageProcessing.KernelChangeListener;
import atomicJ.gui.imageProcessing.KernelElementValueEvent;
import atomicJ.gui.imageProcessing.KernelStructuralEvent;
import atomicJ.gui.imageProcessing.KernelElementValueEvent.KernelElementChange;
import atomicJ.resources.Channel1DResource;
import atomicJ.resources.ResourceView;


public class MedianWeighted1DModel<R extends Channel1DResource> extends Kernel1DModel<R>
{
    public static final String KERNEL_RADIUS = "KernelRadius";

    public static final String KERNEL = "Kernel";

    public static final String EXPORT_ENABLED = "ExportEnabled";

    private int kernelXRadius = 1;

    private final double divisor = 1;

    private double[] kernel = new double[] {1,1,1};

    private final Set<KernelChangeListener> kernelChangeListeners = new LinkedHashSet<>();

    private boolean kernelFilled = true;
    private boolean saveEnabled = true;

    public MedianWeighted1DModel(ResourceView<R, Channel1D, String> manager)
    {
        super(manager, false, false);
    }

    @Override
    public int getKernelXRadius()
    {
        return kernelXRadius;
    }

    @Override
    public void setKernelXRadius(int kernelXRadiusNew)
    {       
        if(kernelXRadiusNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'kernelXRadiusNew' must be grater or equal 0");
        }

        if(this.kernelXRadius != kernelXRadiusNew)
        {
            int kernelWidthOld = this.kernelXRadius;
            this.kernelXRadius = kernelXRadiusNew;

            int diff = kernelXRadiusNew - kernelWidthOld;
            double[] kernelOld = Arrays.copyOf(kernel, kernel.length);
            double[] kernelNew = new double[2*kernelXRadiusNew + 1];

            if(diff > 0)
            {
                System.arraycopy(kernelOld, 0, kernelNew, diff, kernelOld.length);
            }
            else
            {
                System.arraycopy(kernelOld, -diff, kernelNew, 0, kernelOld.length + 2*diff);
            }   

            this.kernel = kernelNew;

            KernelStructuralEvent rowChangeEvent = new KernelStructuralEvent(new double[][] {kernelOld}, new double[][] {Arrays.copyOf(kernel, kernel.length)});
            fireKernelRowChangeEvent(rowChangeEvent);

            firePropertyChange(KERNEL_RADIUS, kernelWidthOld, kernelXRadiusNew);

            updatePreview();
        }
    }

    @Override
    public double[] getKernel()
    {
        return Arrays.copyOf(kernel, kernel.length);
    }

    @Override
    public void setKernelElement(int column, double valueNew)
    {  
        double valueOld = kernel[column];

        //we use compare(), because comparing two Double.NaN always returns false
        if(Double.compare(valueOld, valueNew) != 0)
        {
            kernel[column] = valueNew;
            fireKernelChangeEvent(column, valueOld, valueNew);

            checkIfInputSpecified();

            updatePreview();
        }
    }
    @Override
    public void setKernelElements(double[] kernelElements)
    {
        List<KernelElementChange> kernelChanges = new ArrayList<>();

        int columns = Math.min(kernelElements.length, 2*kernelXRadius + 1);

        for(int j = 0; j<columns; j++)
        {
            double valueOld = kernel[j];
            double valueNew = kernelElements[j];

            //we use compare(), because comparing two Double.NaN always returns false
            if(Double.compare(valueNew, valueOld) != 0)
            {
                kernel[j] = valueNew;

                KernelElementChange elementChange = new KernelElementChange(0, j, valueOld, valueNew);
                kernelChanges.add(elementChange);
            }
        }

        KernelElementValueEvent event = new KernelElementValueEvent(this, kernelChanges);
        fireKernelElementChangeEvent(event);

        updatePreview();
    }

    public void fireKernelChangeEvent(int column, double valueOld, double valueNew)
    {
        KernelElementChange elementChange = new KernelElementChange(0, column, valueOld, valueNew);
        KernelElementValueEvent event = new KernelElementValueEvent(this, elementChange);

        fireKernelElementChangeEvent(event);
    }

    public void fireKernelElementChangeEvent(KernelElementValueEvent event)
    {        
        for(KernelChangeListener listener : kernelChangeListeners)
        {
            listener.kernelElementValueChanged(event);
        }
    }

    public void fireKernelRowChangeEvent(KernelStructuralEvent event)
    {
        for(KernelChangeListener listener : kernelChangeListeners)
        {
            listener.kernelStructureChanged(event);
        }
    }

    @Override
    public void addKernelChangeListener(KernelChangeListener listener)
    {
        kernelChangeListeners.add(listener);
    }

    @Override
    public void removeKernelChangeListener(KernelChangeListener listener)
    {
        kernelChangeListeners.remove(listener);
    }

    @Override
    public boolean isExportEnabled()
    {
        return kernelFilled;
    }

    @Override
    public boolean calculateApplyEnabled()
    {
        return super.calculateApplyEnabled() && (kernelFilled && !Double.isNaN(divisor));
    }

    private void checkIfInputSpecified()
    {
        this.kernelFilled  = checkIfKernelFilled();

        boolean kernelFullySpecified = kernelFilled && !Double.isNaN(divisor);

        if(this.saveEnabled != kernelFullySpecified)
        {
            boolean saveEnabledOld = this.saveEnabled;
            this.saveEnabled = kernelFullySpecified;

            firePropertyChange(EXPORT_ENABLED, saveEnabledOld, this.saveEnabled);
        }

        checkIfApplyEnabled();
    }

    private boolean checkIfKernelFilled()
    {
        boolean kernelFilled = true;

        for(double el : kernel)
        {
            kernelFilled = kernelFilled && !Double.isNaN(el);
        }

        return kernelFilled;
    }

    @Override
    public void importKernel(File file, String[] selectedExtensions) throws IOException
    {
        Array2DTextReader reader = new Array2DTextReader();
        double[] kernel = reader.read(file, selectedExtensions)[0];
        int columnCount = kernel.length;

        setKernelXRadius(columnCount/2);
        setKernelElements(kernel);
    }

    @Override
    public void exportKernel(File file, String[] selectedExtensions) throws IOException 
    {
        ArrayTextExporter exporter = new ArrayTextExporter(); 
        exporter.export(new double[][] {kernel}, file, selectedExtensions);                         
    }

    @Override
    protected Channel1DDataInROITransformation buildTransformation()
    {
        Channel1DDataInROITransformation tr = new MedianWeightedFilter1D(kernel);
        return tr;
    }
}
