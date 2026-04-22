package nl.dcraft.ambientthoughts;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MessageLoader {

    private static final String DEFAULT_MESSAGE_RESOURCE = "/assets/ambientthoughts/messages.json";

    private final Random random = new Random();
    private Map<String, List<String>> messagesByCategory = Collections.emptyMap();

    public void load() {
        ensureExternalMessageFileExists();

        if (Config.messagesFile != null && Config.messagesFile.exists()) {
            loadFromExternalFile();
            return;
        }

        loadFromBundledResource();
    }

    private void ensureExternalMessageFileExists() {
        if (Config.messagesFile == null) {
            AmbientThoughts.LOG.error("Config.messagesFile is null, cannot prepare external messages.json");
            return;
        }

        if (Config.messagesFile.exists()) {
            return;
        }

        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            inputStream = MessageLoader.class.getResourceAsStream(DEFAULT_MESSAGE_RESOURCE);
            if (inputStream == null) {
                AmbientThoughts.LOG.error("Could not find default message resource: " + DEFAULT_MESSAGE_RESOURCE);
                return;
            }

            outputStream = new FileOutputStream(Config.messagesFile);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();

            AmbientThoughts.LOG
                .info("Created default external messages file at " + Config.messagesFile.getAbsolutePath());
        } catch (Exception e) {
            AmbientThoughts.LOG.error("Failed to create external messages.json", e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception ignored) {}

            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ignored) {}
        }
    }

    private void loadFromExternalFile() {
        Reader reader = null;

        try {
            reader = new InputStreamReader(new FileInputStream(Config.messagesFile), StandardCharsets.UTF_8);
            messagesByCategory = parseMessages(reader);

            AmbientThoughts.LOG.info("Loaded external messages from " + Config.messagesFile.getAbsolutePath());
            AmbientThoughts.LOG.info("Loaded Ambient Thoughts message categories: " + messagesByCategory.keySet());
        } catch (Exception e) {
            AmbientThoughts.LOG.error("Failed to load external messages.json", e);
            messagesByCategory = Collections.emptyMap();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception ignored) {}
        }
    }

    private void loadFromBundledResource() {
        InputStream inputStream = null;
        Reader reader = null;

        try {
            inputStream = MessageLoader.class.getResourceAsStream(DEFAULT_MESSAGE_RESOURCE);
            if (inputStream == null) {
                AmbientThoughts.LOG.error("Could not find bundled message resource: " + DEFAULT_MESSAGE_RESOURCE);
                messagesByCategory = Collections.emptyMap();
                return;
            }

            reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            messagesByCategory = parseMessages(reader);

            AmbientThoughts.LOG.info("Loaded bundled messages from " + DEFAULT_MESSAGE_RESOURCE);
            AmbientThoughts.LOG.info("Loaded Ambient Thoughts message categories: " + messagesByCategory.keySet());
        } catch (Exception e) {
            AmbientThoughts.LOG.error("Failed to load bundled messages.json", e);
            messagesByCategory = Collections.emptyMap();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception ignored) {}

            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ignored) {}
        }
    }

    private Map<String, List<String>> parseMessages(Reader reader) {
        Type mapType = new TypeToken<Map<String, List<String>>>() {}.getType();
        Map<String, List<String>> parsedMessages = new Gson().fromJson(reader, mapType);

        if (parsedMessages == null) {
            return Collections.emptyMap();
        }

        return parsedMessages;
    }

    public String getRandomMessageTemplate() {
        List<String> allMessages = new ArrayList<String>();

        for (MessageCategory category : MessageCategory.values()) {
            List<String> categoryMessages = getMessages(category);
            if (!categoryMessages.isEmpty()) {
                allMessages.addAll(categoryMessages);
            }
        }

        if (allMessages.isEmpty()) {
            return "";
        }

        return allMessages.get(random.nextInt(allMessages.size()));
    }

    public String getRandomMessageTemplate(MessageCategory category) {
        List<String> categoryMessages = getMessages(category);

        if (categoryMessages.isEmpty()) {
            return "";
        }

        return categoryMessages.get(random.nextInt(categoryMessages.size()));
    }

    public String getRandomJoinMessageTemplate() {
        List<String> joinMessages = getMessages("join_messages");

        if (joinMessages.isEmpty()) {
            return "";
        }

        return joinMessages.get(random.nextInt(joinMessages.size()));
    }

    public List<String> getMessages(MessageCategory category) {
        return getMessages(category.getJsonKey());
    }

    public List<String> getMessages(String jsonKey) {
        if (messagesByCategory == null) {
            return Collections.emptyList();
        }

        List<String> messages = messagesByCategory.get(jsonKey);
        if (messages == null) {
            return Collections.emptyList();
        }

        return messages;
    }
}
