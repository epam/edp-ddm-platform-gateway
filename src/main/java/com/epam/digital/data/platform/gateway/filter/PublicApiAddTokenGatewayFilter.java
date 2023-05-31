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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class PublicApiAddTokenGatewayFilter implements GatewayFilter {

  private final Logger log = LoggerFactory.getLogger(PublicApiAddTokenGatewayFilter.class);

  private final IdmService publicApiIdmService;
  private final TokenConfig config;

  public PublicApiAddTokenGatewayFilter(IdmService publicApiIdmService, TokenConfig config) {
    this.publicApiIdmService = publicApiIdmService;
    this.config = config;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    log.info("Add access token to public api call");
    var token = publicApiIdmService.getClientAccessToken();
    var request =
        exchange
            .getRequest()
            .mutate()
            .headers(httpHeaders -> httpHeaders.set(config.getHeader(), token))
            .build();
    return chain.filter(exchange.mutate().request(request).build());
  }
}
