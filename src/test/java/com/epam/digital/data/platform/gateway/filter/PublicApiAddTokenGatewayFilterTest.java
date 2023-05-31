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

package com.epam.digital.data.platform.gateway.filter;

import com.epam.digital.data.platform.gateway.filter.factory.model.TokenConfig;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicApiAddTokenGatewayFilterTest {

  private static final String TOKEN_HEADER = "X-Access-Token";
  private static final String TOKEN_VALUE = "token";

  @Mock
  private GatewayFilterChain gatewayFilterChain;

  @Mock
  private IdmService publicApiIdmService;

  private PublicApiAddTokenGatewayFilter publicApiAddTokenGatewayFilter;

  @BeforeEach
  void beforeEach() {
    var tokenConfig = new TokenConfig();
    tokenConfig.setHeader(TOKEN_HEADER);

    publicApiAddTokenGatewayFilter = new PublicApiAddTokenGatewayFilter(publicApiIdmService, tokenConfig);

    when(publicApiIdmService.getClientAccessToken()).thenReturn(TOKEN_VALUE);
  }

  @Test
  void expectAccessTokenAddedToRequest() {
    when(publicApiIdmService.getClientAccessToken()).thenReturn(TOKEN_VALUE);

    var exchange = mockExchange();

    publicApiAddTokenGatewayFilter.filter(exchange, gatewayFilterChain);

    assertThat(exchange.getRequest().getHeaders())
            .containsEntry(TOKEN_HEADER, Collections.singletonList(TOKEN_VALUE));
  }

  private ServerWebExchange mockExchange() {
    return MockServerWebExchange.builder(MockServerHttpRequest.get("url").build()).build();
  }
}
