import Logger.NetworkLogger;
import Reader.SettingsReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionHandlerTest {
    Server server = new Server(Mockito.mock(SettingsReader.class), Mockito.mock(NetworkLogger.class));
    private Server.ConnectionHandler connectionHandler;


    @BeforeEach
    void setUp() {
        Socket mockSocket = Mockito.mock(Socket.class);
        connectionHandler = server.new ConnectionHandler(mockSocket);
    }

    @Test
    void testNullNickname() {
        assertFalse(connectionHandler.isValidNickname(null));
    }

    @Test
    void testEmptyNickname() {
        assertFalse(connectionHandler.isValidNickname(""));
    }

    @Test
    void testValidNicknames() {
        assertTrue(connectionHandler.isValidNickname("john_doe"));
        assertTrue(connectionHandler.isValidNickname("a123"));
        assertTrue(connectionHandler.isValidNickname("_username_"));
    }

    @Test
    void testInvalidNicknames() {
        assertFalse(connectionHandler.isValidNickname("john doe"));
        assertFalse(connectionHandler.isValidNickname("john#doe"));
        assertFalse(connectionHandler.isValidNickname("john+doe"));
        assertFalse(connectionHandler.isValidNickname("john@doe"));
    }


}
