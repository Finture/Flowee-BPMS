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
package com.finture.bpm.engine.impl.dmn.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.finture.bpm.engine.ProcessEngineException;
import com.finture.bpm.engine.impl.Page;
import com.finture.bpm.engine.impl.ProcessEngineLogger;
import com.finture.bpm.engine.impl.cfg.auth.ResourceAuthorizationProvider;
import com.finture.bpm.engine.impl.db.EnginePersistenceLogger;
import com.finture.bpm.engine.impl.db.ListQueryParameterObject;
import com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionQueryImpl;
import com.finture.bpm.engine.impl.persistence.AbstractManager;
import com.finture.bpm.engine.impl.persistence.AbstractResourceDefinitionManager;
import com.finture.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import com.finture.bpm.engine.repository.DecisionDefinition;

public class DecisionDefinitionManager extends AbstractManager implements AbstractResourceDefinitionManager<com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity> {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  public void insertDecisionDefinition(com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity decisionDefinition) {
    getDbEntityManager().insert(decisionDefinition);
    createDefaultAuthorizations(decisionDefinition);
  }

  public void deleteDecisionDefinitionsByDeploymentId(String deploymentId) {
    getDbEntityManager().delete(com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity.class, "deleteDecisionDefinitionsByDeploymentId", deploymentId);
  }

  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity findDecisionDefinitionById(String decisionDefinitionId) {
    return getDbEntityManager().selectById(com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity.class, decisionDefinitionId);
  }

  /**
   * @return the latest version of the decision definition with the given key (from any tenant)
   *
   * @throws ProcessEngineException if more than one tenant has a decision definition with the given key
   *
   * @see #findLatestDecisionDefinitionByKeyAndTenantId(String, String)
   */
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity findLatestDecisionDefinitionByKey(String decisionDefinitionKey) {
    @SuppressWarnings("unchecked")
    List<com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity> decisionDefinitions = getDbEntityManager().selectList("selectLatestDecisionDefinitionByKey", configureParameterizedQuery(decisionDefinitionKey));

    if (decisionDefinitions.isEmpty()) {
      return null;

    } else if (decisionDefinitions.size() == 1) {
      return decisionDefinitions.iterator().next();

    } else {
      throw LOG.multipleTenantsForDecisionDefinitionKeyException(decisionDefinitionKey);
    }
  }

  /**
   * @return the latest version of the decision definition with the given key and tenant id
   *
   * @see #findLatestDecisionDefinitionByKey(String)
   */
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity findLatestDecisionDefinitionByKeyAndTenantId(String decisionDefinitionKey, String tenantId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("decisionDefinitionKey", decisionDefinitionKey);
    parameters.put("tenantId", tenantId);

    if (tenantId == null) {
      return (com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity) getDbEntityManager().selectOne("selectLatestDecisionDefinitionByKeyWithoutTenantId", parameters);
    } else {
      return (com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity) getDbEntityManager().selectOne("selectLatestDecisionDefinitionByKeyAndTenantId", parameters);
    }
  }

  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity findDecisionDefinitionByKeyAndVersion(String decisionDefinitionKey, Integer decisionDefinitionVersion) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("decisionDefinitionVersion", decisionDefinitionVersion);
    parameters.put("decisionDefinitionKey", decisionDefinitionKey);
    return (com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity) getDbEntityManager().selectOne("selectDecisionDefinitionByKeyAndVersion", configureParameterizedQuery(parameters));
  }

  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity findDecisionDefinitionByKeyVersionAndTenantId(String decisionDefinitionKey, Integer decisionDefinitionVersion, String tenantId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("decisionDefinitionVersion", decisionDefinitionVersion);
    parameters.put("decisionDefinitionKey", decisionDefinitionKey);
    parameters.put("tenantId", tenantId);
    if (tenantId == null) {
      return (com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity) getDbEntityManager().selectOne("selectDecisionDefinitionByKeyVersionWithoutTenantId", parameters);
    } else {
      return (com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity) getDbEntityManager().selectOne("selectDecisionDefinitionByKeyVersionAndTenantId", parameters);
    }
  }

  @SuppressWarnings("unchecked")
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity findDecisionDefinitionByKeyVersionTagAndTenantId(String decisionDefinitionKey, String decisionDefinitionVersionTag, String tenantId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("decisionDefinitionVersionTag", decisionDefinitionVersionTag);
    parameters.put("decisionDefinitionKey", decisionDefinitionKey);
    parameters.put("tenantId", tenantId);

    ListQueryParameterObject parameterObject = new ListQueryParameterObject();
    parameterObject.setParameter(parameters);

    List<com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity> decisionDefinitions = getDbEntityManager().selectList("selectDecisionDefinitionByKeyVersionTag", parameterObject);

    if (decisionDefinitions.size() == 1) {
      return decisionDefinitions.get(0);
    } else if (decisionDefinitions.isEmpty()) {
      return null;
    } else {
      throw LOG.multipleDefinitionsForVersionTagException(decisionDefinitionKey, decisionDefinitionVersionTag);
    }
  }

  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity findDecisionDefinitionByDeploymentAndKey(String deploymentId, String decisionDefinitionKey) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("decisionDefinitionKey", decisionDefinitionKey);
    return (com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity) getDbEntityManager().selectOne("selectDecisionDefinitionByDeploymentAndKey", parameters);
  }

  @SuppressWarnings("unchecked")
  public List<DecisionDefinition> findDecisionDefinitionsByQueryCriteria(DecisionDefinitionQueryImpl decisionDefinitionQuery, Page page) {
    configureDecisionDefinitionQuery(decisionDefinitionQuery);
    return getDbEntityManager().selectList("selectDecisionDefinitionsByQueryCriteria", decisionDefinitionQuery, page);
  }

  public long findDecisionDefinitionCountByQueryCriteria(DecisionDefinitionQueryImpl decisionDefinitionQuery) {
    configureDecisionDefinitionQuery(decisionDefinitionQuery);
    return (Long) getDbEntityManager().selectOne("selectDecisionDefinitionCountByQueryCriteria", decisionDefinitionQuery);
  }

  public String findPreviousDecisionDefinitionId(String decisionDefinitionKey, Integer version, String tenantId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("key", decisionDefinitionKey);
    params.put("version", version);
    params.put("tenantId", tenantId);
    return (String) getDbEntityManager().selectOne("selectPreviousDecisionDefinitionId", params);
  }

  @SuppressWarnings("unchecked")
  public List<DecisionDefinition> findDecisionDefinitionByDeploymentId(String deploymentId) {
    return getDbEntityManager().selectList("selectDecisionDefinitionByDeploymentId", deploymentId);
  }



  protected void createDefaultAuthorizations(DecisionDefinition decisionDefinition) {
    if(isAuthorizationEnabled()) {
      ResourceAuthorizationProvider provider = getResourceAuthorizationProvider();
      AuthorizationEntity[] authorizations = provider.newDecisionDefinition(decisionDefinition);
      saveDefaultAuthorizations(authorizations);
    }
  }

  protected void configureDecisionDefinitionQuery(DecisionDefinitionQueryImpl query) {
    getAuthorizationManager().configureDecisionDefinitionQuery(query);
    getTenantManager().configureQuery(query);
  }

  protected ListQueryParameterObject configureParameterizedQuery(Object parameter) {
    return getTenantManager().configureQuery(parameter);
  }

  @Override
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity findLatestDefinitionById(String id) {
    return findDecisionDefinitionById(id);
  }

  @Override
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity findLatestDefinitionByKey(String key) {
    return findLatestDecisionDefinitionByKey(key);
  }

  @Override
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity getCachedResourceDefinitionEntity(String definitionId) {
    return getDbEntityManager().getCachedEntity(com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity.class, definitionId);
  }

  @Override
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity findLatestDefinitionByKeyAndTenantId(String definitionKey, String tenantId) {
    return findLatestDecisionDefinitionByKeyAndTenantId(definitionKey, tenantId);
  }

  @Override
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity findDefinitionByKeyVersionAndTenantId(String definitionKey, Integer definitionVersion, String tenantId) {
    return findDecisionDefinitionByKeyVersionAndTenantId(definitionKey, definitionVersion, tenantId);
  }

  @Override
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity findDefinitionByKeyVersionTagAndTenantId(String definitionKey, String definitionVersionTag, String tenantId) {
    return findDecisionDefinitionByKeyVersionTagAndTenantId(definitionKey, definitionVersionTag, tenantId);
  }

  @Override
  public DecisionDefinitionEntity findDefinitionByDeploymentAndKey(String deploymentId, String definitionKey) {
    return findDecisionDefinitionByDeploymentAndKey(deploymentId, definitionKey);
  }
}
