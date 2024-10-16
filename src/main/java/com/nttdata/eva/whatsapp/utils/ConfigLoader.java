package com.nttdata.eva.whatsapp.utils;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.nttdata.eva.whatsapp.model.BrokerConfiguration;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
public class ConfigLoader {
    
    @Value("${BROKER_CONFIGS_PATH}")
    private String brokerConfigsPath;

    private Map<String, BrokerConfiguration> brokerConfigs = new HashMap<>();

    @PostConstruct
    public void init() {
            try {
    
                // Load properties files from the brokerConfigs directory
                Path configDir = Paths.get(brokerConfigsPath);
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(configDir, "*.properties")) {
                    for (Path entry : stream) {
                        loadProperties(entry);
                    }
                }
            } catch (IOException e) {
                log.error("Error executing bash script or loading properties files", e);
            }
        }

    private void loadProperties(Path path) {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
            log.info("Loaded properties file: " + path.getFileName());
            BrokerConfiguration config = mapPropertiesToConfig(properties);
            String key = config.getMetaConfig().getPhoneID(); // Use a unique key, e.g., organization UUID
            brokerConfigs.put(key, config);
        } catch (IOException ex) {
            throw new RuntimeException("Error loading properties file: " + path, ex);
        }
    }

    private BrokerConfiguration mapPropertiesToConfig(Properties properties) {
        BrokerConfiguration brokerConfigs = new BrokerConfiguration();

        BrokerConfiguration.Meta metaConfig = new BrokerConfiguration.Meta();
        metaConfig.setAccessToken(properties.getProperty("meta.accesstoken"));
        metaConfig.setAppSecret(properties.getProperty("meta.appsecret"));
        metaConfig.setPhoneID(properties.getProperty("meta.phoneid"));

        BrokerConfiguration.EVA evaConfig = new BrokerConfiguration.EVA();

        BrokerConfiguration.EVA.Organization orgConfig = new BrokerConfiguration.EVA.Organization();
        orgConfig.setName(properties.getProperty("eva.org.name"));
        orgConfig.setKeycloak(properties.getProperty("eva.org.keycloak"));
        orgConfig.setUuid(properties.getProperty("eva.org.uuid"));

        BrokerConfiguration.EVA.Environment envConfig = new BrokerConfiguration.EVA.Environment();
        envConfig.setClientId(properties.getProperty("eva.env.client.id"));
        envConfig.setSecret(properties.getProperty("eva.env.secret"));
        envConfig.setApikey(properties.getProperty("eva.env.apikey"));
        envConfig.setUuid(properties.getProperty("eva.env.uuid"));
        envConfig.setInstance(properties.getProperty("eva.env.instance"));

        BrokerConfiguration.EVA.Bot botConfig = new BrokerConfiguration.EVA.Bot();
        botConfig.setUuid(properties.getProperty("eva.bot.uuid"));
        botConfig.setChanneluuid(properties.getProperty("eva.bot.channeluuid"));

        evaConfig.setOrganization(orgConfig);
        evaConfig.setEnvironment(envConfig);
        evaConfig.setBot(botConfig);


        BrokerConfiguration.STT STTConfig = new BrokerConfiguration.STT();
        STTConfig.setUrl(properties.getProperty("google.stt.url"));
        String stringEnable = properties.getProperty("google.stt.enabled");
        boolean booleanEnabled = stringEnable != null && Boolean.parseBoolean(stringEnable);
        STTConfig.setEnabled(booleanEnabled);

        String supportedLanguages = properties.getProperty("google.stt.supportedlanguages");
        if (supportedLanguages != null) {
            List<String> languagesList = Arrays.asList(supportedLanguages.split("\\s*,\\s*"));
            STTConfig.setSupportedLanguages(languagesList);
        }

        brokerConfigs.setMetaConfig(metaConfig);
        brokerConfigs.setEvaConfig(evaConfig);
        brokerConfigs.setSTTConfig(STTConfig);

        return brokerConfigs;
    }

    public Map<String, BrokerConfiguration> getBrokerConfigs() {
        return brokerConfigs;
    }
}