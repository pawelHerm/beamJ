package atomicJ.gui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class Channel1DResultPanel<E extends Channel1DGraphsSupervisor> extends MultipleXYChartPanel<ChannelChart<?>>
{
    private static final long serialVersionUID = 1L;

    private final Action jumpToResultsAction = new JumpToResultsAction();
    private final Action showRawDataAction = new ShowRawDataAction();

    private final JMenuItem jumpToResultsItem = new JMenuItem(jumpToResultsAction);
    private final JMenuItem showRawDataItem = new JMenuItem(showRawDataAction);

    private E supervisor;

    public Channel1DResultPanel(boolean addPopup)
    {
        this(addPopup, true);
    }

    public Channel1DResultPanel(boolean addPopup, boolean allowROIbasedActions)
    {
        this(addPopup, allowROIbasedActions, true);
    }

    public Channel1DResultPanel(boolean addPopup, boolean allowROIbasedActions, boolean allowStaticsGroupActions)
    {
        super(null, false);

        if(addPopup)
        {
            setPopupMenu(buildDenistyPanelPopupMenu(true, true, true, true, true));
        }
    }

    public void setGraphSupervisor(E supervisor)
    {
        this.supervisor = supervisor;
    }

    protected JPopupMenu buildDenistyPanelPopupMenu(boolean properties, boolean copy, boolean save, boolean print, boolean zoom) 
    {
        JPopupMenu popupMenu = super.createPopupMenu(properties, copy, save, print, zoom);

        popupMenu.add(showRawDataItem);
        popupMenu.add(jumpToResultsItem);

        return popupMenu;
    }

    public static class Channel1DPanelFactory<E extends Channel1DGraphsSupervisor> implements AbstractChartPanelFactory<Channel1DResultPanel<E>>
    {
        @Override
        public Channel1DResultPanel<E> buildEmptyPanel() 
        {
            return new Channel1DResultPanel<>(true);
        }       
    }

    private class JumpToResultsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public JumpToResultsAction() 
        {           
            //            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y,InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Jump to result");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            if(supervisor != null)
            {
                supervisor.jumpToResults();
            }
        }
    }

    private class ShowRawDataAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowRawDataAction() 
        {           
            //            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y,InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Raw data");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            if(supervisor != null)
            {
                supervisor.showRawResourceData();
            }
        }
    }
}
