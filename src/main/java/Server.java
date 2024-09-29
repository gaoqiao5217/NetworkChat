import Logger.ChatLogger;
import Logger.NetworkLogger;
import Reader.Reader;
import Reader.SettingsReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private final int port;
    private final List<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    private final ChatLogger logger;



    public static void main(String[] args) throws IOException {
        Reader reader = new SettingsReader("settings.txt");
        NetworkLogger logger = new NetworkLogger("serverLog.txt");
        Server server = new Server(reader, logger);
        server.run();
    }

    public Server(Reader settings, ChatLogger logger){
        connections = new ArrayList<>();
        done = false;
        port = settings.getPort();
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(port);
            pool = Executors.newCachedThreadPool();
            String startingLog = "Server started";
            System.out.println("Server started");
            pool.execute(new ServerInput());
            logger.log(startingLog);
            while (!done) {
                Socket client = server.accept();
                System.out.println("Client " + client.getInetAddress().getHostAddress() + ":" + client.getPort() + " connected");
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }

        } catch (IOException e) {
            logger.log(e.getMessage());
            shutdown();
        }
    }

    public void broadcast(String message, ConnectionHandler connectionHandler) {
        for (ConnectionHandler handler : connections) {
            if (handler != null) {
                if (handler.connected) {
                    if (!connectionHandler.equals(handler)) {
                        handler.sendMessage(message);
                    }
                }
            }
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler handler : connections) {
            if (handler != null) {
                if (handler.connected) {
                    handler.sendMessage(message);
                }
            }
        }
    }

    public void shutdown(){
        logger.log("Server shutting down");
        try {
            done = true;
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler handler : connections) {
                handler.shutdown();
            }
        } catch (IOException e) {
            //ignore
        }
    }

    class ConnectionHandler implements Runnable {

        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;
        private boolean connected = false;


        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM HH:mm", Locale.ENGLISH);
                out.println("Please enter your nickname: ");
                String enteredNickname = in.readLine();
                while (!isValidNickname(enteredNickname)) {
                    out.println("Please enter a valid nickname: ");
                    enteredNickname = in.readLine();
                }
                nickname = enteredNickname;
                String welcomeLog = nickname + " joined the chat!";
                System.out.println(welcomeLog);
                broadcast(welcomeLog, this);
                logger.log(welcomeLog);
                out.println("Hello " + nickname + "!");
                connected = true;
                String message;
                while ((message = in.readLine()) != null) {
                    if(message.startsWith("/nick")){
                        out.println("Enter your new nickname: ");
                        String newNickname = in.readLine();
                        while (!isValidNickname(newNickname)) {
                            out.println("Please a valid nickname:");
                            newNickname = in.readLine();
                        }
                        if (!newNickname.equals(nickname)) {
                            String renameLog = nickname + " renamed to " + newNickname;
                            broadcast(renameLog, this);
                            System.out.println(renameLog);
                            logger.log(renameLog);
                            nickname = newNickname;
                            out.println("Successfully renamed to " + newNickname);
                        } else {
                            out.println("New nickname is already in use!");
                        }
                    } else if (message.startsWith("/exit")){
                        String leaveLog = nickname + " left the chat!";
                        broadcast(leaveLog, this);
                        System.out.println(leaveLog);
                        logger.log(leaveLog);
                        shutdown();
                    } else {
                        logger.log(nickname + ": " + message);
                        broadcast(String.format("[%s] %s: %s", dateFormat.format(new Date()), nickname, message), this);
                    }
                }


            } catch (IOException e) {
                logger.log(e.getMessage());
                shutdown();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public boolean isValidNickname(String nickname) {
            String pattern = "^[a-zA-Z0-9_]+$";
            return nickname != null && nickname.matches(pattern);
        }

        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                //Ignore
            }
        }
    }

    class ServerInput implements Runnable {

        @Override
        public void run() {
            try{
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done){
                    String message = inReader.readLine();
                    broadcast("Server: " + message);
                }
            } catch (IOException e){
                shutdown();
            }
        }
    }

}

