import Reader.SettingsReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SettingsReaderTest {
    private static final String TEST_SETTINGS_PATH = "test-settings.properties";
    private SettingsReader settingsReader;

    @BeforeEach
    void setUp() throws IOException {
        createTestPropertiesFile("port=8080\nhost=localhost");
        settingsReader = new SettingsReader(TEST_SETTINGS_PATH);
    }

    private void createTestPropertiesFile(String content) throws IOException {
        try (FileWriter writer = new FileWriter(TEST_SETTINGS_PATH)) {
            writer.write(content);
        }
    }

    @Test
    void testGetPort() {
        assertEquals(8080, settingsReader.getPort(), "Port should be 8080.");
    }

    @Test
    void testGetHost() {
        assertEquals("localhost", settingsReader.getHost(), "Host should be localhost.");
    }


    @Test
    void testSettingsFileNotFound() {
        String invalidPath = "invalid-path.properties";
        Executable executable = () -> new SettingsReader(invalidPath);
        assertThrows(IOException.class, executable);
    }

    @Test
    void testMissingProperty() throws IOException {
        createTestPropertiesFile("port=8080");
        settingsReader = new SettingsReader(TEST_SETTINGS_PATH);

        assertNull(settingsReader.getHost(), "Host should be null if property is missing.");
    }

    @AfterEach
    void tearDown() {
        new File(TEST_SETTINGS_PATH).delete();
    }
}