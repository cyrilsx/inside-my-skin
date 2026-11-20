package dev.healthhub.ingestion;

public interface ProviderOAuthPort {

    String provider();

    AuthorizationInit startAuthorization();

    ConnectionInfo handleCallback(String code, String state);
}
