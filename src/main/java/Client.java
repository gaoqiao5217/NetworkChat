import Logger.ChatLogger;
import Logger.NetworkLogger;
import Reader.Reader;
import Reader.SettingsReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
    private final String host;
    private final int port;
    private BufferedReader in;
    private PrintWriter out;
    private Boolean done = false;
    private Socket client;
    private ChatLogger logger;

    public static void main(String[] args) throws IOException {
        Reader reader = new SettingsReader("settings.txt");
        NetworkLogger logger = new NetworkLogger("clientLog.txt");
        Client client = new Client(reader, logger);
        client.run();
    }

    public Client(Reader settings, ChatLogger logger) {
        port = settings.getPort();
        host = settings.getHost();
        this.logger = logger;
    }

    @Override
    public void run() {
        try{
            client = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);

            InputHandler inputHandler = new InputHandler();
            new Thread(inputHandler).start();

            String input;
            //
            while ((input = in.readLine()) != null) {
                logger.log(input);
                System.out.println(input);
            }
            new Thread(inputHandler).start();
        } catch (IOException e){
            shutdown();
        }
    }

    public void shutdown(){
        done = true;
        try{
            in.close();
            out.close();
            if (!client.isClosed()){
                client.close();
            }
        } catch (IOException e) {
            //ignore
        }
    }

    class InputHandler implements Runnable {

        @Override
        public void run() {
            try{
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done){
                    //
                    String message = inReader.readLine();
                    if (message.equals("/exit")){
                        out.println(message);
                        logger.log("You left the chat");
                        inReader.close();
                        shutdown();
                    }else {
                        out.println(message);
                        logger.log(message);
                    }
                }
            } catch (IOException e){
                shutdown();
            }
        }
    }
}
