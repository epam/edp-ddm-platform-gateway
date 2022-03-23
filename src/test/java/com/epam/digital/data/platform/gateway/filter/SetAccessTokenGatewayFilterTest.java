/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.gateway.filter;

import com.epam.digital.data.platform.gateway.exception.KeycloakCommunicationException;
import com.epam.digital.data.platform.gateway.exception.VaultDataRetrievingException;
import com.epam.digital.data.platform.gateway.model.KeycloakAuthRequestInfo;
import com.epam.digital.data.platform.gateway.service.TokenManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.token.TokenManager;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.vault.VaultException;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.Map;

import static com.epam.digital.data.platform.gateway.filter.SetAccessTokenGatewayFilter.VAULT_PREFIX;
import static com.epam.digital.data.platform.gateway.filter.factory.SetAccessTokenGatewayFilterFactory.*;
import static com.epam.digital.data.platform.gateway.util.GatewayConstants.REGISTRY_NAME_URL_PATH_VARIABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.vault.core.VaultKeyValueOperationsSupport.*;

@ExtendWith(MockitoExtension.class)
class SetAccessTokenGatewayFilterTest {

  private static final String CLIENT_REGISTRY = "client";
  private static final String TARGET_REGISTRY = "target";
  private static final String TOKEN_HEADER = "X-Access-Token";
  private static final String TOKEN_VALUE = "token";

  @Mock
  private VaultTemplate vaultTemplate;
  @Mock
  private TokenManagerFactory tokenManagerFactory;

  @Mock
  private GatewayFilterChain gatewayFilterChain;

  @Mock
  private VaultKeyValueOperations vaultKeyValueOperations;
  @Mock
  private TokenManager tokenManager;

  private SetAccessTokenGatewayFilter setAccessTokenGatewayFilter;

  @BeforeEach
  void beforeEach() {
    var tokenConfig = new TokenConfig();
    tokenConfig.setHeader(TOKEN_HEADER);

    setAccessTokenGatewayFilter = new SetAccessTokenGatewayFilter(CLIENT_REGISTRY, tokenConfig, vaultTemplate, tokenManagerFactory);

    when(vaultTemplate.opsForKeyValue(VAULT_PREFIX, KeyValueBackend.KV_2))
            .thenReturn(vaultKeyValueOperations);
  }

  @Test
  void expectThirdPartyAccessTokenAddedToRequest() {
    var vaultResponse = new VaultResponseSupport<KeycloakAuthRequestInfo>();
    var keycloakAuthInfo = mockAuthRequestInfo();
    vaultResponse.setData(keycloakAuthInfo);
    when(vaultKeyValueOperations.get(TARGET_REGISTRY + "/" + CLIENT_REGISTRY,
            KeycloakAuthRequestInfo.class))
        .thenReturn(vaultResponse);
    when(tokenManagerFactory.createTokenManager(keycloakAuthInfo)).thenReturn(tokenManager);
    when(tokenManager.getAccessTokenString()).thenReturn(TOKEN_VALUE);

    var exchange = mockExchange();

    setAccessTokenGatewayFilter.filter(exchange, gatewayFilterChain);

    assertThat(exchange.getRequest().getHeaders())
            .containsEntry(TOKEN_HEADER, Collections.singletonList(TOKEN_VALUE));
  }

  @Test
  void expectVaultExceptionIfSecretNotFound() {
    var vaultResponse = new VaultResponseSupport<KeycloakAuthRequestInfo>();
    when(vaultKeyValueOperations.get(TARGET_REGISTRY + "/" + CLIENT_REGISTRY,
            KeycloakAuthRequestInfo.class))
            .thenReturn(vaultResponse);

    var exchange = mockExchange();

    var actualEx =
        assertThrows(
            VaultDataRetrievingException.class,
            () -> setAccessTokenGatewayFilter.filter(exchange, gatewayFilterChain).block());

    assertThat(actualEx.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void expectExceptionIfErrorInCommunicationWithVault() {
    when(vaultKeyValueOperations.get(
            TARGET_REGISTRY + "/" + CLIENT_REGISTRY, KeycloakAuthRequestInfo.class))
        .thenThrow(new VaultException(""));

    var exchange = mockExchange();

    var actualEx =
        assertThrows(
            VaultDataRetrievingException.class,
            () -> setAccessTokenGatewayFilter.filter(exchange, gatewayFilterChain).block());

    assertThat(actualEx.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void expectExceptionIfErrorInCommunicationWithKeycloak() {
    var vaultResponse = new VaultResponseSupport<KeycloakAuthRequestInfo>();
    var keycloakAuthInfo = mockAuthRequestInfo();
    vaultResponse.setData(keycloakAuthInfo);
    when(vaultKeyValueOperations.get(
            TARGET_REGISTRY + "/" + CLIENT_REGISTRY, KeycloakAuthRequestInfo.class))
        .thenReturn(vaultResponse);
    when(tokenManagerFactory.createTokenManager(keycloakAuthInfo))
        .thenThrow(new RuntimeException());

    var exchange = mockExchange();

    var actualEx =
        assertThrows(
            KeycloakCommunicationException.class,
            () -> setAccessTokenGatewayFilter.filter(exchange, gatewayFilterChain).block());

    assertThat(actualEx.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private ServerWebExchange mockExchange() {
    var exchange =
            MockServerWebExchange.builder(
                    MockServerHttpRequest.get("url")
                            .header(TOKEN_HEADER, "OldValue")
                            .build())
                    .build();
    exchange.getAttributes().put(ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
            Map.of(REGISTRY_NAME_URL_PATH_VARIABLE, TARGET_REGISTRY));
    return exchange;
  }

  private KeycloakAuthRequestInfo mockAuthRequestInfo() {
    var requestInfo = new KeycloakAuthRequestInfo();
    requestInfo.setUrl("url");
    requestInfo.setRealm("realm");
    requestInfo.setClientId("client");
    requestInfo.setClientSecret("secret");
    return requestInfo;
  }
}
