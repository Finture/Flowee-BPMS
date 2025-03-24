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

import com.finture.bpm.engine.impl.Page;
import com.finture.bpm.engine.impl.cfg.auth.ResourceAuthorizationProvider;
import com.finture.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity;
import com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionQueryImpl;
import com.finture.bpm.engine.impl.persistence.AbstractManager;
import com.finture.bpm.engine.impl.persistence.AbstractResourceDefinitionManager;
import com.finture.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import com.finture.bpm.engine.repository.DecisionRequirementsDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Johannes Heinemann
 */
public class DecisionRequirementsDefinitionManager extends AbstractManager implements AbstractResourceDefinitionManager<com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity> {

  public void insertDecisionRequirementsDefinition(com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity decisionRequirementsDefinition) {
    getDbEntityManager().insert(decisionRequirementsDefinition);
    createDefaultAuthorizations(decisionRequirementsDefinition);
  }

  public void deleteDecisionRequirementsDefinitionsByDeploymentId(String deploymentId) {
    getDbEntityManager().delete(DecisionDefinitionEntity.class, "deleteDecisionRequirementsDefinitionsByDeploymentId", deploymentId);
  }

  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity findDecisionRequirementsDefinitionById(String decisionRequirementsDefinitionId) {
    return getDbEntityManager().selectById(com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity.class, decisionRequirementsDefinitionId);
  }

  public String findPreviousDecisionRequirementsDefinitionId(String decisionRequirementsDefinitionKey, Integer version, String tenantId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("key", decisionRequirementsDefinitionKey);
    params.put("version", version);
    params.put("tenantId", tenantId);
    return (String) getDbEntityManager().selectOne("selectPreviousDecisionRequirementsDefinitionId", params);
  }

  @SuppressWarnings("unchecked")
  public List<DecisionRequirementsDefinition> findDecisionRequirementsDefinitionByDeploymentId(String deploymentId) {
    return getDbEntityManager().selectList("selectDecisionRequirementsDefinitionByDeploymentId", deploymentId);
  }

  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity findDecisionRequirementsDefinitionByDeploymentAndKey(String deploymentId, String decisionRequirementsDefinitionKey) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("decisionRequirementsDefinitionKey", decisionRequirementsDefinitionKey);
    return (com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity) getDbEntityManager().selectOne("selectDecisionRequirementsDefinitionByDeploymentAndKey", parameters);
  }

  /**
   * @return the latest version of the decision requirements definition with the given key and tenant id
   */
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity findLatestDecisionRequirementsDefinitionByKeyAndTenantId(String decisionRequirementsDefinitionKey, String tenantId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("decisionRequirementsDefinitionKey", decisionRequirementsDefinitionKey);
    parameters.put("tenantId", tenantId);

    if (tenantId == null) {
      return (com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity) getDbEntityManager().selectOne("selectLatestDecisionRequirementsDefinitionByKeyWithoutTenantId", parameters);
    } else {
      return (com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity) getDbEntityManager().selectOne("selectLatestDecisionRequirementsDefinitionByKeyAndTenantId", parameters);
    }
  }

  @SuppressWarnings("unchecked")
  public List<DecisionRequirementsDefinition> findDecisionRequirementsDefinitionsByQueryCriteria(DecisionRequirementsDefinitionQueryImpl query, Page page) {
    configureDecisionRequirementsDefinitionQuery(query);
    return getDbEntityManager().selectList("selectDecisionRequirementsDefinitionsByQueryCriteria", query, page);
  }

  public long findDecisionRequirementsDefinitionCountByQueryCriteria(DecisionRequirementsDefinitionQueryImpl query) {
    configureDecisionRequirementsDefinitionQuery(query);
    return (Long) getDbEntityManager().selectOne("selectDecisionRequirementsDefinitionCountByQueryCriteria", query);
  }

  protected void createDefaultAuthorizations(DecisionRequirementsDefinition decisionRequirementsDefinition) {
    if (isAuthorizationEnabled()) {
      ResourceAuthorizationProvider provider = getResourceAuthorizationProvider();
      AuthorizationEntity[] authorizations = provider.newDecisionRequirementsDefinition(decisionRequirementsDefinition);
      saveDefaultAuthorizations(authorizations);
    }
  }

  protected void configureDecisionRequirementsDefinitionQuery(DecisionRequirementsDefinitionQueryImpl query) {
    getAuthorizationManager().configureDecisionRequirementsDefinitionQuery(query);
    getTenantManager().configureQuery(query);
  }


  @Override
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity findLatestDefinitionByKey(String key) {
    return null;
  }

  @Override
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity findLatestDefinitionById(String id) {
    return getDbEntityManager().selectById(com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity.class, id);
  }

  @Override
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity findLatestDefinitionByKeyAndTenantId(String definitionKey, String tenantId) {
    return null;
  }

  @Override
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity findDefinitionByKeyVersionAndTenantId(String definitionKey, Integer definitionVersion, String tenantId) {
    return null;
  }

  @Override
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity findDefinitionByKeyVersionTagAndTenantId(String definitionKey, String definitionVersionTag, String tenantId) {
    return null;
  }

  @Override
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity findDefinitionByDeploymentAndKey(String deploymentId, String definitionKey) {
    return null;
  }

  @Override
  public com.finture.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity getCachedResourceDefinitionEntity(String definitionId) {
    return getDbEntityManager().getCachedEntity(DecisionRequirementsDefinitionEntity.class, definitionId);
  }
}
