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

import java.util.Collections;
import java.util.Set;

import com.finture.bpm.application.ProcessApplicationReference;
import com.finture.bpm.application.ProcessApplicationRegistration;
import com.finture.bpm.engine.impl.application.ProcessApplicationManager;
import com.finture.bpm.engine.impl.cfg.CommandChecker;
import com.finture.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import com.finture.bpm.engine.impl.context.Context;
import com.finture.bpm.engine.impl.interceptor.Command;
import com.finture.bpm.engine.impl.interceptor.CommandContext;

/**
 *
 *
 * @author Daniel Meyer
 *
 */
public class RegisterProcessApplicationCmd implements Command<ProcessApplicationRegistration> {

  protected ProcessApplicationReference reference;
  protected Set<String> deploymentsToRegister;

  public RegisterProcessApplicationCmd(String deploymentId, ProcessApplicationReference reference) {
    this(Collections.singleton(deploymentId), reference);
  }

  public RegisterProcessApplicationCmd(Set<String> deploymentsToRegister, ProcessApplicationReference appReference) {
    this.deploymentsToRegister = deploymentsToRegister;
    reference = appReference;

  }

  public ProcessApplicationRegistration execute(CommandContext commandContext) {
    commandContext.getAuthorizationManager().checkCamundaAdminOrPermission(CommandChecker::checkRegisterProcessApplication);

    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    final ProcessApplicationManager processApplicationManager = processEngineConfiguration.getProcessApplicationManager();

    return processApplicationManager.registerProcessApplicationForDeployments(deploymentsToRegister, reference);
  }

}
