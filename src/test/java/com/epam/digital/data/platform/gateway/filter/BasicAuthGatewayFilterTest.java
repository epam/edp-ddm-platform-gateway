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

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.gateway.filter.factory.BasicAuthGatewayFilterFactory.BasicAuthConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

@ExtendWith(MockitoExtension.class)
class BasicAuthGatewayFilterTest {

  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";

  @Mock
  private GatewayFilterChain gatewayFilterChain;

  private BasicAuthGatewayFilter basicAuthGatewayFilter;

  @BeforeEach
  void beforeEach() {
    var basicAuthConfig = new BasicAuthConfig();
    basicAuthConfig.setUsername(USERNAME);
    basicAuthConfig.setPassword(PASSWORD);

    basicAuthGatewayFilter = new BasicAuthGatewayFilter(basicAuthConfig);
  }

  @Test
  void expectThirdPartyAccessTokenAddedToRequest() {
    var exchange = mockExchange();

    basicAuthGatewayFilter.filter(exchange, gatewayFilterChain);

    var expectedHeaderValue = "Basic " + HttpHeaders.encodeBasicAuth(USERNAME, PASSWORD, null);
    assertThat(exchange.getRequest().getHeaders())
        .containsEntry(HttpHeaders.AUTHORIZATION, List.of(expectedHeaderValue));
  }

  private ServerWebExchange mockExchange() {
    return MockServerWebExchange.from(
        MockServerHttpRequest.get("url")
            .header(HttpHeaders.AUTHORIZATION, "OldValue"));
  }
}
