package com.epam.digital.data.platform.gateway.service;

import com.epam.digital.data.platform.gateway.model.KeycloakAuthRequestInfo;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.token.TokenManager;
import org.springframework.stereotype.Component;

@Component
public class TokenManagerFactory {

  public TokenManager createTokenManager(KeycloakAuthRequestInfo authRequestInfo) {
    return KeycloakBuilder.builder()
        .serverUrl(authRequestInfo.getUrl())
        .realm(authRequestInfo.getRealm())
        .clientId(authRequestInfo.getClientId())
        .clientSecret(authRequestInfo.getClientSecret())
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .build()
        .tokenManager();
  }
}
