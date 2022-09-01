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

import com.epam.digital.data.platform.gateway.filter.factory.KubernetesUrlMappingGatewayFilterFactory;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

import static com.epam.digital.data.platform.gateway.util.GatewayConstants.REGISTRY_NAME_URL_PATH_VARIABLE;

public class KubernetesUrlMappingGatewayFilter implements GatewayFilter {

  private final Logger log = LoggerFactory.getLogger(KubernetesUrlMappingGatewayFilter.class);

  private final KubernetesUrlMappingGatewayFilterFactory.Config config;

  public KubernetesUrlMappingGatewayFilter(KubernetesUrlMappingGatewayFilterFactory.Config config) {
    this.config = config;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    log.info("Adding target registry to request url");
    Map<String, String> pathVariables =
        exchange.getAttribute(ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    Route preconfiguredRoute = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
    Route routeWithRegistryHost =
        createRouteWithUrl(pathVariables.get(REGISTRY_NAME_URL_PATH_VARIABLE), preconfiguredRoute);
    exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, routeWithRegistryHost);
    return chain.filter(exchange);
  }

  private Route createRouteWithUrl(String registryName, Route preconfiguredRoute) {
    String registryServiceUrl =
        StrSubstitutor.replace(
            config.getServiceUrlPattern(),
            Map.of(REGISTRY_NAME_URL_PATH_VARIABLE, registryName),
            "{",
            "}");
    return Route.async()
        .id(preconfiguredRoute.getId())
        .order(preconfiguredRoute.getOrder())
        .filters(preconfiguredRoute.getFilters())
        .asyncPredicate(preconfiguredRoute.getPredicate())
        .metadata(preconfiguredRoute.getMetadata())
        .uri(URI.create(registryServiceUrl))
        .build();
  }
}
