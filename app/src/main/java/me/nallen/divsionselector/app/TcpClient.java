package me.nallen.divsionselector.app;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class TcpClient {
    public static final int CONNECT_OK = 0;
    public static final int CONNECT_ISSUE = 1;
    public static final int CONNECT_ALREADY_CONNECTED = 2;

    public static final int SOCKET_TIMEOUT_MS = 1000;
    public static final int PORT = 5008;

    private static final char FIELD_SEPARATOR = ((char) 28);
    private static final char ITEM_SEPARATOR = ((char) 29);

    private static TcpClient singleton = new TcpClient();

    private Socket socket = null;
    private BufferedReader in = null;
    private BufferedWriter out = null;

    private SortedSet<String> teams = new TreeSet<String>();
    private SortedMap<String, Boolean> teamsHaveDivisions = new TreeMap<String, Boolean>();
    private List<String> divisions = new ArrayList<String>();
    private Map<String, SortedSet<String>> divisionTeams = new HashMap<String, SortedSet<String>>();

    private Random rand = new Random();

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

    private synchronized void fireUpdate() {
        Iterator<DataListener> i = _listeners.iterator();
        while(i.hasNext())  {
            i.next().dataUpdated();
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

    private boolean sendCommand(MessageType type) {
        return sendCommand(type, null);
    }

    private boolean sendCommand(MessageType type, String[] fields) {
        String str = "" + type.getValue() + FIELD_SEPARATOR;
        if(fields != null) {
            for(String field : fields) {
                str += field + FIELD_SEPARATOR;
            }
        }
        str = str.substring(0, str.length()-1);
        return sendMessage(str);
    }

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

            try {
                String response = in.readLine();
                if(response.equals("1")) {
                    // Initialise data
                    response = in.readLine();

                    String[] parts = response.split("" + ITEM_SEPARATOR, -1);

                    if(parts.length == 3) {
                        String[] fields = parts[0].split("" + FIELD_SEPARATOR, -1);
                        for(String field : fields) {
                            addDivision(field);
                        }

                        fields = parts[1].split("" + FIELD_SEPARATOR, -1);
                        for(String field : fields) {
                            addTeam(field);
                        }

                        fields = parts[2].split("" + FIELD_SEPARATOR, -1);
                        for(int i=0; i<(fields.length/2); i++) {
                            assignDivisionForTeam(fields[2*i], fields[2*i + 1]);
                        }

                        listener = new Thread(new Runnable() {
                            public void run() {
                                while (true) {
                                    try {
                                        String str = in.readLine();

                                        if(str != null) {
                                            String[] parts = str.split("" + FIELD_SEPARATOR, -1);
                                            if(parts.length > 0) {
                                                MessageType type = MessageType.fromInt(Integer.parseInt(parts[0]));

                                                if(type == MessageType.CLEAR_ALL) {
                                                    clear();
                                                }
                                                else if(type == MessageType.CLEAR_TEAMS) {
                                                    clearTeams();
                                                }
                                                else if(type == MessageType.CLEAR_DIVISIONS) {
                                                    clearDivisions();
                                                }
                                                else if(type == MessageType.ADD_TEAM) {
                                                    if(parts.length == 2) {
                                                        addTeam(parts[1]);
                                                    }
                                                }
                                                else if(type == MessageType.ASSIGN_TEAM) {
                                                    if(parts.length == 3) {
                                                        assignDivisionForTeam(parts[1], parts[2]);
                                                    }
                                                }
                                                else if(type == MessageType.UNASSIGN_TEAM) {
                                                    if(parts.length == 2) {
                                                        removeDivisionForTeam(parts[1]);
                                                    }
                                                }
                                                else if(type == MessageType.ADD_DIVISION) {
                                                    if(parts.length == 2) {
                                                        addDivision(parts[1]);
                                                    }
                                                }
                                                else if(type == MessageType.REMOVE_DIVISION) {
                                                    if(parts.length == 2) {
                                                        removeDivision(parts[1]);
                                                    }
                                                }
                                            }
                                        }
                                        else {
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
                }

            } catch (IOException e) {}

            cleanUp();
            return CONNECT_ISSUE;
        }

        return CONNECT_ALREADY_CONNECTED;
    }

    public boolean isConnected() {
        return isConnected;
    }

    private void clear() {
        clearTeams();
        clearDivisions();
    }

    private void clearTeams() {
        teams.clear();
        teamsHaveDivisions.clear();
        for(String division : divisions) {
            divisionTeams.get(division).clear();
        }

        fireUpdate();
    }

    private void clearDivisions() {
        divisions.clear();
        divisionTeams.clear();

        for(String team : teamsHaveDivisions.keySet()) {
            teamsHaveDivisions.put(team, false);
        }

        fireUpdate();
    }

    public void addDivision(String name) {
        if(!divisions.contains(name)) {
            divisions.add(name);
            divisionTeams.put(name, new TreeSet<String>());

            fireUpdate();
        }
    }

    private void removeDivision(String name) {
        if(divisions.contains(name)) {
            divisions.remove(name);

            for(String team : divisionTeams.get(name)) {
                teamsHaveDivisions.put(team, false);
            }

            divisionTeams.remove(name);

            fireUpdate();
        }
    }

    private void addTeam(String team) {
        if(!teams.contains(team)) {
            teams.add(team);
            teamsHaveDivisions.put(team, false);

            fireUpdate();
        }
    }

    public void requestRemoveDivisionForTeam(String number) {
        sendCommand(MessageType.UNASSIGN_TEAM, new String[]{number});
    }

    private void removeDivisionForTeam(String number) {
        for(String division : divisions) {
            if (divisionTeams.get(division).contains(number)) {
                divisionTeams.get(division).remove(number);
                teamsHaveDivisions.put(number, false);

                fireUpdate();
            }
        }
    }

    public void requestAssignDivisionForTeam(String number, String division) {
        sendCommand(MessageType.ASSIGN_TEAM, new String[]{number, division});
    }

    private void assignDivisionForTeam(String number, String division) {
        if(teams.contains(number)) {
            if(divisions.contains(division)) {
                removeDivisionForTeam(number);

                divisionTeams.get(division).add(number);
                teamsHaveDivisions.put(number, true);

                fireUpdate();
            }
        }
    }

    public void requestRandomiseRemainingTeams() {
        sendCommand(MessageType.RANDOMISE_TEAMS);
    }

    public String[] getAllTeams() {
        return teams.toArray(new String[teams.size()]);
    }

    public String[] getAllDivisions() {
        return divisions.toArray(new String[divisions.size()]);
    }

    public String[] getTeamsForDivision(String division) {
        if(divisionTeams.containsKey(division)) {
            return divisionTeams.get(division).toArray(new String[divisionTeams.get(division).size()]);
        }

        return null;
    }

    public String[] getAllUnassignedTeams() {
        return getTeamsForAssignedStatus(false);
    }

    public String[] getAllAssignedTeams() {
        return getTeamsForAssignedStatus(true);
    }

    private String[] getTeamsForAssignedStatus(Boolean status) {
        List<String> teamList = new ArrayList<String>();

        for(Map.Entry<String, Boolean> e : teamsHaveDivisions.entrySet()) {
            if(e.getValue() == status) {
                teamList.add(e.getKey());
            }
        }

        return teamList.toArray(new String[teamList.size()]);
    }
}