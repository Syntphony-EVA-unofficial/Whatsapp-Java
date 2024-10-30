package com.nttdata.eva.whatsapp.model;
import java.util.List;

import lombok.Data;

@Data
public class BrokerConfiguration {
    
    private Meta metaConfig;
    private EVA evaConfig;
    private STT STTConfig;
    private MessageLoger messageLogerConfig;


    @Data
    public static class MessageLoger
    {
        private String url;
        private Boolean enabled;
    }

    @Data
    public static class STT
    {
        private String url;
        private Boolean enabled;
        private List<String> supportedLanguages;
    }

    @Data
    public static class Meta
    {
        private String accessToken;
        private String appSecret; 
        private String phoneID;
    }

    @Data
    public static class EVA 
    {
        private Organization organization;
        private Environment environment;
        private Bot bot;
        

        @Data
        public static class Bot {
            private String uuid;
            private String channeluuid;
        }

        @Data
        public static class Organization {
            private String name;
            private String keycloak;
            private String uuid;
        }

        @Data
        public static class Environment {
            private String clientId;
            private String secret;
            private String apikey;
            private String uuid;
            private String instance;
        }
    }
        
}

