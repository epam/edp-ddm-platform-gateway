/*
 * Copyright 2023 EPAM Systems.
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

package com.epam.digital.data.platform.gateway.config;

import com.epam.digital.data.platform.integration.idm.config.IdmClientServiceConfig;
import com.epam.digital.data.platform.integration.idm.factory.IdmServiceFactory;
import com.epam.digital.data.platform.integration.idm.model.KeycloakClientProperties;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(IdmClientServiceConfig.class)
public class PublicApiKeycloakConfig {

  @Bean
  @ConfigurationProperties(prefix = "keycloak.public-api")
  public KeycloakClientProperties keycloakPublicApiClientProperties() {
    return new KeycloakClientProperties();
  }

  @Bean
  public IdmService publicApiIdmService(
      IdmServiceFactory idmServiceFactory,
      KeycloakClientProperties keycloakPublicApiClientProperties) {
    return idmServiceFactory.createIdmService(
        keycloakPublicApiClientProperties.getRealm(),
        keycloakPublicApiClientProperties.getClientId(),
        keycloakPublicApiClientProperties.getClientSecret());
  }
}
