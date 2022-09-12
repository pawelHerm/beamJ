
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Paweł Hermanowicz
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

package atomicJ.gui;

import javax.swing.*;

import chloroplastInterface.MainFrame;

import java.awt.*;
import java.util.Locale;

public class AtomicJ
{
    public static final String APPLICATION_NAME = "ChloroplastJ";
    public static final String APPLICATION_VERSION = "1.0";
    public static final String MANUAL_FILE_NAME = "AtomicJ_Users_Manual.pdf";
    public static final String CONTACT_MAIL = "pawel.hermanowicz@uj.edu.pl";

    public static final String COPYRRIGHT_NOTICE = "Copyright 2020 by Pawel Hermanowicz \n\nThis program is free software; you can redistribute it and/or modify " +
            "it under the terms of the GNU General Public License as published by " +
            "the Free Software Foundation version 2 of the License. "+
            "This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without " +
            "even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU " +
            "General Public License for more details."+
            "You should have received a copy of the GNU General Public License along with this program; if not, " +
            "see http://www.gnu.org/licenses/";

    public static MainFrame currentFrame;

    @SuppressWarnings("unused")
    private static byte[] lastResort = new byte[256*1024];

    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    Locale.setDefault(Locale.US);			
                }
                catch(OutOfMemoryError oome)
                {
                    handleOOME(oome);
                }
                catch(Exception e) 
                {
                    e.printStackTrace();
                }

                currentFrame = new MainFrame();
                currentFrame.setVisible(true);
            }
        });	
    }

    public static void handleOOME(OutOfMemoryError oome)
    {
        lastResort = null;

        Object[] options = {"Yes", "No"};
        int n = JOptionPane.showOptionDialog(currentFrame, "AtomicJ has run out of memory. The application will probably not work properly now. Do you want to close it?","Out of memory error", JOptionPane.YES_NO_OPTION,JOptionPane.ERROR_MESSAGE,
                null,  options, options[0]); 

        if(n == 0)
        {
            System.exit(-1);
        }
    }
}

