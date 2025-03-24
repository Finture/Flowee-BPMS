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

import static com.finture.bpm.engine.impl.util.EnsureUtil.ensureOnlyOneNotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.finture.bpm.engine.history.HistoricVariableInstance;
import com.finture.bpm.engine.history.HistoricVariableInstanceQuery;
import com.finture.bpm.engine.impl.HistoricVariableInstanceQueryImpl;
import com.finture.bpm.engine.impl.Page;
import com.finture.bpm.engine.impl.db.ListQueryParameterObject;
import com.finture.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import com.finture.bpm.engine.impl.persistence.AbstractHistoricManager;
import com.finture.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import com.finture.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;


/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricVariableInstanceManager extends AbstractHistoricManager {

  public void deleteHistoricVariableInstanceByVariableInstanceId(String historicVariableInstanceId) {
    if (isHistoryEnabled()) {
      com.finture.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity historicVariableInstance = findHistoricVariableInstanceByVariableInstanceId(historicVariableInstanceId);
      if (historicVariableInstance != null) {
        historicVariableInstance.delete();
      }
    }
  }

  public void deleteHistoricVariableInstanceByProcessInstanceIds(List<String> historicProcessInstanceIds) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processInstanceIds", historicProcessInstanceIds);
    deleteHistoricVariableInstances(parameters);
  }

  public void deleteHistoricVariableInstancesByTaskProcessInstanceIds(List<String> historicProcessInstanceIds) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("taskProcessInstanceIds", historicProcessInstanceIds);
    deleteHistoricVariableInstances(parameters);
  }

  public void deleteHistoricVariableInstanceByCaseInstanceId(String historicCaseInstanceId) {
    deleteHistoricVariableInstancesByProcessCaseInstanceId(null, historicCaseInstanceId);
  }

  public void deleteHistoricVariableInstancesByCaseInstanceIds(List<String> historicCaseInstanceIds) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("caseInstanceIds", historicCaseInstanceIds);
    deleteHistoricVariableInstances(parameters);
  }

  protected void deleteHistoricVariableInstances(Map<String, Object> parameters) {
    getDbEntityManager().deletePreserveOrder(ByteArrayEntity.class, "deleteHistoricVariableInstanceByteArraysByIds", parameters);
    getDbEntityManager().deletePreserveOrder(com.finture.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity.class, "deleteHistoricVariableInstanceByIds", parameters);
  }

  protected void deleteHistoricVariableInstancesByProcessCaseInstanceId(String historicProcessInstanceId, String historicCaseInstanceId) {
    ensureOnlyOneNotNull("Only the process instance or case instance id should be set", historicProcessInstanceId, historicCaseInstanceId);
    if (isHistoryEnabled()) {

      // delete entries in DB
      List<HistoricVariableInstance> historicVariableInstances;
      if (historicProcessInstanceId != null) {
        historicVariableInstances = findHistoricVariableInstancesByProcessInstanceId(historicProcessInstanceId);
      }
      else {
        historicVariableInstances = findHistoricVariableInstancesByCaseInstanceId(historicCaseInstanceId);
      }

      for (HistoricVariableInstance historicVariableInstance : historicVariableInstances) {
        ((com.finture.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity) historicVariableInstance).delete();
      }

      // delete entries in Cache
      List <com.finture.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity> cachedHistoricVariableInstances = getDbEntityManager().getCachedEntitiesByType(com.finture.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity.class);
      for (com.finture.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity historicVariableInstance : cachedHistoricVariableInstances) {
        // make sure we only delete the right ones (as we cannot make a proper query in the cache)
        if ((historicProcessInstanceId != null && historicProcessInstanceId.equals(historicVariableInstance.getProcessInstanceId()))
            || (historicCaseInstanceId != null && historicCaseInstanceId.equals(historicVariableInstance.getCaseInstanceId()))) {
          historicVariableInstance.delete();
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public List<HistoricVariableInstance> findHistoricVariableInstancesByProcessInstanceId(String processInstanceId) {
    return getDbEntityManager().selectList("selectHistoricVariablesByProcessInstanceId", processInstanceId);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricVariableInstance> findHistoricVariableInstancesByCaseInstanceId(String caseInstanceId) {
    return getDbEntityManager().selectList("selectHistoricVariablesByCaseInstanceId", caseInstanceId);
  }

  public long findHistoricVariableInstanceCountByQueryCriteria(HistoricVariableInstanceQueryImpl historicProcessVariableQuery) {
    configureQuery(historicProcessVariableQuery);
    return (Long) getDbEntityManager().selectOne("selectHistoricVariableInstanceCountByQueryCriteria", historicProcessVariableQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricVariableInstance> findHistoricVariableInstancesByQueryCriteria(HistoricVariableInstanceQueryImpl historicProcessVariableQuery, Page page) {
    configureQuery(historicProcessVariableQuery);
    return getDbEntityManager().selectList("selectHistoricVariableInstanceByQueryCriteria", historicProcessVariableQuery, page);
  }

  public com.finture.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity findHistoricVariableInstanceByVariableInstanceId(String variableInstanceId) {
    return (com.finture.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity) getDbEntityManager().selectOne("selectHistoricVariableInstanceByVariableInstanceId", variableInstanceId);
  }

  public void deleteHistoricVariableInstancesByTaskId(String taskId) {
    if (isHistoryEnabled()) {
      HistoricVariableInstanceQuery historicProcessVariableQuery = new HistoricVariableInstanceQueryImpl().taskIdIn(taskId);
      List<HistoricVariableInstance> historicProcessVariables = historicProcessVariableQuery.list();
      for(HistoricVariableInstance historicProcessVariable : historicProcessVariables) {
        ((com.finture.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity) historicProcessVariable).delete();
      }
    }
  }

  public DbOperation addRemovalTimeToVariableInstancesByRootProcessInstanceId(String rootProcessInstanceId, Date removalTime, Integer batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("rootProcessInstanceId", rootProcessInstanceId);
    parameters.put("removalTime", removalTime);
    parameters.put("maxResults", batchSize);

    return getDbEntityManager()
      .updatePreserveOrder(com.finture.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity.class, "updateHistoricVariableInstancesByRootProcessInstanceId", parameters);
  }

  public DbOperation addRemovalTimeToVariableInstancesByProcessInstanceId(String processInstanceId, Date removalTime, Integer batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("removalTime", removalTime);
    parameters.put("maxResults", batchSize);

    return getDbEntityManager()
      .updatePreserveOrder(com.finture.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity.class, "updateHistoricVariableInstancesByProcessInstanceId", parameters);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricVariableInstance> findHistoricVariableInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int
          maxResults) {
    return getDbEntityManager().selectListWithRawParameter("selectHistoricVariableInstanceByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findHistoricVariableInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbEntityManager().selectOne("selectHistoricVariableInstanceCountByNativeQuery", parameterMap);
  }

  protected void configureQuery(HistoricVariableInstanceQueryImpl query) {
    getAuthorizationManager().configureHistoricVariableInstanceQuery(query);
    getTenantManager().configureQuery(query);
  }

  public DbOperation deleteHistoricVariableInstancesByRemovalTime(Date removalTime, int minuteFrom, int minuteTo, int batchSize) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("removalTime", removalTime);
    if (minuteTo - minuteFrom + 1 < 60) {
      parameters.put("minuteFrom", minuteFrom);
      parameters.put("minuteTo", minuteTo);
    }
    parameters.put("batchSize", batchSize);

    return getDbEntityManager()
      .deletePreserveOrder(HistoricVariableInstanceEntity.class, "deleteHistoricVariableInstancesByRemovalTime",
        new ListQueryParameterObject(parameters, 0, batchSize));
  }
}
