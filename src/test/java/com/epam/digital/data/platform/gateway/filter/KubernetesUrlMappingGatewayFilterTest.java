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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.epam.digital.data.platform.gateway.filter.factory.KubernetesUrlMappingGatewayFilterFactory.*;
import static com.epam.digital.data.platform.gateway.util.GatewayConstants.REGISTRY_NAME_URL_PATH_VARIABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KubernetesUrlMappingGatewayFilterTest {

  private static final String SERVICE_URL_PATTERN = "http://target.{registry}:8080";

  private KubernetesUrlMappingGatewayFilter kubernetesUrlMappingGatewayFilter;

  @Mock
  private ServerWebExchange serverWebExchange;
  @Mock
  private GatewayFilterChain gatewayFilterChain;

  @BeforeEach
  void beforeEach() {
    Config config = new Config();
    config.setServiceUrlPattern(SERVICE_URL_PATTERN);

    kubernetesUrlMappingGatewayFilter = new KubernetesUrlMappingGatewayFilter(config);
  }

  @Test
  void expectHostIsMappedWithRegistryName() {
    when(serverWebExchange.getAttribute(ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
            .thenReturn(Map.of(REGISTRY_NAME_URL_PATH_VARIABLE, "registry-name"));
    Route preconfiguredRoute = mockRoute();
    when(serverWebExchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR))
            .thenReturn(preconfiguredRoute);

    Map<String, Object> attributesMap = new HashMap<>();
    Map<String, Object> spyAttributesMap = spy(attributesMap);
    when(serverWebExchange.getAttributes()).thenReturn(spyAttributesMap);

    kubernetesUrlMappingGatewayFilter.filter(serverWebExchange, gatewayFilterChain);

    ArgumentCaptor<Route> updatedRouteCaptor = ArgumentCaptor.forClass(Route.class);
    verify(spyAttributesMap).put(eq(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR), updatedRouteCaptor.capture());

    Route updatedRoute = updatedRouteCaptor.getValue();
    assertThat(updatedRoute.getId()).isEqualTo(preconfiguredRoute.getId());
    assertThat(updatedRoute.getUri()).isEqualTo(URI.create("http://target.registry-name:8080"));
    assertThat(updatedRoute.getPredicate()).isEqualTo(preconfiguredRoute.getPredicate());
    assertThat(updatedRoute.getOrder()).isEqualTo(preconfiguredRoute.getOrder());
    assertThat(updatedRoute.getFilters()).isEqualTo(preconfiguredRoute.getFilters());
    assertThat(updatedRoute.getMetadata()).isEqualTo(preconfiguredRoute.getMetadata());
  }

  private Route mockRoute() {
    return Route.async()
            .id("id")
            .uri(URI.create("http://rest-api-pod-name:80"))
            .predicate(ex -> true)
            .build();
  }
}
