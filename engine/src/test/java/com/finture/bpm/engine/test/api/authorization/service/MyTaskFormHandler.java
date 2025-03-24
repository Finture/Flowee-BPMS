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
package com.finture.bpm.engine.test.api.authorization.service;

import com.finture.bpm.engine.IdentityService;
import com.finture.bpm.engine.RuntimeService;
import com.finture.bpm.engine.delegate.VariableScope;
import com.finture.bpm.engine.form.TaskFormData;
import com.finture.bpm.engine.impl.bpmn.parser.BpmnParse;
import com.finture.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import com.finture.bpm.engine.impl.context.Context;
import com.finture.bpm.engine.impl.form.TaskFormDataImpl;
import com.finture.bpm.engine.impl.form.handler.TaskFormHandler;
import com.finture.bpm.engine.impl.persistence.entity.DeploymentEntity;
import com.finture.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import com.finture.bpm.engine.impl.persistence.entity.TaskEntity;
import com.finture.bpm.engine.impl.util.xml.Element;
import com.finture.bpm.engine.variable.VariableMap;

/**
 * @author Roman Smirnov
 *
 */
public class MyTaskFormHandler extends MyDelegationService implements TaskFormHandler {

  public void parseConfiguration(Element activityElement, DeploymentEntity deployment, ProcessDefinitionEntity processDefinition, BpmnParse bpmnParse) {
    // do nothing
  }

  public void submitFormVariables(VariableMap properties, VariableScope variableScope) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    IdentityService identityService = processEngineConfiguration.getIdentityService();
    RuntimeService runtimeService = processEngineConfiguration.getRuntimeService();

    logAuthentication(identityService);
    logInstancesCount(runtimeService);
  }

  public TaskFormData createTaskForm(TaskEntity task) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    IdentityService identityService = processEngineConfiguration.getIdentityService();
    RuntimeService runtimeService = processEngineConfiguration.getRuntimeService();

    logAuthentication(identityService);
    logInstancesCount(runtimeService);

    TaskFormDataImpl result = new TaskFormDataImpl();
    result.setTask(task);
    return result;
  }

}
