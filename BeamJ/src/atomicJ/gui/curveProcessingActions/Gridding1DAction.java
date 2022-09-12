package atomicJ.gui.curveProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import atomicJ.curveProcessing.Gridding1DDialog;
import atomicJ.curveProcessing.Gridding1DModel;
import atomicJ.data.Channel1D;
import atomicJ.resources.Channel1DResource;
import atomicJ.resources.ResourceView;


public class Gridding1DAction<R extends Channel1DResource> extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final ResourceView<R, Channel1D, String> manager;

    public Gridding1DAction(ResourceView<R, Channel1D, String> manager) 
    {
        this.manager = manager;

        putValue(NAME, "Gridding");
        putValue(SHORT_DESCRIPTION, "Gridding");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        Gridding1DDialog dialog = new Gridding1DDialog(manager.getAssociatedWindow(), "Gridding", true);
        Gridding1DModel<R> model = new Gridding1DModel<R>(manager);

        dialog.showDialog(model);
        dialog.dispose();
    }
}