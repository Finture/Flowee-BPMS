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
import com.finture.bpm.engine.task.TaskQuery;



/**
 * Contains the possible properties that can be used in a {@link TaskQuery}.
 *
 * @author Joram Barrez
 */
public interface TaskQueryProperty {

  public static final QueryProperty TASK_ID = new com.finture.bpm.engine.impl.QueryPropertyImpl("ID_");
  public static final QueryProperty NAME = new com.finture.bpm.engine.impl.QueryPropertyImpl("NAME_");
  public static final QueryProperty NAME_CASE_INSENSITIVE = new com.finture.bpm.engine.impl.QueryPropertyImpl("NAME_", "LOWER");
  public static final QueryProperty DESCRIPTION = new com.finture.bpm.engine.impl.QueryPropertyImpl("DESCRIPTION_");
  public static final QueryProperty PRIORITY = new com.finture.bpm.engine.impl.QueryPropertyImpl("PRIORITY_");
  public static final QueryProperty ASSIGNEE = new com.finture.bpm.engine.impl.QueryPropertyImpl("ASSIGNEE_");
  public static final QueryProperty CREATE_TIME = new com.finture.bpm.engine.impl.QueryPropertyImpl("CREATE_TIME_");
  public static final QueryProperty LAST_UPDATED = new com.finture.bpm.engine.impl.QueryPropertyImpl("LAST_UPDATED_");
  public static final QueryProperty PROCESS_INSTANCE_ID = new com.finture.bpm.engine.impl.QueryPropertyImpl("PROC_INST_ID_");
  public static final QueryProperty CASE_INSTANCE_ID = new com.finture.bpm.engine.impl.QueryPropertyImpl("CASE_INST_ID_");
  public static final QueryProperty EXECUTION_ID = new com.finture.bpm.engine.impl.QueryPropertyImpl("EXECUTION_ID_");
  public static final QueryProperty CASE_EXECUTION_ID = new com.finture.bpm.engine.impl.QueryPropertyImpl("CASE_EXECUTION_ID_");
  public static final QueryProperty DUE_DATE = new com.finture.bpm.engine.impl.QueryPropertyImpl("DUE_DATE_");
  public static final QueryProperty FOLLOW_UP_DATE = new com.finture.bpm.engine.impl.QueryPropertyImpl("FOLLOW_UP_DATE_");
  public static final QueryProperty TENANT_ID = new QueryPropertyImpl("TENANT_ID_");

}
