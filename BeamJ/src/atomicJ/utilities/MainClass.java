package atomicJ.utilities;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitUtilities;
import chloroplastInterface.Nicaiu;
import chloroplastInterface.flipper.ThorlabsFlipperMotionControl;

public class MainClass 
{
    private static void writeAnalogueOut(Pointer signalTask, double value) 
    {
        int errorStart = Nicaiu.INSTANCE.DAQmxStartTask(signalTask);                      
        int errorRead = Nicaiu.INSTANCE.DAQmxWriteAnalogScalarF64(signalTask, new NativeLong(0), 100.0, value, null);       
    }

    //https://github.com/dspsandbox/Canvas/blob/master/Zynq_7010/Zynq_PS/constantsLoader.c
    public static void main(String[] args) throws InterruptedException
    {   
        
        System.out.println(Double.toHexString(-0.1607687662093));
        
        PrefixedUnit unit = UnitUtilities.getSIUnit("Transmittance (%)");
        System.out.println(unit.getFullName());
        
        //        List<NIDevice> devices = NIDevice.getAvailableNIDevices();
        //        
        //        for(NIDevice dev : devices)
        //        {
        //            if(!dev.isSimulated())
        //            {
        //                System.out.println("DEV "+dev.getName());
        //            }
        //            else
        //            {
        //                continue;
        //            }
        //            
        //            String TASK_NAME = "VoltageSignal";
        //            
        //            PointerByReference taskHandleRef = new PointerByReference();
        //            Nicaiu.INSTANCE.DAQmxCreateTask(TASK_NAME, taskHandleRef);
        //            Pointer signalTask = taskHandleRef.getValue();
        //            
        //            int aoChannelIndex = 0;
        //            double minOutputVoltage = -10.0;
        //            double maxOutputVoltage = 10.0;
        //            
        //            String channelDescription = dev.getChannelDescriptionForAnalogOutputChannel(aoChannelIndex);
        //            
        //            int errorCreateChannel=Nicaiu.INSTANCE.DAQmxCreateAOVoltageChan(signalTask, channelDescription, "", minOutputVoltage, maxOutputVoltage, Nicaiu.DAQmx_Val_Volts, null);
        //      
        //            System.out.println("errorCreateChannel A "+errorCreateChannel);
        //            
        //            writeAnalogueOut(signalTask, 5);          
        //        
        //            Thread.sleep(5000);
        //
        //            writeAnalogueOut(signalTask, 0);          
        //        }
        //        
        //        

        //   

        //   


        //        String valueString = "-4.380018E-09";
        //        double val = Double.parseDouble(valueString);
        //        
        //        System.out.println(val);

        //        String resourceName = "ASRL10::INSTR";
        //        
        //        KeithleyPicoammeterSCPIController controller = new KeithleyPicoammeterSCPIController(resourceName);
        //        controller.initialize();
        //        controller.prepareInstrumentAndPerformZeroCorrection();
        //        
        //        for(int i = 0; i<10;i++)
        //        {
        //            try {
        //                UnitExpression reading = controller.readCurrent();
        //                System.out.println(reading.toString());
        //            } catch (JVisaException e) {
        //                // TODO Auto-generated catch block
        //                e.printStackTrace();
        //            }
        //            
        //        }
        //        
        //        JVisaResourceManager resourceManager;
        //
        //        try {
        //            resourceManager = new JVisaResourceManager();
        //        } catch (JVisaException ex) {
        //            System.err.println("Couldn't open the default resource manager");
        //            ex.printStackTrace();
        //            return;
        //        } catch (UnsatisfiedLinkError err) {
        //            System.err.println("Couldn't load nivisa.dll");
        //            err.printStackTrace();
        //            return;
        //        }
        //        
        //        try {
        //            JVisaInstrument instrumentSerial = resourceManager.openInstrument(resourceName);
        //            
        //          //  instrumentSerial.setTimeout(200000);
        //            
        //           // ByteBuffer instrumentIdentity = instrumentSerial.sendAndReceiveBytes("*IDN?\n");
        //            
        //            
        //            instrumentSerial.write("*RST\n");
        //            Thread.sleep(2000);
        //
        //            instrumentSerial.write("SYST:ZCH ON\n");
        //            Thread.sleep(2000);
        //
        //            instrumentSerial.write("RANG:AUTO ON\n");
        //            Thread.sleep(2000);
        //
        //            instrumentSerial.write("INIT\n");
        //            Thread.sleep(2000);
        //
        //            instrumentSerial.write("SYST:ZCOR:ACQ\n");
        //            Thread.sleep(2000);
        //
        //            instrumentSerial.write("SYST:ZCOR ON\n");
        //            Thread.sleep(2000);
        //
        //            instrumentSerial.write("RANG:AUTO ON\n");
        //            Thread.sleep(2000);
        //
        //            instrumentSerial.write("SYST:ZCH OFF\n");
        //            
        //            instrumentSerial.write("NPLC 50\n");
        //
        //            for(int i = 0; i<10;i++)
        //            {
        //                Thread.sleep(1000);
        //                ByteBuffer reading = instrumentSerial.sendAndReceiveBytes("READ?\n");           
        //                Charset charset = Charset.forName("UTF-8");
        //                System.out.println(charset.decode(reading).toString());
        //            }
        //
        //
        //          //  String newContent = charset.decode(instrumentIdentity).toString();
        //            
        //
        //            
        //        } catch (JVisaException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        //        
        //        final String[] resourceNames;
        //        try {
        //            resourceNames = resourceManager.findResources();
        //        } catch (JVisaException ex) {
        //            System.err.println("Couldn't find any VISA resources");
        //            ex.printStackTrace();
        //            return;
        //        }
        //        final int foundCount = resourceNames.length;
        //        if (foundCount < 1) {
        //            System.err.println("Couldn't find any VISA resources");
        //            return;
        //        }
        //        
        //        System.out.printf("Found %d VISA instrument(s):\n", foundCount);
        //        for (int i = 0; i < foundCount; i++) {
        //            try {
        //                final JVisaInstrument instrument = resourceManager.openInstrument(resourceNames[i]);
        //                System.out.printf("%d / %d: \"%s\"\n", i + 1, foundCount, instrument.sendAndReceiveString("*IDN?"));
        //                instrument.close();
        //            } catch (JVisaException ex) {
        //                ex.printStackTrace();
        //            }
        //
        //        }
        //
        //        try {
        //            resourceManager.close();
        //        } catch (JVisaException ex) {
        //            ex.printStackTrace();
        //        }

        ThorlabsFlipperMotionControl flipper = ThorlabsFlipperMotionControl.INSTANCE;
        int response = flipper.TLI_BuildDeviceList();
        int deviceCount = flipper.TLI_GetDeviceListSize();
        System.out.println("deviceCount "+deviceCount);

        int openResponse = flipper.FF_Open("37004582");

        System.out.println("openResponse "+openResponse);

        boolean connected = flipper.FF_CheckConnection("37004582");
        System.out.println("connected "+connected);

        int res = flipper.FF_MoveToPosition("37004582", 1);
        System.out.println(res);
        flipper.FF_MoveToPosition("37004582", 2);
        //
        //        System.out.println(flipper.FF_GetPosition("37004582"));

        //        int closeResponse = flipper.FF_Close("37004582");
        //        System.out.println("closeResponse "+closeResponse);

        //
        //        int x28 = (0xffffffc8 << 4) >> 4;//we need to define x28 as a long number, otherwise x28*x28 is too large
        //        System.out.println(Integer.toBinaryString(0xffffffc8));
        //        System.out.println(Integer.toBinaryString((0xffffffc8 << 4)));
        //        System.out.println(Integer.toBinaryString((0xffffffc8 << 4)>>4));
        //
        //        System.out.println(x28);
        //        System.out.println(Integer.toBinaryString(0xffffffc8));

        //        int port = 22;
        //        String userName = "root";
        //        String password = "root";
        //        String host = "rp-f0882c.local";
        //        
        //        RedPitayaCommunicationChannel comm = new RedPitayaCommunicationChannel(userName, password, host, port);
        //        RedPitayaLockInDevice device = new RedPitayaLockInDevice(comm);
        //        
        //        try {
        //            System.out.println("DUPA");
        //            //System.out.println(RedPitayaLockInDevice.checkIfLockInApplicationInstalled(comm));
        //            comm.sendCommandToRedPitayaAndGetResponse("cd");
        //        } catch (JSchException | IOException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        //        device.initializeSource();

        //        String LOCK_IN_INITIALIZATION_COMMAND = "cat /opt/redpitaya/www/apps/lock_in+pid/red_pitaya.bit > /dev/xdevcfg";
        //        String PYTHON_LOCK_IN_INITIAL_SETTINGS_COMMAND = "cd /opt/redpitaya/www/apps/lock_in+pid/py/;./lock.py signal_sw 0;./lock.py sg_amp1 0;./lock.py gen_mod_phase 0;./lock.py out1_sw 5;./lpf_F1 45";

        //        try {
        //            comm.sendCommandToRedPitayaAndGetResponse(LOCK_IN_INITIALIZATION_COMMAND);
        //            String resp = comm.sendCommandToRedPitayaAndGetResponse(PYTHON_LOCK_IN_INITIAL_SETTINGS_COMMAND);
        //            System.out.println(resp);
        //        } catch (JSchException | IOException e1) {
        //            // TODO Auto-generated catch block
        //            e1.printStackTrace();
        //        }
        //        for(int i = 0; i<10; i++)
        //        {
        //            double t1 = System.currentTimeMillis();
        //            RawVoltageSample sample = device.getSample();
        //            double t2 = System.currentTimeMillis();
        //            double elapsed= t2 - t1;
        //            System.out.println("ELAPSED "+elapsed);
        //            System.out.println(sample.getValueInVolts());
        //        }
        //       
        //        comm.close();

        //        String username = "root";
        //        String password = "root";
        //        String host = "rp-f0882c.local";
        //        int port = 22;
        //        String command = "/opt/redpitaya/www/apps/lock_in+pid/py/thinHugoSmall.py";
        //
        //        
        //        try {
        //           
        //            long t1 = System.currentTimeMillis();
        //            String result = RedPitayaCommunicationChannel.readRedPitayaResponseUsingOutputChannel(username, password, host, port,command);
        //      
        //            long t2 = System.currentTimeMillis();
        //            System.out.println("ELAPSED " + (t2 - t1));
        //            
        //            System.out.println(result);
        //        } catch (Exception e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        //        
        //        RedPitayaCommunicationChannel source = new RedPitayaCommunicationChannel(username, password, host);
        //        try {
        //            
        //            long t1 = System.currentTimeMillis();
        //            String result = source.sendCommandToRedPitayaAndGetResponse(command);
        //      
        //            long t2 = System.currentTimeMillis();
        //            System.out.println("ELAPSED B " + (t2 - t1));
        //            
        //            System.out.println(result);
        //        } catch (Exception e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        //        
        //        try {
        //            
        //            long t1 = System.currentTimeMillis();
        //            String result = source.sendCommandToRedPitayaAndGetResponse(command);
        //      
        //            long t2 = System.currentTimeMillis();
        //            System.out.println("ELAPSED C " + (t2 - t1));
        //            
        //            System.out.println(result);
        //        } catch (Exception e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        //        
        //        
        //        try {
        //            
        //            long t1 = System.currentTimeMillis();
        //            String result = source.sendCommandToRedPitayaAndGetResponse(command);
        //      
        //            long t2 = System.currentTimeMillis();
        //            System.out.println("ELAPSED D " + (t2 - t1));
        //            
        //            System.out.println(result);
        //        } catch (Exception e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        //        
        //        
        //        source.close();
        //
        //        List<NIDevice> devices = NIDevice.getAvailableNIDevices();
        //
        //        for(NIDevice dev : devices)
        //        {
        //            System.out.println(dev.toString());
        //        }
        //
        //        NIDevice niDevice = devices.get(0);
        //        int numberOfPointsInWaveform = 1000;
        //        NIDAQmxWaveformGeneration generator = new NIDAQmxWaveformGeneration(niDevice, numberOfPointsInWaveform);
        //
        //        generator.startGeneration();

        //        // Make a URL to the web page
        //        URL url = null;
        //        try {
        //            url = new URL("https://sourceforge.net/projects/jrobust/best_release.json");
        //        } catch (MalformedURLException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        //
        //        // Get the input stream through URL Connection
        //        URLConnection con = null;
        //        try {
        //            con = url.openConnection();
        //        } catch (IOException e1) {
        //            // TODO Auto-generated catch block
        //            e1.printStackTrace();
        //        }
        //        InputStream is = null;
        //        try {
        //            is = con.getInputStream();
        //        } catch (IOException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        //
        //        // Once you have the Input Stream, it's just plain old Java IO stuff.
        //
        //        // For this case, since you are interested in getting plain-text web page
        //        // I'll use a reader and output the text content to System.out.
        //
        //        // For binary content, it's better to directly read the bytes from stream and write
        //        // to the target file.
        //
        //
        //        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        //
        //        String line = null;
        //
        //        // read each line and write to System.out
        //        try {
        //            while ((line = br.readLine()) != null) {
        //                System.out.println(line);
        //            }
        //        } catch (IOException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
    }
}   
