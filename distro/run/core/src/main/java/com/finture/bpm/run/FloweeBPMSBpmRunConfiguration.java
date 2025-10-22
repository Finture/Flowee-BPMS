/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package com.finture.bpm.run;

import com.finture.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import com.finture.bpm.engine.impl.cfg.ProcessEnginePlugin;
import com.finture.bpm.engine.impl.plugin.AdministratorAuthorizationPlugin;
import com.finture.bpm.identity.impl.ldap.plugin.LdapIdentityProviderPlugin;
import com.finture.bpm.run.property.FloweeBPMSBpmRunAdministratorAuthorizationProperties;
import com.finture.bpm.run.property.FloweeBPMSBpmRunLdapProperties;
import com.finture.bpm.run.property.FloweeBPMSBpmRunProperties;
import com.finture.bpm.spring.boot.starter.FloweeBPMSBpmAutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@EnableConfigurationProperties(FloweeBPMSBpmRunProperties.class)
@Configuration
@AutoConfigureAfter({ FloweeBPMSBpmAutoConfiguration.class })
public class FloweeBPMSBpmRunConfiguration {

  @Bean
  @ConditionalOnProperty(name = "enabled", havingValue = "true", prefix = FloweeBPMSBpmRunLdapProperties.PREFIX)
  public LdapIdentityProviderPlugin ldapIdentityProviderPlugin(FloweeBPMSBpmRunProperties properties) {
    return properties.getLdap();
  }

  @Bean
  @ConditionalOnProperty(name = "enabled", havingValue = "true", prefix = FloweeBPMSBpmRunAdministratorAuthorizationProperties.PREFIX)
  public AdministratorAuthorizationPlugin administratorAuthorizationPlugin(FloweeBPMSBpmRunProperties properties) {
    return properties.getAdminAuth();
  }

  @Bean
  public ProcessEngineConfigurationImpl processEngineConfigurationImpl(List<ProcessEnginePlugin> processEnginePluginsFromContext,
                                                                       FloweeBPMSBpmRunProperties properties,
                                                                       FloweeBPMSBpmRunDeploymentConfiguration deploymentConfig) {
    String normalizedDeploymentDir = deploymentConfig.getNormalizedDeploymentDir();
    boolean deployChangedOnly = properties.getDeployment().isDeployChangedOnly();
    var processEnginePluginsFromYaml = properties.getProcessEnginePlugins();

    return new FloweeBPMSBpmRunProcessEngineConfiguration(normalizedDeploymentDir, deployChangedOnly,
        processEnginePluginsFromContext, processEnginePluginsFromYaml);
  }

  @Bean
  public FloweeBPMSBpmRunDeploymentConfiguration camundaDeploymentConfiguration(@Value("${flowee-bpms.deploymentDir:#{\"\"}}") String deploymentDir) {
    return new FloweeBPMSBpmRunDeploymentConfiguration(deploymentDir);
  }

}
