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

import com.epam.digital.data.platform.gateway.filter.BasicAuthGatewayFilter;
import com.epam.digital.data.platform.gateway.filter.factory.BasicAuthGatewayFilterFactory.BasicAuthConfig;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class BasicAuthGatewayFilterFactory
    extends AbstractGatewayFilterFactory<BasicAuthConfig> {

  public BasicAuthGatewayFilterFactory() {
    super(BasicAuthConfig.class);
  }

  @Override
  public GatewayFilter apply(BasicAuthConfig config) {
    return new BasicAuthGatewayFilter(config);
  }

  public static class BasicAuthConfig {

    private String username;
    private String password;

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }
}
