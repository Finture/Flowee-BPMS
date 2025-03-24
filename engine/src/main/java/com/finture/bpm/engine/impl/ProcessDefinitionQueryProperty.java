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
package com.finture.bpm.engine.impl;

import com.finture.bpm.engine.impl.QueryPropertyImpl;
import com.finture.bpm.engine.query.QueryProperty;
import com.finture.bpm.engine.repository.ProcessDefinitionQuery;


/**
 * Contains the possible properties that can be used in a {@link ProcessDefinitionQuery}.
 *
 * @author Joram Barrez
 */
public interface ProcessDefinitionQueryProperty {

  public static final QueryProperty PROCESS_DEFINITION_KEY = new com.finture.bpm.engine.impl.QueryPropertyImpl("KEY_");
  public static final QueryProperty PROCESS_DEFINITION_CATEGORY = new com.finture.bpm.engine.impl.QueryPropertyImpl("CATEGORY_");
  public static final QueryProperty PROCESS_DEFINITION_ID = new com.finture.bpm.engine.impl.QueryPropertyImpl("ID_");
  public static final QueryProperty PROCESS_DEFINITION_VERSION = new com.finture.bpm.engine.impl.QueryPropertyImpl("VERSION_");
  public static final QueryProperty PROCESS_DEFINITION_NAME = new com.finture.bpm.engine.impl.QueryPropertyImpl("NAME_");
  public static final QueryProperty DEPLOYMENT_ID = new com.finture.bpm.engine.impl.QueryPropertyImpl("DEPLOYMENT_ID_");
  public static final QueryProperty DEPLOY_TIME = new com.finture.bpm.engine.impl.QueryPropertyImpl("DEPLOY_TIME_");
  public static final QueryProperty TENANT_ID = new com.finture.bpm.engine.impl.QueryPropertyImpl("TENANT_ID_");
  public static final QueryProperty VERSION_TAG = new QueryPropertyImpl("VERSION_TAG_");

}
