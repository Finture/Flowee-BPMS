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
package com.finture.bpm.engine.impl.persistence.entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.finture.bpm.engine.authorization.Resources;
import com.finture.bpm.engine.impl.AbstractQuery;
import com.finture.bpm.engine.impl.ExecutionQueryImpl;
import com.finture.bpm.engine.impl.Page;
import com.finture.bpm.engine.impl.ProcessEngineLogger;
import com.finture.bpm.engine.impl.ProcessInstanceQueryImpl;
import com.finture.bpm.engine.impl.cfg.auth.ResourceAuthorizationProvider;
import com.finture.bpm.engine.impl.db.EnginePersistenceLogger;
import com.finture.bpm.engine.impl.db.ListQueryParameterObject;
import com.finture.bpm.engine.impl.persistence.AbstractManager;
import com.finture.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity;
import com.finture.bpm.engine.impl.persistence.entity.SuspensionState;
import com.finture.bpm.engine.impl.util.ImmutablePair;
import com.finture.bpm.engine.runtime.Execution;
import com.finture.bpm.engine.runtime.ProcessInstance;


/**
 * @author Tom Baeyens
 */
public class ExecutionManager extends AbstractManager {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  public void insertExecution(com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity execution) {
    getDbEntityManager().insert(execution);
    createDefaultAuthorizations(execution);
  }

  public void deleteExecution(com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity execution) {
    getDbEntityManager().delete(execution);
    if (execution.isProcessInstanceExecution()) {
      deleteAuthorizations(Resources.PROCESS_INSTANCE, execution.getProcessInstanceId());
    }
  }

  @SuppressWarnings("unchecked")
  public void deleteProcessInstancesByProcessDefinition(String processDefinitionId, String deleteReason, boolean cascade, boolean skipCustomListeners, boolean skipIoMappings) {
    List<String> processInstanceIds = getDbEntityManager()
      .selectList("selectProcessInstanceIdsByProcessDefinitionId", processDefinitionId);

    for (String processInstanceId: processInstanceIds) {
      deleteProcessInstance(processInstanceId, deleteReason, cascade, skipCustomListeners, false, skipIoMappings, false);
    }

    if (cascade) {
      getHistoricProcessInstanceManager().deleteHistoricProcessInstanceByProcessDefinitionId(processDefinitionId);
    }
  }

  public void deleteProcessInstance(String processInstanceId, String deleteReason) {
    deleteProcessInstance(processInstanceId, deleteReason, false, false);
  }

  public void deleteProcessInstance(String processInstanceId, String deleteReason, boolean cascade, boolean skipCustomListeners) {
    deleteProcessInstance(processInstanceId, deleteReason, cascade, skipCustomListeners, false, false, false);
  }

  public void deleteProcessInstance(String processInstanceId, String deleteReason, boolean cascade, boolean skipCustomListeners, boolean externallyTerminated,
      boolean skipIoMappings, boolean skipSubprocesses) {
    com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity execution = findExecutionById(processInstanceId);

    if(execution == null) {
      throw LOG.requestedProcessInstanceNotFoundException(processInstanceId);
    }

    getTaskManager().deleteTasksByProcessInstanceId(processInstanceId, deleteReason, cascade, skipCustomListeners);

    // delete the execution BEFORE we delete the history, otherwise we will produce orphan HistoricVariableInstance instances
    execution.deleteCascade(deleteReason, skipCustomListeners, skipIoMappings, externallyTerminated, skipSubprocesses);

    if (cascade) {
      getHistoricProcessInstanceManager().deleteHistoricProcessInstanceByIds(Arrays.asList(processInstanceId));
    }
  }

  public com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity findSubProcessInstanceBySuperExecutionId(String superExecutionId) {
    return (com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity) getDbEntityManager().selectOne("selectSubProcessInstanceBySuperExecutionId", superExecutionId);
  }

  public com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity findSubProcessInstanceBySuperCaseExecutionId(String superCaseExecutionId) {
    return (com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity) getDbEntityManager().selectOne("selectSubProcessInstanceBySuperCaseExecutionId", superCaseExecutionId);
  }

  @SuppressWarnings("unchecked")
  public List<com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity> findChildExecutionsByParentExecutionId(String parentExecutionId) {
    return getDbEntityManager().selectList("selectExecutionsByParentExecutionId", parentExecutionId);
  }

  @SuppressWarnings("unchecked")
  public List<com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity> findExecutionsByProcessInstanceId(String processInstanceId) {
    return getDbEntityManager().selectList("selectExecutionsByProcessInstanceId", processInstanceId);
  }

  public com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity findExecutionById(String executionId) {
    return getDbEntityManager().selectById(com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity.class, executionId);
  }

  public long findExecutionCountByQueryCriteria(ExecutionQueryImpl executionQuery) {
    configureQuery(executionQuery);
    return (Long) getDbEntityManager().selectOne("selectExecutionCountByQueryCriteria", executionQuery);
  }

  @SuppressWarnings("unchecked")
  public List<com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity> findExecutionsByQueryCriteria(ExecutionQueryImpl executionQuery, Page page) {
    configureQuery(executionQuery);
    return getDbEntityManager().selectList("selectExecutionsByQueryCriteria", executionQuery, page);
  }

  public long findProcessInstanceCountByQueryCriteria(ProcessInstanceQueryImpl processInstanceQuery) {
    configureQuery(processInstanceQuery);
    return (Long) getDbEntityManager().selectOne("selectProcessInstanceCountByQueryCriteria", processInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<ProcessInstance> findProcessInstancesByQueryCriteria(ProcessInstanceQueryImpl processInstanceQuery, Page page) {
    configureQuery(processInstanceQuery);
    return getDbEntityManager().selectList("selectProcessInstanceByQueryCriteria", processInstanceQuery, page);
  }

  @SuppressWarnings("unchecked")
  public List<String> findProcessInstancesIdsByQueryCriteria(ProcessInstanceQueryImpl processInstanceQuery) {
    configureQuery(processInstanceQuery);
    return getDbEntityManager().selectList("selectProcessInstanceIdsByQueryCriteria", processInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<ImmutablePair<String, String>> findDeploymentIdMappingsByQueryCriteria(ProcessInstanceQueryImpl processInstanceQuery) {
    configureQuery(processInstanceQuery);
    return getDbEntityManager().selectList("selectProcessInstanceDeploymentIdMappingsByQueryCriteria", processInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity> findEventScopeExecutionsByActivityId(String activityRef, String parentExecutionId) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("activityId", activityRef);
    parameters.put("parentExecutionId", parentExecutionId);
    return getDbEntityManager().selectList("selectExecutionsByParentExecutionId", parameters);
  }

  @SuppressWarnings("unchecked")
  public List<Execution> findExecutionsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbEntityManager().selectListWithRawParameter("selectExecutionByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @SuppressWarnings("unchecked")
  public List<ProcessInstance> findProcessInstanceByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbEntityManager().selectListWithRawParameter("selectExecutionByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findExecutionCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbEntityManager().selectOne("selectExecutionCountByNativeQuery", parameterMap);
  }

  public void updateExecutionSuspensionStateByProcessDefinitionId(String processDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("processDefinitionId", processDefinitionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity.class, "updateExecutionSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateExecutionSuspensionStateByProcessInstanceId(String processInstanceId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity.class, "updateExecutionSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateExecutionSuspensionStateByProcessDefinitionKey(String processDefinitionKey, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("isTenantIdSet", false);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity.class, "updateExecutionSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateExecutionSuspensionStateByProcessDefinitionKeyAndTenantId(String processDefinitionKey, String tenantId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("isTenantIdSet", true);
    parameters.put("tenantId", tenantId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity.class, "updateExecutionSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  // helper ///////////////////////////////////////////////////////////

  protected void createDefaultAuthorizations(ExecutionEntity execution) {
    if(execution.isProcessInstanceExecution() && isAuthorizationEnabled()) {
      ResourceAuthorizationProvider provider = getResourceAuthorizationProvider();
      AuthorizationEntity[] authorizations = provider.newProcessInstance(execution);
      saveDefaultAuthorizations(authorizations);
    }
  }

  protected void configureQuery(AbstractQuery<?, ?> query) {
    getAuthorizationManager().configureExecutionQuery(query);
    getTenantManager().configureQuery(query);
  }

  protected ListQueryParameterObject configureParameterizedQuery(Object parameter) {
    return getTenantManager().configureQuery(parameter);
  }

}
