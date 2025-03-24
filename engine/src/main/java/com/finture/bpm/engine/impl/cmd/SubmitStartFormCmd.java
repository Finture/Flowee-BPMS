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
package com.finture.bpm.engine.impl.cmd;

import static com.finture.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import com.finture.bpm.engine.history.UserOperationLogEntry;
import com.finture.bpm.engine.impl.cfg.CommandChecker;
import com.finture.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import com.finture.bpm.engine.impl.context.Context;
import com.finture.bpm.engine.impl.interceptor.Command;
import com.finture.bpm.engine.impl.interceptor.CommandContext;
import com.finture.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity;
import com.finture.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import com.finture.bpm.engine.impl.persistence.entity.PropertyChange;
import com.finture.bpm.engine.runtime.ProcessInstance;
import com.finture.bpm.engine.variable.VariableMap;
import com.finture.bpm.engine.variable.Variables;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class SubmitStartFormCmd implements Command<ProcessInstance>, Serializable {

  private static final long serialVersionUID = 1L;

  protected final String processDefinitionId;
  protected final String businessKey;
  protected VariableMap variables;

  public SubmitStartFormCmd(String processDefinitionId, String businessKey, Map<String, Object> properties) {
    this.processDefinitionId = processDefinitionId;
    this.businessKey = businessKey;
    this.variables = Variables.fromMap(properties);
  }

  @Override
  public ProcessInstance execute(CommandContext commandContext) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();
    ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
    ensureNotNull("No process definition found for id = '" + processDefinitionId + "'", "processDefinition", processDefinition);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkCreateProcessInstance(processDefinition);
    }

    ExecutionEntity processInstance = null;
    if (businessKey != null) {
      processInstance = processDefinition.createProcessInstance(businessKey);
    } else {
      processInstance = processDefinition.createProcessInstance();
    }

    processInstance.startWithFormProperties(variables);

    commandContext.getOperationLogManager().logProcessInstanceOperation(
        UserOperationLogEntry.OPERATION_TYPE_CREATE,
        processInstance.getId(),
        processInstance.getProcessDefinitionId(),
        processInstance.getProcessDefinition().getKey(),
        Collections.singletonList(PropertyChange.EMPTY_CHANGE));

    return processInstance;
  }
}
