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
