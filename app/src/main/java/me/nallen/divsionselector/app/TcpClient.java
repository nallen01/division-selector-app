package me.nallen.divsionselector.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

public class TcpClient {
    public static final int CONNECT_OK = 0;
    public static final int CONNECT_ISSUE = 1;
    public static final int CONNECT_ALREADY_CONNECTED = 2;

    public static final int SOCKET_TIMEOUT_MS = 1000;
    public static final int PORT = 5008;

    private static TcpClient singleton = new TcpClient();

    private Socket socket = null;
    private BufferedReader in = null;
    private BufferedWriter out = null;

    private LinkedList<DataListener> _listeners = new LinkedList<DataListener>();
    private boolean isConnected = false;

    private TcpClient() {
    }

    public static TcpClient getInstance() {
        return singleton;
    }

    public synchronized void addDataListener(DataListener listener)  {
        _listeners.add(listener);
    }
    public synchronized void removeDataListener(DataListener listener)   {
        _listeners.remove(listener);
    }

    private synchronized void connectionDropped() {
        Iterator<DataListener> i = _listeners.iterator();
        while(i.hasNext())  {
            i.next().connectionDropped();
        }
    }

    public void cleanUp() {
        isConnected = false;

        try {
            socket.close();
        } catch (Exception e) { }
        socket = null;
        in = null;
        out = null;
    }

    public void logout() {
        cleanUp();
    }

    private boolean sendMessage(String paramString) {
        if (out != null) {
            try {
                out.write(paramString + '\n');
                out.flush();
                return true;
            } catch (Exception e) {}
        }
        return false;
    }

    /*private boolean sendCommand(ScoreField field, MessageType type, int value) {
        return sendMessage("" + field.getValue() + ((char)29) + type.getValue() + ((char)29) + value);
    }*/

    private Thread listener;

    public int connect(String server_ip) {
        if(!isConnected()) {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(server_ip, PORT), SOCKET_TIMEOUT_MS);
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
            catch(Exception e) {
                cleanUp();
                return CONNECT_ISSUE;
            }

            listener = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            String str = in.readLine();

                            if(str == null) {
                                throw new Exception("Connection Dropped");
                            }

                            Thread.sleep(10);
                        }
                        catch (Exception e) {
                            if(isConnected) {
                                logout();
                                connectionDropped();
                            }
                            return;
                        }
                    }
                }
            });
            listener.start();

            isConnected = true;

            return CONNECT_OK;
        }

        return CONNECT_ALREADY_CONNECTED;
    }

    public boolean isConnected() {
        return isConnected;
    }
}