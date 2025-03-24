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

import com.finture.bpm.engine.history.HistoricDetailQuery;
import com.finture.bpm.engine.impl.QueryPropertyImpl;
import com.finture.bpm.engine.query.QueryProperty;


/**
 * Contains the possible properties which can be used in a {@link HistoricDetailQuery}.
 *
 * @author Tom Baeyens
 */
public interface HistoricDetailQueryProperty {

  public static final QueryProperty PROCESS_INSTANCE_ID = new com.finture.bpm.engine.impl.QueryPropertyImpl("PROC_INST_ID_");
  public static final QueryProperty VARIABLE_NAME = new com.finture.bpm.engine.impl.QueryPropertyImpl("NAME_");
  public static final QueryProperty VARIABLE_TYPE = new com.finture.bpm.engine.impl.QueryPropertyImpl("TYPE_");
  public static final QueryProperty VARIABLE_REVISION = new com.finture.bpm.engine.impl.QueryPropertyImpl("REV_");
  public static final QueryProperty TIME = new com.finture.bpm.engine.impl.QueryPropertyImpl("TIME_");
  public static final QueryProperty SEQUENCE_COUNTER = new com.finture.bpm.engine.impl.QueryPropertyImpl("SEQUENCE_COUNTER_");
  public static final QueryProperty TENANT_ID = new QueryPropertyImpl("TENANT_ID_");
}
