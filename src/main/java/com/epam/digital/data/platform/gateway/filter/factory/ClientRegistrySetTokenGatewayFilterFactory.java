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

package com.epam.digital.data.platform.gateway.filter.factory;

import com.epam.digital.data.platform.gateway.filter.ClientRegistrySetTokenGatewayFilter;
import com.epam.digital.data.platform.gateway.filter.factory.model.TokenConfig;
import com.epam.digital.data.platform.gateway.service.TokenManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;

@Component
public class ClientRegistrySetTokenGatewayFilterFactory
        extends AbstractGatewayFilterFactory<TokenConfig> {

  private final String clientRegistry;
  private final VaultTemplate vaultTemplate;
  private final TokenManagerFactory tokenManagerFactory;

  public ClientRegistrySetTokenGatewayFilterFactory(
      @Value("${registry.name}") String clientRegistry,
      VaultTemplate vaultTemplate,
      TokenManagerFactory tokenManagerFactory) {
    super(TokenConfig.class);
    this.clientRegistry = clientRegistry;
    this.vaultTemplate = vaultTemplate;
    this.tokenManagerFactory = tokenManagerFactory;
  }

  @Override
  public GatewayFilter apply(TokenConfig config) {
    return new ClientRegistrySetTokenGatewayFilter(
        clientRegistry, config, vaultTemplate, tokenManagerFactory);
  }
}
