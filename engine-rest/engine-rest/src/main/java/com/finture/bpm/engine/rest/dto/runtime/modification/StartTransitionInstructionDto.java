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
package com.finture.bpm.engine.rest.dto.runtime.modification;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finture.bpm.engine.rest.exception.InvalidRequestException;
import com.finture.bpm.engine.ProcessEngine;
import com.finture.bpm.engine.runtime.InstantiationBuilder;
import com.finture.bpm.engine.runtime.ProcessInstanceModificationBuilder;
import com.finture.bpm.engine.runtime.ProcessInstanceModificationInstantiationBuilder;
import com.finture.bpm.engine.runtime.ProcessInstantiationBuilder;

import javax.ws.rs.core.Response.Status;

/**
 * @author Thorben Lindhauer
 *
 */
@JsonTypeName(ProcessInstanceModificationInstructionDto.START_TRANSITION_INSTRUCTION_TYPE)
public class StartTransitionInstructionDto extends ProcessInstanceModificationInstructionDto {

  @Override
  public void applyTo(ProcessInstanceModificationBuilder builder, ProcessEngine engine, ObjectMapper mapper) {
    checkValidity();

    ProcessInstanceModificationInstantiationBuilder activityBuilder = null;

    if (ancestorActivityInstanceId != null) {
      activityBuilder = builder.startTransition(transitionId, ancestorActivityInstanceId);
    }
    else {
      activityBuilder = builder.startTransition(transitionId);
    }

    applyVariables(activityBuilder, engine, mapper);
  }

  @Override
  public void applyTo(InstantiationBuilder<?> builder, ProcessEngine engine, ObjectMapper mapper) {
    checkValidity();

    builder.startTransition(transitionId);
    if (builder instanceof ProcessInstantiationBuilder) {
      applyVariables((ProcessInstantiationBuilder) builder, engine, mapper);
    }
  }

  protected void checkValidity() {
    if (transitionId == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST,
          buildErrorMessage("'transitionId' must be set"));
    }
  }
}
