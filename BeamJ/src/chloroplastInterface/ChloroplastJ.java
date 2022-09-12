
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

package chloroplastInterface;

import javax.swing.*;

import java.awt.*;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ChloroplastJ
{
    public static MainFrame CURRENT_FRAME;

    @SuppressWarnings("unused")
    private static byte[] LAST_RESORT = new byte[256*1024];
    private static Logger ROOT_LOGGER = Logger.getLogger("");
    private static String LOGGING_FILE_NAME = "chloroplastJ.log";


    public static void main(String[] args)
    {
        String userDirectory = System.getProperty("user.dir");
        System.setProperty("jna.library.path", userDirectory + System.getProperty("file.separator")+"lib");

        initializeLogger();

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
                catch(Exception | UnsatisfiedLinkError e) 
                {
                    ROOT_LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    e.printStackTrace();
                }

                CURRENT_FRAME = new MainFrame();
                CURRENT_FRAME.setVisible(true);
            }
        });	
    }

    private static void initializeLogger()
    {
        try {
            FileHandler fh = new FileHandler(LOGGING_FILE_NAME);
            ROOT_LOGGER.addHandler(fh);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    public static void handleOOME(OutOfMemoryError oome)
    {
        LAST_RESORT = null;

        Object[] options = {"Yes", "No"};
        int n = JOptionPane.showOptionDialog(CURRENT_FRAME, "AtomicJ has run out of memory. The application will probably not work properly now. Do you want to close it?","Out of memory error", JOptionPane.YES_NO_OPTION,JOptionPane.ERROR_MESSAGE,
                null,  options, options[0]); 

        if(n == 0)
        {
            System.exit(-1);
        }
    }
}

