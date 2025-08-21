package com.wanli.backend.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Configuration
public class GoogleApiConfig {

    private static final String APPLICATION_NAME = "Wanli Backend";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(
            DocsScopes.DOCUMENTS_READONLY,
            DriveScopes.DRIVE_READONLY
    );

    @Value("${google.credentials.file-path:}")
    private String credentialsFilePath;

    @Value("${google.credentials.json:}")
    private String credentialsJson;

    @Bean
    public NetHttpTransport httpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        GoogleCredentials credentials;
        
        if (credentialsJson != null && !credentialsJson.isEmpty()) {
            // 从环境变量中的JSON字符串创建凭据
            InputStream credentialsStream = new java.io.ByteArrayInputStream(credentialsJson.getBytes());
            credentials = ServiceAccountCredentials.fromStream(credentialsStream)
                    .createScoped(SCOPES);
        } else if (credentialsFilePath != null && !credentialsFilePath.isEmpty()) {
            // 从文件路径创建凭据
            InputStream credentialsStream = new java.io.FileInputStream(credentialsFilePath);
            credentials = ServiceAccountCredentials.fromStream(credentialsStream)
                    .createScoped(SCOPES);
        } else {
            // 使用默认凭据（适用于在Google Cloud环境中运行）
            credentials = GoogleCredentials.getApplicationDefault()
                    .createScoped(SCOPES);
        }
        
        return credentials;
    }

    @Bean
    public Docs docsService(NetHttpTransport httpTransport, GoogleCredentials credentials) {
        return new Docs.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Bean
    public Drive driveService(NetHttpTransport httpTransport, GoogleCredentials credentials) {
        return new Drive.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}