package chloroplastInterface.redPitaya;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class RedPitayaCommunicationChannel
{
    private static final int KEEP_ALIVE_MESSAGE_INTERVAL_IN_MILISECONDS = 60000;
    private static final int DEFAULT_PORT = 22;

    private final String userName;
    private final String host;
    private final String password;

    private JSch sshInstance;
    private final int port;

    private Session lastUsedSession;

    public RedPitayaCommunicationChannel(String userName, String password, String host)
    {
        this(userName, password, host, DEFAULT_PORT);
    }

    public RedPitayaCommunicationChannel(String userName, String password, String host, int port)
    {
        this.userName = userName;
        this.host = host;
        this.sshInstance = new JSch();
        this.password = password;
        this.port= port;
    }

    public int getPort()
    {
        return port;
    }

    public String getHost()
    {
        return host;
    }

    public boolean canEstablishConnection()
    {
        Session session = null;
        try {
            session = getSession();
            session.setPort(port);   
            session.setPassword(password);
            session.connect();
            return true;
        }
        catch(Exception ex)
        {
        }
        finally
        {
            if(session!= null)
            {
                session.disconnect();
            }
        }
        return false;
    }

    public boolean isOpen()
    {
        boolean open = (lastUsedSession) != null ? lastUsedSession.isConnected(): false;
        return open;
    }

    public void refresh() throws JSchException
    {
        if(this.lastUsedSession != null)
        {
            this.lastUsedSession.disconnect();
        }
        this.sshInstance = new JSch();
        this.lastUsedSession = getSession();

    }

    public void close()
    {
        if(lastUsedSession != null)
        {
            lastUsedSession.disconnect();
        }
    }

    private Session retrieveOrCreateIfNecessaryNewSession() throws JSchException
    {
        if(this.lastUsedSession == null)
        {
            this.lastUsedSession = getSession();
        }

        return lastUsedSession;
    }

    private Session getSession() throws JSchException
    {
        Session newSession = sshInstance.getSession(userName, host, port);
        newSession.setPassword(password);
        newSession.setConfig("StrictHostKeyChecking", "no");
        newSession.setServerAliveInterval(KEEP_ALIVE_MESSAGE_INTERVAL_IN_MILISECONDS);

        return newSession;
    }

    private void refreshLastUsedSession() throws JSchException
    {
        if(this.lastUsedSession != null)
        {
            this.lastUsedSession.disconnect();
        }

        this.lastUsedSession = getSession();
    }

    public String sendCommandToRedPitayaAndGetResponse(String command) throws JSchException, IOException 
    {              
        String response=null;

        try {
            response = readRedPitayaResponseWithoutReconecting(command);              
        } 
        catch(JSchException | IOException error)
        {
            error.printStackTrace();
            refreshLastUsedSession();
            response = readRedPitayaResponseWithoutReconecting(command);
        }

        return response;
    }

    public String sendCommandToRedPitayaAndGetExtendedResponse(String command) throws JSchException, IOException 
    {              
        String response=null;

        try {
            response = readRedPitayaExtendedResponseWithoutReconecting(command);              
        } 
        catch(JSchException | IOException error)
        {
            error.printStackTrace();
            refreshLastUsedSession();
            response = readRedPitayaExtendedResponseWithoutReconecting(command);
        }

        return response;
    }

    private String readRedPitayaResponseWithoutReconecting(String command) throws JSchException, IOException 
    {              
        ChannelExec channel = null;
        String response=null;

        try {                          
            Session session = retrieveOrCreateIfNecessaryNewSession();

            if(!session.isConnected())
            {
                session.connect();
            }

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            StringBuilder outputBuffer = new StringBuilder();
            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] tmp = new byte[1024];
            while (true) 
            {
                while (in.available() > 0) 
                {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    outputBuffer.append(new String(tmp, 0, i));
                }

                if (channel.isClosed()) 
                {
                    if (in.available() > 0) continue; 
                    break;
                }
            }
            response = outputBuffer.toString();              
        } 
        finally 
        {
            if (channel != null) {
                channel.disconnect();
            }
        }
        return response;
    }
    private String readRedPitayaExtendedResponseWithoutReconecting(String command) throws JSchException, IOException 
    {              
        ChannelExec channel = null;
        String response=null;

        try {                          
            Session session = retrieveOrCreateIfNecessaryNewSession();

            if(!session.isConnected())
            {
                session.connect();
            }

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            StringBuilder outputBuffer = new StringBuilder();
            InputStream in = channel.getExtInputStream();


            channel.connect();

            byte[] tmp = new byte[1024];
            while (true) 
            {
                while (in.available() > 0) 
                {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    outputBuffer.append(new String(tmp, 0, i));
                }

                if (channel.isClosed()) 
                {
                    if (in.available() > 0) continue; 
                    break;
                }
            }
            response = outputBuffer.toString();              
        } 
        finally 
        {
            if (channel != null) {
                channel.disconnect();
            }
        }
        return response;
    }

    //https://stackoverflow.com/questions/40625441/how-can-i-make-a-session-in-jsch-to-last-longer

    public static String readRedPitayaResponseUsingOutputChannel(String username, String password, String host, int port, String command) throws Exception 
    {              
        Session session = null;
        ChannelExec channel = null;
        String response=null;

        try {
            session = new JSch().getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOutputStream(responseStream);
            channel.connect();

            while (channel.isConnected()) {
                Thread.sleep(100);
            }

            response = new String(responseStream.toByteArray());              
        } 
        finally 
        {
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
        return response;
    }

    public static String readRedPitayaResponseUsingInputChannel(String username, String password, String host, int port, String command) throws Exception 
    {              
        Session session = null;
        ChannelExec channel = null;

        try {
            session = new JSch().getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = (ChannelExec)session.openChannel("exec");
            session.isConnected();
            channel.setCommand(command);

            StringBuilder outputBuffer = new StringBuilder();
            InputStream in = channel.getInputStream();

            channel.connect();

            //#sendKeepAliveMsg()

            long t3 =System.currentTimeMillis();
            byte[] tmp = new byte[1024];
            while (true) 
            {
                while (in.available() > 0) 
                {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    outputBuffer.append(new String(tmp, 0, i));
                }

                if (channel.isClosed()) 
                {
                    if (in.available() > 0) continue; 
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try 
                { 
                    //see https://sourceforge.net/p/jsch/mailman/jsch-users/thread/200806052025.IAF78102.tKGNSSGBPUD%40I-love.SAKURA.ne.jp/#msg19537077
                    Thread.sleep(100);
                } catch (Exception ee) 
                {
                    ee.printStackTrace();
                }
            }

            channel.disconnect();

            long t4 = System.currentTimeMillis();
            long elapsed2= t4 - t3;
            System.out.println("ELAPSED B " + elapsed2);

            return outputBuffer.toString();
        } 
        finally {
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
    }
}
