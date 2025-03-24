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
package com.finture.bpm.engine.impl.db;

import java.util.ArrayList;
import java.util.List;

import com.finture.bpm.engine.ProcessEngineException;
import com.finture.bpm.engine.authorization.Permission;
import com.finture.bpm.engine.authorization.Resource;
import com.finture.bpm.engine.impl.context.Context;
import com.finture.bpm.engine.impl.db.CompositePermissionCheck;
import com.finture.bpm.engine.impl.db.PermissionCheck;
import com.finture.bpm.engine.impl.persistence.entity.AuthorizationManager;

/**
 * @author Thorben Lindhauer
 *
 */
public class PermissionCheckBuilder {

  protected List<com.finture.bpm.engine.impl.db.PermissionCheck> atomicChecks = new ArrayList<com.finture.bpm.engine.impl.db.PermissionCheck>();
  protected List<com.finture.bpm.engine.impl.db.CompositePermissionCheck> compositeChecks = new ArrayList<com.finture.bpm.engine.impl.db.CompositePermissionCheck>();
  protected boolean disjunctive = true;

  protected PermissionCheckBuilder parent;

  public PermissionCheckBuilder() {
  }

  public PermissionCheckBuilder(PermissionCheckBuilder parent) {
    this.parent = parent;
  }

  public PermissionCheckBuilder disjunctive() {
    this.disjunctive = true;
    return this;
  }

  public PermissionCheckBuilder conjunctive() {
    this.disjunctive = false;
    return this;
  }

  public PermissionCheckBuilder atomicCheck(Resource resource, String queryParam, Permission permission) {
    if (!isPermissionDisabled(permission)) {
      com.finture.bpm.engine.impl.db.PermissionCheck permCheck = new com.finture.bpm.engine.impl.db.PermissionCheck();
      permCheck.setResource(resource);
      permCheck.setResourceIdQueryParam(queryParam);
      permCheck.setPermission(permission);
      this.atomicChecks.add(permCheck);
    }

    return this;
  }

  public PermissionCheckBuilder atomicCheckForResourceId(Resource resource, String resourceId, Permission permission) {
    if (!isPermissionDisabled(permission)) {
      com.finture.bpm.engine.impl.db.PermissionCheck permCheck = new com.finture.bpm.engine.impl.db.PermissionCheck();
      permCheck.setResource(resource);
      permCheck.setResourceId(resourceId);
      permCheck.setPermission(permission);
      this.atomicChecks.add(permCheck);
    }

    return this;
  }

  public PermissionCheckBuilder composite() {
    return new PermissionCheckBuilder(this);
  }

  public PermissionCheckBuilder done() {
    parent.compositeChecks.add(this.build());
    return parent;
  }

  public com.finture.bpm.engine.impl.db.CompositePermissionCheck build() {
    validate();

    com.finture.bpm.engine.impl.db.CompositePermissionCheck permissionCheck = new CompositePermissionCheck(disjunctive);
    permissionCheck.setAtomicChecks(atomicChecks);
    permissionCheck.setCompositeChecks(compositeChecks);

    return permissionCheck;
  }
  
  public List<PermissionCheck> getAtomicChecks() {
    return atomicChecks;
  }

  protected void validate() {
    if (!atomicChecks.isEmpty() && !compositeChecks.isEmpty()) {
      throw new ProcessEngineException("Mixed authorization checks of atomic and composite permissions are not supported");
    }
  }

  public boolean isPermissionDisabled(Permission permission) {
    AuthorizationManager authorizationManager = Context.getCommandContext().getAuthorizationManager();
    return authorizationManager.isPermissionDisabled(permission);
  }
}
