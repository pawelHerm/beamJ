
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

package atomicJ.gui.editors;


import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import atomicJ.gui.FontReceiver;
import atomicJ.gui.SubPanel;



public class FontChooserDialog extends JDialog implements ItemListener, ChangeListener, ListSelectionListener
{
    private static final long serialVersionUID = 1L;

    private int fontSize;
    private String fontName;

    private boolean bold;
    private boolean italic;
    private boolean underlined;
    private boolean strikeThrough;

    private Font initFont;
    private Font currentFont;

    private JList<String> fontList;
    private final JSpinner spinnerFontSize = new JSpinner(new SpinnerNumberModel(12, 1, 1000, 1));

    private final JCheckBox checkBoxBold = new JCheckBox("Bold");
    private final JCheckBox checkBoxItalic = new JCheckBox("Italic");  
    private final JCheckBox checkBoxUnderlined = new JCheckBox("Underline");;
    private final JCheckBox checkBoxStrikeThrough = new JCheckBox("Strike through");

    private final JButton buttonOK = new JButton(new OKAction());
    private final JButton buttonReset = new JButton(new ResetAction());
    private final JButton buttonCancel = new JButton(new CancelAction());

    private FontReceiver receiver;

    public FontChooserDialog(Window parent, String title) 
    {
        super(parent, title, ModalityType.MODELESS);
        setLayout(new BorderLayout());

        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();

        add(mainPanel, BorderLayout.NORTH);   	
        add(panelButtons, BorderLayout.SOUTH);   	

        initChangeListener();
        initListSelectionListener();
        initItemListener();

        pack();
        setLocationRelativeTo(parent); 
    }


    private JPanel buildMainPanel()
    {
        JPanel mainPanel = new JPanel();

        GraphicsEnvironment g  = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = g.getAvailableFontFamilyNames(); 


        //buildFontPanel
        JPanel panelFont = new JPanel(new BorderLayout());
        panelFont.setBorder(BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), "Font"));
        this.fontList = new JList<>(fonts);

        JScrollPane paneFont = new JScrollPane(this.fontList);
        paneFont.setBorder(BorderFactory.createEtchedBorder());
        panelFont.add(paneFont); 

        //buildSizePanel
        JPanel panelSize = new JPanel(new BorderLayout());
        panelSize.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Size"));

        JPanel panelFontSize = new JPanel(new BorderLayout());
        SubPanel innerPanel = new SubPanel();
        innerPanel.addComponent(spinnerFontSize, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);
        panelFontSize.add(innerPanel, BorderLayout.CENTER);
        panelSize.add(panelFontSize);

        //builds fontStylePanel
        JPanel panelFontStyle = new JPanel(new GridLayout(2, 2));
        panelFontStyle.add(this.checkBoxBold);
        panelFontStyle.add(this.checkBoxItalic);
        panelFontStyle.add(this.checkBoxUnderlined);
        panelFontStyle.add(this.checkBoxStrikeThrough);

        panelFontStyle.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Style"));


        //layout

        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(panelFont)
                .addGroup(layout.createParallelGroup()
                        .addComponent(panelSize)
                        .addComponent(panelFontStyle)
                        )
                );

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(panelFont)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(panelSize)
                        .addComponent(panelFontStyle))

                );

        layout.linkSize(SwingConstants.HORIZONTAL, panelFontStyle, panelSize);

        return mainPanel;
    }

    private void initChangeListener()
    {
        spinnerFontSize.addChangeListener(this);
    }

    private void initListSelectionListener()
    {
        fontList.addListSelectionListener(this);
    }

    private void initItemListener()
    {
        checkBoxBold.addItemListener(this);
        checkBoxItalic.addItemListener(this);
        checkBoxUnderlined.addItemListener(this);
        checkBoxStrikeThrough.addItemListener(this);
    }

    public Font getSelectedFont() 
    {
        Font font = new Font(getSelectedName(), getSelectedStyle(),
                getSelectedSize()).deriveFont(getFontAttributes());

        return font;
    }


    public String getSelectedName() {
        return this.fontList.getSelectedValue();
    }

    public int getSelectedStyle() 
    {
        int style = Font.PLAIN;

        if (bold)
        {
            style = style + Font.BOLD;
        }
        if (italic) 
        {
            style = style + Font.ITALIC;
        }

        return  style;       
    }

    public Map<TextAttribute, Object> getFontAttributes()
    {
        Map<TextAttribute, Object> fontAttributes = new HashMap<TextAttribute, Object>();
        if(underlined)
        {
            fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        }
        if(strikeThrough)
        {
            fontAttributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);

        }

        return fontAttributes;
    }


    public int getSelectedSize() 
    {
        return fontSize;        
    }


    public void showDialog(FontReceiver fontReceiver)
    {
        this.receiver = fontReceiver;

        this.initFont = fontReceiver.getFont();        
        setSelectedFont(initFont);

        setVisible(true);
    }

    private void setSelectedFont (Font font) 
    {
        if (font == null) {
            throw new NullPointerException();
        }

        this.currentFont = font;
        this.bold = font.isBold();
        this.italic = font.isItalic();

        Map<TextAttribute, ?>  attributes = font.getAttributes();
        this.underlined = (attributes.get(TextAttribute.UNDERLINE) == TextAttribute.UNDERLINE_ON);
        this.strikeThrough = (attributes.get(TextAttribute.STRIKETHROUGH) == TextAttribute.STRIKETHROUGH_ON);

        this.checkBoxBold.setSelected(bold);
        this.checkBoxItalic.setSelected(italic);
        this.checkBoxUnderlined.setSelected(underlined);
        this.checkBoxStrikeThrough.setSelected(strikeThrough);

        this.fontName = font.getName();
        ListModel<String> model = this.fontList.getModel();
        this.fontList.clearSelection();
        for (int i = 0; i < model.getSize(); i++) 
        {
            if (fontName.equals(model.getElementAt(i))) 
            {
                this.fontList.setSelectedIndex(i);
                this.fontList.ensureIndexIsVisible(i);
                break;
            }
        }

        this.fontSize = font.getSize();
        spinnerFontSize.setValue(fontSize);
    }

    private void approve()
    {
        setVisible(false);
    }

    private void reset()
    {
        setSelectedFont(initFont);
        receiver.setFont(initFont);
    }

    private void cancel()
    {
        setSelectedFont(initFont);
        receiver.setFont(initFont);
        setVisible(false);
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonOK).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(buttonReset).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonOK)
                .addComponent(buttonReset)
                .addComponent(buttonCancel));

        layout.linkSize(buttonOK, buttonReset, buttonCancel);

        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        return buttonPanel;
    }

    private class OKAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public OKAction()
        {			
            putValue(MNEMONIC_KEY,KeyEvent.VK_O);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"OK");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            approve();
        }
    }

    private class ResetAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ResetAction()
        {			
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Reset");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            reset();
        }
    }

    private class CancelAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CancelAction()
        {			
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            cancel();
        }
    }

    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Object source = evt.getSource();

        if(source == spinnerFontSize)
        {
            this.fontSize = ((SpinnerNumberModel)spinnerFontSize.getModel()).getNumber().intValue();
            this.currentFont = getSelectedFont();
            receiver.setFont(currentFont);
        }			
    }


    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        Object source = evt.getSource();
        boolean selected = (evt.getStateChange()== ItemEvent.SELECTED);

        if(source == checkBoxBold)
        {
            this.bold = selected;
            this.currentFont = getSelectedFont();
            receiver.setFont(currentFont);
        }
        else if(source == checkBoxItalic)
        {
            this.italic = selected;
            this.currentFont = getSelectedFont();
            receiver.setFont(currentFont);
        }
        else if(source == checkBoxUnderlined)
        {
            this.underlined = selected;
            this.currentFont = getSelectedFont();
            receiver.setFont(currentFont);
        }
        else if(source == checkBoxStrikeThrough)
        {
            this.strikeThrough = selected;
            this.currentFont = getSelectedFont();
            receiver.setFont(currentFont);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) 
    {
        if((evt.getValueIsAdjusting() == false)&&(fontList.isSelectionEmpty()== false))
        {
            this.fontName = fontList.getSelectedValue();

            this.currentFont = getSelectedFont();
            receiver.setFont(currentFont);
        }		
    }
}
