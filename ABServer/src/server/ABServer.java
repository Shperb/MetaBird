//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package server;

import client.logger.PlayerInfo;
import io.LoadScoreList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import proxy.Proxy;

public class ABServer {
    ServerSocket providerSocket;
    Socket connection = null;
    OutputStream out;
    InputStream in;
    private static Proxy agentProxy;
    public static byte round = 1;
    public static int group_count = 1;
    public static int current_group = -1;
    public static byte level = 21;
    public static byte time_limit = 0;
    public static int test_time_limit = 0;
    public static String file_dir = "";
    public static PlayerInfo player_info;
    public static ConcurrentHashMap<Integer, Integer> global;
    public int start = -1;

    ABServer() {
        initialize();
    }

    public static void initialize() {
        player_info = new PlayerInfo();
        global = new ConcurrentHashMap();
        current_group = group_count++;
        System.out.println();

        for(int i = 1; i <= level; ++i) {
            global.put(i, 0);
        }

    }

    ABServer(boolean loadwithstate) {
        if (loadwithstate) {
            LoadServerState();
        } else {
            initialize();
        }

    }

    void run() {
        try {
            this.providerSocket = new ServerSocket(2018, 10);
            System.out.println("AB Server waiting for connection");
            if (agentProxy == null) {
                try {
                    agentProxy = new Proxy(9000) {
                        public void onOpen() {
                            System.out.println(" A Chrome Window is connected to the agent proxy");
                        }

                        public void onClose() {
                            System.out.println("A Chrome Window is disconnected from the agent proxy");
                        }
                    };
                    agentProxy.start();
                    System.out.println("The Chrome Server started on port: " + agentProxy.getPort());
                } catch (UnknownHostException var13) {
                    var13.printStackTrace();
                }
            }

            System.out.println("Round_Info: " + round);
            if (file_dir != "") {
                System.out.println("Load Best Scores");
                int[] scores = LoadScoreList.loadScores(file_dir);

                int i;
                for(i = 0; i < scores.length; ++i) {
                    global.put(i + 1, scores[i]);
                }

                for(i = 0; i < scores.length; ++i) {
                    System.out.print(" level " + (i + 1) + " : " + global.get(i + 1) + "  ");
                }
            }

            ABServerManager ABM = new ABServerManager();
            Thread t_1 = new Thread(ABM);
            t_1.start();

            while(true) {
                System.out.println(" waiting for agent  ");
                Socket agentSocket = this.providerSocket.accept();
                Agent agent = new Agent(agentSocket, agentProxy);
                System.out.println(" agent connection established");
                ABM.createAgentThread(agent);
            }
        } catch (IOException var14) {
            var14.printStackTrace();
            SaveServerState();
        } finally {
            try {
                this.in.close();
                this.out.close();
                this.providerSocket.close();
            } catch (IOException var12) {
                var12.printStackTrace();
                SaveServerState();
            }

        }

    }

    public static synchronized void updateGlobalScore(int level, int score, int player) {
        if (round > 2) {
            if (!global.containsKey(level)) {
                global.put(level, score);
            } else if ((Integer)global.get(level) <= score) {
                global.put(level, score);
            }
        }

    }

    protected static void SaveServerState() {
    }

    private static void LoadServerState() {
    }

    public static String[] extract(String[] commands) {
        if (commands.length < 2) {
            return null;
        } else {
            String _option = commands[0];
            String _value = commands[1];
            int value;
            if (_option.equalsIgnoreCase("-t")) {
                value = Integer.parseInt(_value);
                if (value > 127) {
                    time_limit = 127;
                    test_time_limit = 127;
                } else if (value <= 0) {
                    time_limit = 0;
                    test_time_limit = -1;
                } else {
                    time_limit = (byte)value;
                    test_time_limit = value;
                }
            } else if (_option.equals("-n")) {
                int level = Byte.parseByte(_value);
                if (level > 21) {
                    System.out.println(" Invalid Level: the maximum number of levels is 21. Will use 21 instead ");
                } else {
                    level = Byte.parseByte(_value);
                }
            } else if (_option.equalsIgnoreCase("-r")) {
                value = Integer.parseInt(_value);
                if (value < 5) {
                    round = (byte)value;
                } else {
                    System.out.println(" Invalid Round Info: choose from [0-5]");
                }
            } else if (_option.equalsIgnoreCase("-h")) {
                file_dir = _value;
            }

            String[] _commands = new String[commands.length - 2];
            System.arraycopy(commands, 2, _commands, 0, _commands.length);
            extract(_commands);
            return null;
        }
    }

    public static void main(String[] args) {
        ABServer server = new ABServer(false);
        time_limit = -1;
        test_time_limit = -1;
        extract(args);
        server.run();
    }
}
