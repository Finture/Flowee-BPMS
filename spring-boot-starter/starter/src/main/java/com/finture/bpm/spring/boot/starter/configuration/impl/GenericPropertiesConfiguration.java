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
package com.finture.bpm.spring.boot.starter.configuration.impl;

import com.finture.bpm.spring.boot.starter.configuration.Ordering;
import com.finture.bpm.spring.boot.starter.property.GenericProperties;
import com.finture.bpm.spring.boot.starter.util.SpringBootProcessEngineLogger;
import com.finture.bpm.spring.boot.starter.util.SpringBootStarterPropertyHelper;
import com.finture.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Order(Ordering.DEFAULT_ORDER - 1)
public class GenericPropertiesConfiguration extends AbstractCamundaConfiguration {

  protected static final SpringBootProcessEngineLogger LOG = SpringBootProcessEngineLogger.LOG;

  @Override
  public void preInit(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
    GenericProperties genericProperties = camundaBpmProperties.getGenericProperties();
    final Map<String, Object> properties = genericProperties.getProperties();

    if (!CollectionUtils.isEmpty(properties)) {
      SpringBootStarterPropertyHelper
          .applyProperties(springProcessEngineConfiguration, properties, genericProperties.isIgnoreUnknownFields());
      LOG.propertiesApplied(genericProperties);
    }
  }

}
