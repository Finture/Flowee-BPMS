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
package com.finture.bpm.engine;


import com.finture.bpm.engine.*;
import com.finture.bpm.engine.CaseService;
import com.finture.bpm.engine.ExternalTaskService;
import com.finture.bpm.engine.FilterService;
import com.finture.bpm.engine.RuntimeService;
import com.finture.bpm.engine.TaskService;

/**
 * <p>Base interface providing access to the process engine's
 * public API services.</p>
 *
 * @author Daniel Meyer
 *
 */
public interface ProcessEngineServices {

  /**
   * Returns the process engine's {@link com.finture.bpm.engine.RuntimeService}.
   *
   * @return the {@link com.finture.bpm.engine.RuntimeService} object.
   */
  RuntimeService getRuntimeService();

  /**
   * Returns the process engine's {@link RepositoryService}.
   *
   * @return the {@link RepositoryService} object.
   */
  RepositoryService getRepositoryService();

  /**
   * Returns the process engine's {@link FormService}.
   *
   * @return the {@link FormService} object.
   */
  FormService getFormService();

  /**
   * Returns the process engine's {@link com.finture.bpm.engine.TaskService}.
   *
   * @return the {@link com.finture.bpm.engine.TaskService} object.
   */
  TaskService getTaskService();

  /**
   * Returns the process engine's {@link HistoryService}.
   *
   * @return the {@link HistoryService} object.
   */
  HistoryService getHistoryService();

  /**
   * Returns the process engine's {@link IdentityService}.
   *
   * @return the {@link IdentityService} object.
   */
  IdentityService getIdentityService();

  /**
   * Returns the process engine's {@link ManagementService}.
   *
   * @return the {@link ManagementService} object.
   */
  ManagementService getManagementService();

  /**
   * Returns the process engine's {@link AuthorizationService}.
   *
   * @return the {@link AuthorizationService} object.
   */
  AuthorizationService getAuthorizationService();

  /**
   * Returns the engine's {@link com.finture.bpm.engine.CaseService}.
   *
   * @return the {@link com.finture.bpm.engine.CaseService} object.
   *
   */
  CaseService getCaseService();

  /**
   * Returns the engine's {@link com.finture.bpm.engine.FilterService}.
   *
   * @return the {@link com.finture.bpm.engine.FilterService} object.
   *
   */
  FilterService getFilterService();

  /**
   * Returns the engine's {@link com.finture.bpm.engine.ExternalTaskService}.
   *
   * @return the {@link com.finture.bpm.engine.ExternalTaskService} object.
   */
  ExternalTaskService getExternalTaskService();

  /**
   * Returns the engine's {@link DecisionService}.
   *
   * @return the {@link DecisionService} object.
   */
  DecisionService getDecisionService();

}
