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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

import com.epam.digital.data.platform.gateway.filter.factory.model.TokenConfig;
import static com.epam.digital.data.platform.gateway.util.GatewayConstants.REGISTRY_NAME_URL_PATH_VARIABLE;
import static org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend;

public class ClientRegistrySetTokenGatewayFilter implements GatewayFilter {

  private static final String VAULT_PATTERN = "%s/%s";
  protected static final String VAULT_PREFIX = "platform-integration";

  private final Logger log = LoggerFactory.getLogger(ClientRegistrySetTokenGatewayFilter.class);

  private final String clientRegistry;
  private final TokenConfig config;
  private final VaultTemplate vaultTemplate;
  private final TokenManagerFactory tokenManagerFactory;

  public ClientRegistrySetTokenGatewayFilter(
      String clientRegistry,
      TokenConfig config,
      VaultTemplate vaultTemplate,
      TokenManagerFactory tokenManagerFactory) {
    this.clientRegistry = clientRegistry;
    this.config = config;
    this.vaultTemplate = vaultTemplate;
    this.tokenManagerFactory = tokenManagerFactory;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    log.info("Replacing token in request");
    var authInfoOpt = getAuthInfoFromVault(exchange);
    if (authInfoOpt.isEmpty()) {
      throw new VaultDataRetrievingException("Auth configs were not found in vault");
    }
    var token = getTokenFromKeycloak(authInfoOpt.get());

    var request =
        exchange
            .getRequest()
            .mutate()
            .headers(httpHeaders -> httpHeaders.set(config.getHeader(), token))
            .build();
    return chain.filter(exchange.mutate().request(request).build());
  }

  private Optional<KeycloakAuthRequestInfo> getAuthInfoFromVault(ServerWebExchange exchange) {
    Map<String, String> pathVariables =
            exchange.getAttribute(ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    var targetRegistry = pathVariables.get(REGISTRY_NAME_URL_PATH_VARIABLE);
    var vaultPath = String.format(VAULT_PATTERN, targetRegistry, clientRegistry);
    try {
      var response =
              vaultTemplate
                      .opsForKeyValue(VAULT_PREFIX, KeyValueBackend.KV_2)
                      .get(vaultPath, KeycloakAuthRequestInfo.class);
      return Optional.ofNullable(response).map(VaultResponseSupport::getData);
    } catch (Exception e) {
      throw new VaultDataRetrievingException("Error while communicating with vault", e);
    }
  }

  private String getTokenFromKeycloak(KeycloakAuthRequestInfo authRequestInfo) {
    try {
      return tokenManagerFactory.createTokenManager(authRequestInfo).getAccessTokenString();
    } catch (Exception e) {
      throw new KeycloakCommunicationException(
          "Error while retrieving valid access token from keycloak", e);
    }
  }
}
