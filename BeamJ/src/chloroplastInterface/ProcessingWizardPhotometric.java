
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Pawe� Hermanowicz
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

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

import atomicJ.gui.MessageDisplayer;
import atomicJ.gui.SubPanel;
import atomicJ.gui.WizardPage;


public class ProcessingWizardPhotometric extends JDialog implements PropertyChangeListener, MessageDisplayer
{
    private static final long serialVersionUID = 1L;

    private static final String BUTTON_NEXT = "ButtonNext";
    private static final String BUTTON_NEXT_BATCH = "ButtonNextBatch";
    private static final String BUTTON_BACK = "ButtonBack";
    private static final String BUTTON_PREVIOUS_BATCH = "ButtonPreviousBatch";

    private final JLabel labelTaskName = new JLabel();
    private final JLabel labelTaskDescription = new JLabel();
    private final JLabel labelMessage = new JLabel();

    private final BackAction backAction = new BackAction();
    private final NextAction nextAction = new NextAction();
    private final NextBatchAction nextBatchAction = new NextBatchAction();
    private final PreviousAction previousAction = new PreviousAction();
    private final FinishAction finishAction = new FinishAction();
    private final CancelAction cancelAction = new CancelAction();

    private final JButton buttonBack = new JButton(backAction);
    private final JButton buttonNext = new JButton(nextAction);
    private final JButton buttonNextBatch = new JButton(nextBatchAction);
    private final JButton buttonPreviousBatch = new JButton(previousAction);
    private final JButton buttonFinish = new JButton(finishAction);
    private final JButton buttonCancel = new JButton(cancelAction);

    private final CardLayout nextButtonCards = new CardLayout(0,0); 
    private final CardLayout backButtonCards = new CardLayout(0,0); 
    private final JPanel panelNextButtons = new JPanel(nextButtonCards);
    private final JPanel panelBackButtons = new JPanel(backButtonCards);

    private final JPanel panelPages = new JPanel(new CardLayout());
    private final JPanel panelControls = new JPanel(new CardLayout());
    private final ProcessingSettingsPage pageSettings;
    private final ProcessingSourceSelectionPagePhotometric pageSources;
    private final List<WizardPage> pages = new ArrayList<WizardPage>();
    private final ProcessingWizardModelPhotometric wizardModel;
    private ProcessingModel processingModel;

    public ProcessingWizardPhotometric(ProcessingModel model)
    {
        super(model.getResultDestination().getPublicationSite(), "Processing assistant", ModalityType.MODELESS);

        this.processingModel = model;

        Container content = getContentPane();
        GroupLayout layout = new GroupLayout(content);
        content.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        JPanel panelTaskInformation = buildTaskInformationPanel();
        JPanel panelButtons = buildButtonPanel();	

        this.pageSources = new ProcessingSourceSelectionPagePhotometric(processingModel);
        this.pageSettings = new ProcessingSettingsPage(processingModel);

        pages.add(pageSources);
        pages.add(pageSettings);

        this.wizardModel = new ProcessingWizardModelPhotometric(pages, processingModel);
        wizardModel.addPropertyChangeListener(this);

        panelPages.add(pageSettings.getView(), pageSettings.getIdentifier());
        panelPages.add(pageSources.getView(), pageSources.getIdentifier());
        panelControls.add(pageSettings.getControls(), pageSettings.getIdentifier());
        panelControls.add(pageSources.getControls(), pageSources.getIdentifier());

        pullModelProperties();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                ProcessingWizardPhotometric.this.cancel();
            }
        });

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelControls, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(
                        layout.createParallelGroup()
                        .addComponent(panelButtons)
                        .addComponent(panelPages, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelTaskInformation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(panelTaskInformation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup()
                        .addComponent(panelControls)
                        .addComponent(panelPages, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addComponent(panelButtons, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                );


        pack();
        setLocationRelativeTo(model.getResultDestination().getPublicationSite());

    }

    public void setProcessingModel(ProcessingModel processingModelNew)
    {
        this.processingModel = processingModelNew;

        pageSources.setModel(processingModelNew);
        pageSettings.setProcessingModel(processingModelNew);		
        wizardModel.setProcessingModel(processingModelNew);

        pullModelProperties();
    }

    private void pullModelProperties()
    {
        boolean backEnabled = wizardModel.isBackEnabled();
        buttonBack.setEnabled(backEnabled);

        boolean previousBatchEnabled = wizardModel.isPreviousBatchEnabled();
        buttonPreviousBatch.setEnabled(previousBatchEnabled);

        boolean nextEnabled = wizardModel.isNextEnabled();
        buttonNext.setEnabled(nextEnabled);	

        boolean nextBatchEnabled = wizardModel.isNextBatchEnabled();
        buttonNextBatch.setEnabled(nextBatchEnabled);

        boolean finishEnabled = wizardModel.isFinishEnabled();
        buttonFinish.setEnabled(finishEnabled);

        WizardPage currentPage = wizardModel.getCurrentPage();
        updateWizard(currentPage);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();

        if(ProcessingWizardModelPhotometric.NEXT_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = buttonNext.isEnabled();
            if(newVal != oldVal)
            {
                buttonNext.setEnabled(newVal);
            }
        }
        else if(ProcessingWizardModelPhotometric.NEXT_BATCH_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = buttonNextBatch.isEnabled();
            if(newVal != oldVal)
            {
                buttonNextBatch.setEnabled(newVal);
            }
        }
        else if(ProcessingWizardModelPhotometric.BACK_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = buttonBack.isEnabled();
            if(newVal != oldVal)
            {
                buttonBack.setEnabled(newVal);
            }
        }
        else if(ProcessingWizardModelPhotometric.PREVIOUS_BATCH_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = buttonPreviousBatch.isEnabled();
            if(newVal != oldVal)
            {
                buttonPreviousBatch.setEnabled(newVal);
            }
        }
        else if(ProcessingWizardModelPhotometric.FINISH_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = buttonFinish.isEnabled();
            if(newVal != oldVal)
            {
                buttonFinish.setEnabled(newVal);
            }
        }
        else if(ProcessingWizardModelPhotometric.CURRENT_PAGE.equals(property))
        {
            WizardPage currentPage = (WizardPage)evt.getNewValue();
            updateWizard(currentPage);
        }
    }

    private JPanel buildTaskInformationPanel()
    {
        SubPanel panel = new SubPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));	

        Color color = UIManager.getColor("Label.background");
        panel.setBackground(color);
        labelTaskName.setBackground(color);
        labelTaskDescription.setBackground(color);
        labelMessage.setBackground(color);

        Font font = UIManager.getFont("TextField.font");
        labelTaskName.setFont(font.deriveFont(Font.BOLD));
        labelTaskDescription.setFont(font);

        panel.addComponent(labelTaskName, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        panel.addComponent(labelTaskDescription, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        panel.addComponent(labelMessage, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);

        return panel;
    }

    private void updateWizard(WizardPage page)
    {
        String id = page.getIdentifier();

        CardLayout layoutPages = (CardLayout)(panelPages.getLayout());
        layoutPages.show(panelPages, id);

        CardLayout layoutControls = (CardLayout)(panelControls.getLayout());
        layoutControls.show(panelControls, id);

        String name = page.getTaskName();
        String description = page.getTaskDescription();

        labelTaskName.setText(name);
        labelTaskDescription.setText(description);

        if(page.isFirst())
        {
            backButtonCards.show(panelBackButtons, BUTTON_PREVIOUS_BATCH);
        }
        else
        {
            backButtonCards.show(panelBackButtons, BUTTON_BACK);
        }
        if(page.isLast())
        {
            nextButtonCards.show(panelNextButtons, BUTTON_NEXT_BATCH);
        }
        else
        {
            nextButtonCards.show(panelNextButtons, BUTTON_NEXT);
        }
    }

    private JPanel buildButtonPanel()
    {

        buttonNext.setMargin(new Insets(0,7,0,7));
        buttonNextBatch.setMargin(new Insets(0,7,0,7));
        buttonBack.setMargin(new Insets(0,7,0,7));
        buttonPreviousBatch.setMargin(new Insets(0,7,0,7));

        panelNextButtons.add(buttonNext, BUTTON_NEXT);
        panelNextButtons.add(buttonNextBatch, BUTTON_NEXT_BATCH);
        panelBackButtons.add(buttonPreviousBatch, BUTTON_PREVIOUS_BATCH);
        panelBackButtons.add(buttonBack, BUTTON_BACK);

        JPanel panelButtons = new JPanel();	

        GroupLayout layout = new GroupLayout(panelButtons);
        panelButtons.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panelBackButtons, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelNextButtons, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonFinish)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(panelBackButtons)
                .addComponent(panelNextButtons)
                .addComponent(buttonFinish)
                .addComponent(buttonCancel));

        layout.linkSize(SwingConstants.HORIZONTAL, buttonFinish,buttonCancel);
        layout.linkSize(SwingConstants.HORIZONTAL, panelBackButtons,panelNextButtons);
        layout.linkSize(SwingConstants.VERTICAL, buttonFinish,buttonCancel,panelBackButtons,panelNextButtons);

        return panelButtons;
    }

    public void next()
    {		
        wizardModel.next();
    }

    public void back()
    {
        wizardModel.back();
    }

    public void finish()
    {
        processingModel.processCurvesConcurrently();
        setVisible(false);
    }

    public void cancel()
    {
        pageSources.cancel();
        processingModel.cancel();
        setVisible(false);
    }

    public ProcessingModel getModel()
    {
        return processingModel;
    }

    @Override
    public void publishErrorMessage(String message) 
    {
        Icon icon = UIManager.getIcon("OptionPane.errorIcon");
        labelMessage.setIcon(icon);
        labelMessage.setText(message);
        pack();
        validate();
    }

    @Override
    public void publishWarningMessage(String message) 
    {
        Icon icon = UIManager.getIcon("OptionPane.warningIcon");
        labelMessage.setIcon(icon);
        labelMessage.setText(message);
        pack();
        validate();
    }

    @Override
    public void publishInformationMessage(String message) 
    {
        Icon icon = UIManager.getIcon("OptionPane.informationIcon");
        labelMessage.setIcon(icon);
        labelMessage.setText(message);
        pack();
        validate();
    }

    @Override
    public void clearMessage() 
    {
        labelMessage.setIcon(null);
        labelMessage.setText(null);
        pack();
        validate();
    }

    private class BackAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public BackAction()
        {			
            putValue(NAME,"<<   Back");
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            back();
        }
    }

    private class PreviousAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public PreviousAction()
        {			
            putValue(NAME, "<<   Back");
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            back();
        }
    }

    private class NextAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public NextAction()
        {			
            putValue(NAME,"Next   >>");
            putValue(MNEMONIC_KEY, KeyEvent.VK_N);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            next();
        }
    }

    private class NextBatchAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public NextBatchAction()
        {			
            putValue(NAME, "Next batch >>");
            putValue(MNEMONIC_KEY, KeyEvent.VK_N);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            next();
        }
    }

    private class FinishAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FinishAction()
        {			
            putValue(NAME, "Finish");
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            finish();
        }
    }

    private class CancelAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CancelAction()
        {			
            putValue(NAME, "Cancel");
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            cancel();
        }
    }
}
