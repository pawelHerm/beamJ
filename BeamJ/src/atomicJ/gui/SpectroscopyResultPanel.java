package atomicJ.gui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class SpectroscopyResultPanel extends Channel1DResultPanel<SpectroscopyGraphsSupervisor>
{
    private static final long serialVersionUID = 1L;

    private final Action markOnMapAction = new MarkOnMapAction();
    private final JMenuItem markOnMapItem = new JMenuItem(markOnMapAction);

    private SpectroscopyGraphsSupervisor supervisor;

    public SpectroscopyResultPanel(boolean addPopup)
    {
        this(addPopup, true);
    }

    public SpectroscopyResultPanel(boolean addPopup, boolean allowROIbasedActions)
    {
        this(addPopup, allowROIbasedActions, true);
    }

    public SpectroscopyResultPanel(boolean addPopup, boolean allowROIbasedActions, boolean allowStaticsGroupActions)
    {
        super(addPopup, allowROIbasedActions, allowStaticsGroupActions);
    }

    @Override
    protected JPopupMenu buildDenistyPanelPopupMenu(boolean properties, boolean copy, boolean save, boolean print, boolean zoom) 
    {
        JPopupMenu popupMenu = super.createPopupMenu(properties, copy, save, print, zoom);

        popupMenu.add(markOnMapItem);

        return popupMenu;
    }

    public void setMapPositionMarkingEnabled(boolean enabled)
    {
        markOnMapAction.setEnabled(enabled);
    }

    public static class SpectroscopyPanelFactory implements AbstractChartPanelFactory<SpectroscopyResultPanel>
    {
        private static final SpectroscopyPanelFactory INSTANCE = new SpectroscopyPanelFactory();

        public static SpectroscopyPanelFactory getInstance()
        {
            return INSTANCE;
        }

        @Override
        public SpectroscopyResultPanel buildEmptyPanel() 
        {
            return new SpectroscopyResultPanel(true);
        }       
    }


    private class MarkOnMapAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public MarkOnMapAction() 
        {           
            //            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y,InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Mark on map");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            if(supervisor != null)
            {
                supervisor.markSourcePosition();
            }
        }
    }


}
