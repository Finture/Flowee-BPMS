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
package com.finture.bpm.spring.boot.starter;

import static com.finture.bpm.spring.boot.starter.jdbc.HistoryLevelDeterminatorJdbcTemplateImpl.createHistoryLevelDeterminator;

import java.util.List;

import com.finture.bpm.engine.impl.cfg.CompositeProcessEnginePlugin;
import com.finture.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import com.finture.bpm.engine.impl.cfg.ProcessEnginePlugin;
import com.finture.bpm.engine.spring.SpringProcessEngineConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.FloweeBPMSAuthorizationConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.FloweeBPMSDatasourceConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.FloweeBPMSDeploymentConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.FloweeBPMSFailedJobConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.FloweeBPMSHistoryConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.FloweeBPMSHistoryLevelAutoHandlingConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.FloweeBPMSJobConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.FloweeBPMSMetricsConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.FloweeBPMSProcessEngineConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.condition.NeedsHistoryAutoConfigurationCondition;
import com.finture.bpm.spring.boot.starter.configuration.id.IdGeneratorConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.impl.custom.CreateAdminUserConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.impl.custom.CreateFilterConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.impl.DefaultAuthorizationConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.impl.DefaultDatasourceConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.impl.DefaultDeploymentConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.impl.DefaultFailedJobConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.impl.DefaultHistoryConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.impl.DefaultHistoryLevelAutoHandlingConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.impl.DefaultJobConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.impl.DefaultJobConfiguration.JobConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.impl.DefaultMetricsConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.impl.DefaultProcessEngineConfiguration;
import com.finture.bpm.spring.boot.starter.configuration.impl.GenericPropertiesConfiguration;
import com.finture.bpm.spring.boot.starter.event.EventPublisherPlugin;
import com.finture.bpm.spring.boot.starter.jdbc.HistoryLevelDeterminator;
import com.finture.bpm.spring.boot.starter.property.FloweeBPMSBpmProperties;
import com.finture.bpm.spring.boot.starter.telemetry.CamundaIntegrationDeterminator;
import com.finture.bpm.spring.boot.starter.util.FloweeBPMSSpringBootUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import({
    JobConfiguration.class,
    IdGeneratorConfiguration.class
})
public class FloweeBPMSBpmConfiguration {

  @Bean
  @ConditionalOnMissingBean(ProcessEngineConfigurationImpl.class)
  public ProcessEngineConfigurationImpl processEngineConfigurationImpl(List<ProcessEnginePlugin> processEnginePlugins) {
    final SpringProcessEngineConfiguration configuration = FloweeBPMSSpringBootUtil.springProcessEngineConfiguration();
    configuration.getProcessEnginePlugins().add(new CompositeProcessEnginePlugin(processEnginePlugins));
    return configuration;
  }

  @Bean
  @ConditionalOnMissingBean(DefaultProcessEngineConfiguration.class)
  public static FloweeBPMSProcessEngineConfiguration camundaProcessEngineConfiguration() {
    return new DefaultProcessEngineConfiguration();
  }

  @Bean
  @ConditionalOnMissingBean(FloweeBPMSDatasourceConfiguration.class)
  public static FloweeBPMSDatasourceConfiguration camundaDatasourceConfiguration() {
    return new DefaultDatasourceConfiguration();
  }

  @Bean
  @ConditionalOnMissingBean(FloweeBPMSJobConfiguration.class)
  @ConditionalOnProperty(prefix = "flowee-bpms.bpm.job-execution", name = "enabled", havingValue = "true", matchIfMissing = true)
  public static FloweeBPMSJobConfiguration camundaJobConfiguration() {
    return new DefaultJobConfiguration();
  }

  @Bean
  @ConditionalOnMissingBean(FloweeBPMSHistoryConfiguration.class)
  public static FloweeBPMSHistoryConfiguration camundaHistoryConfiguration() {
    return new DefaultHistoryConfiguration();
  }

  @Bean
  @ConditionalOnMissingBean(FloweeBPMSMetricsConfiguration.class)
  public static FloweeBPMSMetricsConfiguration camundaMetricsConfiguration() {
    return new DefaultMetricsConfiguration();
  }

  //TODO to be removed within CAM-8108
  @Bean(name = "historyLevelAutoConfiguration")
  @ConditionalOnMissingBean(FloweeBPMSHistoryLevelAutoHandlingConfiguration.class)
  @ConditionalOnProperty(prefix = "flowee-bpms.bpm", name = "history-level", havingValue = "auto", matchIfMissing = false)
  @Conditional(NeedsHistoryAutoConfigurationCondition.class)
  public static FloweeBPMSHistoryLevelAutoHandlingConfiguration historyLevelAutoHandlingConfiguration() {
    return new DefaultHistoryLevelAutoHandlingConfiguration();
  }

  //TODO to be removed within CAM-8108
  @Bean(name = "historyLevelDeterminator")
  @ConditionalOnMissingBean(name = { "camundaBpmJdbcTemplate", "historyLevelDeterminator" })
  @ConditionalOnBean(name = "historyLevelAutoConfiguration")
  public static HistoryLevelDeterminator historyLevelDeterminator(FloweeBPMSBpmProperties floweeBPMSBpmProperties, JdbcTemplate jdbcTemplate) {
    return createHistoryLevelDeterminator(floweeBPMSBpmProperties, jdbcTemplate);
  }

  //TODO to be removed within CAM-8108
  @Bean(name = "historyLevelDeterminator")
  @ConditionalOnBean(name = { "camundaBpmJdbcTemplate", "historyLevelAutoConfiguration", "historyLevelDeterminator" })
  @ConditionalOnMissingBean(name = "historyLevelDeterminator")
  public static HistoryLevelDeterminator historyLevelDeterminatorMultiDatabase(FloweeBPMSBpmProperties floweeBPMSBpmProperties,
                                                                               @Qualifier("camundaBpmJdbcTemplate") JdbcTemplate jdbcTemplate) {
    return createHistoryLevelDeterminator(floweeBPMSBpmProperties, jdbcTemplate);
  }

  @Bean
  @ConditionalOnMissingBean(FloweeBPMSAuthorizationConfiguration.class)
  public static FloweeBPMSAuthorizationConfiguration camundaAuthorizationConfiguration() {
    return new DefaultAuthorizationConfiguration();
  }

  @Bean
  @ConditionalOnMissingBean(FloweeBPMSDeploymentConfiguration.class)
  public static FloweeBPMSDeploymentConfiguration camundaDeploymentConfiguration() {
    return new DefaultDeploymentConfiguration();
  }

  @Bean
  public GenericPropertiesConfiguration genericPropertiesConfiguration() {
    return new GenericPropertiesConfiguration();
  }

  @Bean
  @ConditionalOnProperty(prefix = "flowee-bpms.bpm.admin-user", name = "id")
  public CreateAdminUserConfiguration createAdminUserConfiguration() {
    return new CreateAdminUserConfiguration();
  }

  @Bean
  @ConditionalOnMissingBean(FloweeBPMSFailedJobConfiguration.class)
  public static FloweeBPMSFailedJobConfiguration failedJobConfiguration() {
    return new DefaultFailedJobConfiguration();
  }

  @Bean
  @ConditionalOnProperty(prefix = "flowee-bpms.bpm.filter", name = "create")
  public CreateFilterConfiguration createFilterConfiguration() {
    return new CreateFilterConfiguration();
  }

  @Bean
  public EventPublisherPlugin eventPublisherPlugin(FloweeBPMSBpmProperties properties, ApplicationEventPublisher publisher) {
    return new EventPublisherPlugin(properties.getEventing(), publisher);
  }

  @Bean
  public CamundaIntegrationDeterminator camundaIntegrationDeterminator() {
    return new CamundaIntegrationDeterminator();
  }
}
