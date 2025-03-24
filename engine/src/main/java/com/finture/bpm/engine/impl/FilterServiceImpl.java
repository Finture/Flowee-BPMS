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

import java.util.List;

import com.finture.bpm.engine.EntityTypes;
import com.finture.bpm.engine.FilterService;
import com.finture.bpm.engine.filter.Filter;
import com.finture.bpm.engine.filter.FilterQuery;
import com.finture.bpm.engine.impl.ServiceImpl;
import com.finture.bpm.engine.impl.cmd.CreateFilterCmd;
import com.finture.bpm.engine.impl.cmd.DeleteFilterCmd;
import com.finture.bpm.engine.impl.cmd.ExecuteFilterCountCmd;
import com.finture.bpm.engine.impl.cmd.ExecuteFilterListCmd;
import com.finture.bpm.engine.impl.cmd.ExecuteFilterListPageCmd;
import com.finture.bpm.engine.impl.cmd.ExecuteFilterSingleResultCmd;
import com.finture.bpm.engine.impl.cmd.GetFilterCmd;
import com.finture.bpm.engine.impl.cmd.SaveFilterCmd;
import com.finture.bpm.engine.impl.filter.FilterQueryImpl;
import com.finture.bpm.engine.query.Query;


/**
 * @author Sebastian Menski
 */
public class FilterServiceImpl extends ServiceImpl implements FilterService {

  public Filter newTaskFilter() {
    return commandExecutor.execute(new CreateFilterCmd(EntityTypes.TASK));
  }

  public Filter newTaskFilter(String filterName) {
    return newTaskFilter().setName(filterName);
  }

  public FilterQuery createFilterQuery() {
    return new FilterQueryImpl(commandExecutor);
  }

  public FilterQuery createTaskFilterQuery() {
    return new FilterQueryImpl(commandExecutor).filterResourceType(EntityTypes.TASK);
  }

  public Filter saveFilter(Filter filter) {
    return commandExecutor.execute(new SaveFilterCmd(filter));
  }

  public Filter getFilter(String filterId) {
    return commandExecutor.execute(new GetFilterCmd(filterId));
  }

  public void deleteFilter(String filterId) {
    commandExecutor.execute(new DeleteFilterCmd(filterId));
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> list(String filterId) {
    return (List<T>) commandExecutor.execute(new ExecuteFilterListCmd(filterId));
  }

  @SuppressWarnings("unchecked")
  public <T, Q extends Query<?, T>> List<T> list(String filterId, Q extendingQuery) {
    return (List<T>) commandExecutor.execute(new ExecuteFilterListCmd(filterId, extendingQuery));
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> listPage(String filterId, int firstResult, int maxResults) {
    return (List<T>) commandExecutor.execute(new ExecuteFilterListPageCmd(filterId, firstResult, maxResults));
  }

  @SuppressWarnings("unchecked")
  public <T, Q extends Query<?, T>> List<T> listPage(String filterId, Q extendingQuery, int firstResult, int maxResults) {
    return (List<T>) commandExecutor.execute(new ExecuteFilterListPageCmd(filterId, extendingQuery, firstResult, maxResults));
  }

  @SuppressWarnings("unchecked")
  public <T> T singleResult(String filterId) {
    return (T) commandExecutor.execute(new ExecuteFilterSingleResultCmd(filterId));
  }

  @SuppressWarnings("unchecked")
  public <T, Q extends Query<?, T>> T singleResult(String filterId, Q extendingQuery) {
    return (T) commandExecutor.execute(new ExecuteFilterSingleResultCmd(filterId, extendingQuery));
  }

  public Long count(String filterId) {
    return commandExecutor.execute(new ExecuteFilterCountCmd(filterId));
  }

  public Long count(String filterId, Query<?, ?> extendingQuery) {
    return commandExecutor.execute(new ExecuteFilterCountCmd(filterId, extendingQuery));
  }

}
