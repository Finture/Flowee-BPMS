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

import java.io.InputStream;
import java.io.Serializable;
import com.finture.bpm.engine.ProcessEngineException;
import com.finture.bpm.engine.impl.bpmn.diagram.ProcessDiagramLayoutFactory;
import com.finture.bpm.engine.impl.cfg.CommandChecker;
import com.finture.bpm.engine.impl.cmd.GetDeploymentProcessDiagramCmd;
import com.finture.bpm.engine.impl.cmd.GetDeploymentProcessModelCmd;
import com.finture.bpm.engine.impl.context.Context;
import com.finture.bpm.engine.impl.interceptor.Command;
import com.finture.bpm.engine.impl.interceptor.CommandContext;
import com.finture.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import com.finture.bpm.engine.repository.DiagramLayout;


/**
 * Provides positions and dimensions of elements in a process diagram as
 * provided by {@link com.finture.bpm.engine.impl.cmd.GetDeploymentProcessDiagramCmd}.
 *
 * This command requires a process model and a diagram image to be deployed.
 * @author Falko Menge
 */
public class GetDeploymentProcessDiagramLayoutCmd implements Command<DiagramLayout>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;

  public GetDeploymentProcessDiagramLayoutCmd(String processDefinitionId) {
    if (processDefinitionId == null || processDefinitionId.length() < 1) {
      throw new ProcessEngineException("The process definition id is mandatory, but '" + processDefinitionId + "' has been provided.");
    }
    this.processDefinitionId = processDefinitionId;
  }

  public DiagramLayout execute(final CommandContext commandContext) {
    ProcessDefinitionEntity processDefinition = Context
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .findDeployedProcessDefinitionById(processDefinitionId);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadProcessDefinition(processDefinition);
    }

    InputStream processModelStream = commandContext.runWithoutAuthorization(
        new GetDeploymentProcessModelCmd(processDefinitionId));

    InputStream processDiagramStream = commandContext.runWithoutAuthorization(
        new GetDeploymentProcessDiagramCmd(processDefinitionId));

    return new ProcessDiagramLayoutFactory().getProcessDiagramLayout(processModelStream, processDiagramStream);
  }

}
