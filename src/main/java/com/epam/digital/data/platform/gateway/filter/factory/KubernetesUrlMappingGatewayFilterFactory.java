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

import com.epam.digital.data.platform.gateway.filter.KubernetesUrlMappingGatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class KubernetesUrlMappingGatewayFilterFactory
        extends AbstractGatewayFilterFactory<KubernetesUrlMappingGatewayFilterFactory.Config> {

  public KubernetesUrlMappingGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return new KubernetesUrlMappingGatewayFilter(config);
  }

  public static class Config {
    private String serviceUrlPattern;

    public String getServiceUrlPattern() {
      return serviceUrlPattern;
    }

    public void setServiceUrlPattern(String serviceUrlPattern) {
      this.serviceUrlPattern = serviceUrlPattern;
    }
  }
}
