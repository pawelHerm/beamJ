
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

package atomicJ.gui.statistics;

import static atomicJ.gui.PreferenceKeys.WINDOW_HEIGHT;
import static atomicJ.gui.PreferenceKeys.WINDOW_WIDTH;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import atomicJ.analysis.Batch;
import atomicJ.analysis.Processed1DPack;



public class ResultsChooser<E extends Processed1DPack<E,?>> extends JDialog implements ActionListener
{
    private static final long serialVersionUID = 1L;

    private static final String OK_ACTION_COMMAND = "OKCommand";
    private static final String CANCEL_ACTION_COMMAND = "CancelCommand";

    private static final int DEFAULT_HEIGHT = Math.round(Toolkit.getDefaultToolkit().getScreenSize().height/3);
    private static final int DEFAULT_WIDTH = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width/4);

    private final Preferences pref = Preferences.userRoot().node(getClass().getName());

    private final int HEIGHT =  pref.getInt(WINDOW_HEIGHT,DEFAULT_HEIGHT);
    private final int WIDTH =  pref.getInt(WINDOW_WIDTH,DEFAULT_WIDTH);

    private final JButton buttonOK = new JButton("OK");
    private final JButton buttonCancel = new JButton("Cancel");
    private final JTree tree;

    private List<E> selectedPacks;

    private boolean selectionApproved;

    public ResultsChooser(List<Batch<E>> batches, Window parent)
    {
        super(parent, "Choose sample", ModalityType.APPLICATION_MODAL);

        tree = buildTree(batches);
        JPanel buttonPanel = buildButtonPanel();

        JScrollPane scrollPane = new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS); 
        scrollPane.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),scrollPane.getBorder()));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel,BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent evt)
            {				
                pref.putInt(WINDOW_HEIGHT, ResultsChooser.this.getHeight());
                pref.putInt(WINDOW_WIDTH, ResultsChooser.this.getWidth());
            }
        });

        setSize(WIDTH,HEIGHT);
        setLocationRelativeTo(parent);
    }

    public boolean showDialog()
    {
        selectionApproved = false;
        setVisible(true);
        return selectionApproved;
    }

    public List<E> getSelectedPacks()
    {
        return selectedPacks;
    }

    private void updateSelectedPacks()
    {
        selectedPacks = new ArrayList<>();

        TreePath[] selectedPaths = tree.getSelectionPaths();

        for(TreePath path: selectedPaths)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if(node.isLeaf())
            {
                E pack = (E)node.getUserObject();
                selectedPacks.add(pack);
            }
            else
            {
                Batch<E> batch = (Batch<E>)node.getUserObject();
                List<E> packs = batch.getPacks();
                selectedPacks.addAll(packs);
            }
        }
    }

    private JTree buildTree(List<Batch<E>> batches)
    {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Results");

        for(Batch<E> batch: batches)
        {
            List<E> leaves = batch.getPacks();

            DefaultMutableTreeNode node = new DefaultMutableTreeNode(batch);
            rootNode.add(node);

            for(E leaf: leaves)
            {
                DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode(leaf);
                node.add(leafNode);
            }
        }

        JTree tree = new JTree(rootNode);
        return tree;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        buttonOK.addActionListener(this);
        buttonOK.setActionCommand(OK_ACTION_COMMAND);
        buttonOK.setMnemonic(KeyEvent.VK_O);
        buttonCancel.addActionListener(this);
        buttonCancel.setActionCommand(CANCEL_ACTION_COMMAND);
        buttonCancel.setMnemonic(KeyEvent.VK_C);

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonOK)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonOK)
                .addComponent(buttonCancel));

        layout.linkSize(buttonOK, buttonCancel);

        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        return buttonPanel;
    }

    @Override
    public void actionPerformed(ActionEvent evt) 
    {
        String command = evt.getActionCommand();
        if(OK_ACTION_COMMAND.equals(command))
        {
            updateSelectedPacks();
            selectionApproved = true;
            setVisible(false);
        }
        else if(CANCEL_ACTION_COMMAND.equals(command))
        {
            selectionApproved = false;
            setVisible(false);
        }
    }
}
