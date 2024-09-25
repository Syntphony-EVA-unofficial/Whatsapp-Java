package com.nttdata.eva.whatsapp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v2.SpeechSettings;
import com.google.cloud.speech.v2.AutoDetectDecodingConfig;
import com.google.cloud.speech.v2.CreateRecognizerRequest;
import com.google.cloud.speech.v2.OperationMetadata;
import com.google.cloud.speech.v2.RecognitionConfig;
import com.google.cloud.speech.v2.RecognizeRequest;
import com.google.cloud.speech.v2.RecognizeResponse;
import com.google.cloud.speech.v2.Recognizer;
import com.google.cloud.speech.v2.SpeechClient;
import com.google.cloud.speech.v2.SpeechRecognitionAlternative;
import com.google.cloud.speech.v2.SpeechRecognitionResult;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.UUID;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
public class AudioSTT {
    private static final Logger logger = LoggerFactory.getLogger(AudioSTT.class);
    private static boolean initialized = false;
    private static List<String> supportedLanguages;
    private static SpeechClient client;
    private static Recognizer recognizer;
    private static RecognitionConfig recognitionConfig;


    public static String transcribeFileV2(String projectId, byte[] audioData) {
        if (!initialized) {
            initialize(projectId);
        }

        ByteString audioBytes = ByteString.copyFrom(audioData);

        RecognizeRequest request = RecognizeRequest.newBuilder()
                .setConfig(recognitionConfig)
                .setRecognizer(recognizer.getName())
                .setContent(audioBytes)
                .build();

        logger.info("Transcribing audio file");
        try {
            RecognizeResponse response = client.recognize(request);
            List<SpeechRecognitionResult> results = response.getResultsList();

            if (!results.isEmpty() && results.get(0).getAlternativesCount() > 0) {
                SpeechRecognitionAlternative alternative = results.get(0).getAlternativesList().get(0);
                return alternative.getTranscript();
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("An error occurred in Transcribe: {}", e.getMessage());
            return null;
        }
    }

    private static void initialize(String projectId) {
        try {
            supportedLanguages = List.of(System.getenv("STT_SUPPORTED_LANGUAGES").split(","));
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                AudioSTT.class.getClassLoader().getResourceAsStream("key.json")
            );

            // Create SpeechSettings with the credentials
            SpeechSettings speechSettings = SpeechSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            // Use the speechSettings to create the SpeechClient
            client = SpeechClient.create(speechSettings);

            String parent = String.format("projects/%s/locations/global", projectId);
            String recognizerId = UUID.randomUUID().toString(); // Generate a unique recognizer ID
            String recognizerPath = String.format("projects/%s/locations/global/recognizers/%s", projectId, recognizerId);

            recognizer = Recognizer.newBuilder().build();

            CreateRecognizerRequest createRecognizerRequest = CreateRecognizerRequest.newBuilder()
                    .setParent(parent)
                    .setRecognizerId(recognizerPath)
                    .setRecognizer(recognizer)
                    .build();

            // Asynchronously create the recognizer
            OperationFuture<Recognizer, OperationMetadata> operationFuture = client.createRecognizerAsync(createRecognizerRequest);

            // Wait for the operation to complete and get the result
            recognizer = operationFuture.get();

            // Initialize the RecognitionConfig
            RecognitionConfig.Builder recognitionConfigBuilder = RecognitionConfig.newBuilder()
                    .setAutoDecodingConfig(AutoDetectDecodingConfig.newBuilder().build());

            for (int i = 0; i < supportedLanguages.size(); i++) {
                recognitionConfigBuilder.setLanguageCodes(i, supportedLanguages.get(i));
            }

            recognitionConfig = recognitionConfigBuilder.build();

            initialized = true;
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Failed to initialize AudioSTT: {}", e.getMessage());
        }
    }

    public static byte[] getDownloadAudio(String audioURL) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(audioURL)
                .addHeader("Authorization", "Bearer " + System.getenv("FACEBOOK_ACCESS_TOKEN"))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Failed to download audio: {}", response);
                return null;
            }
            return response.body().bytes();
        } catch (IOException e) {
            logger.error("An error occurred while downloading audio: {}", e.getMessage());
            return null;
        }
    }

    public static String getAudioURL(String audioID) {
        String url = String.format("https://graph.facebook.com/v19.0/%s", audioID);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + System.getenv("FACEBOOK_ACCESS_TOKEN"))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Failed to get audio URL: {}", response);
                return null;
            }
            Map<String, Object> responseData = new ObjectMapper().readValue(response.body().string(), Map.class);
            logger.info("Data Response: {}", responseData);
            return (String) responseData.get("url");
        } catch (IOException e) {
            logger.error("An error occurred while getting audio URL: {}", e.getMessage());
            return null;
        }
    }
}