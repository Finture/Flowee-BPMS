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
package com.finture.bpm.engine.rest.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finture.bpm.engine.rest.TaskRestService;
import com.finture.bpm.engine.rest.dto.CountResultDto;
import com.finture.bpm.engine.rest.dto.task.TaskDto;
import com.finture.bpm.engine.rest.dto.task.TaskQueryDto;
import com.finture.bpm.engine.rest.dto.task.TaskWithAttachmentAndCommentDto;
import com.finture.bpm.engine.rest.exception.InvalidRequestException;
import com.finture.bpm.engine.rest.hal.Hal;
import com.finture.bpm.engine.rest.hal.task.HalTaskList;
import com.finture.bpm.engine.rest.sub.task.TaskReportResource;
import com.finture.bpm.engine.rest.sub.task.TaskResource;
import com.finture.bpm.engine.rest.sub.task.impl.TaskReportResourceImpl;
import com.finture.bpm.engine.rest.sub.task.impl.TaskResourceImpl;
import com.finture.bpm.engine.rest.util.QueryUtil;
import com.finture.bpm.engine.ProcessEngine;
import com.finture.bpm.engine.TaskService;
import com.finture.bpm.engine.exception.NotValidException;
import com.finture.bpm.engine.task.Task;
import com.finture.bpm.engine.task.TaskQuery;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskRestServiceImpl extends AbstractRestProcessEngineAware implements TaskRestService {

  public static final List<Variant> VARIANTS = Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE, Hal.APPLICATION_HAL_JSON_TYPE).add().build();

  public TaskRestServiceImpl(String engineName, final ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public Object getTasks(Request request, UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    Variant variant = request.selectVariant(VARIANTS);
    if (variant != null) {
      if (MediaType.APPLICATION_JSON_TYPE.equals(variant.getMediaType())) {
        return getJsonTasks(uriInfo, firstResult, maxResults);
      }
      else if (Hal.APPLICATION_HAL_JSON_TYPE.equals(variant.getMediaType())) {
        return getHalTasks(uriInfo, firstResult, maxResults);
      }
    }
    throw new InvalidRequestException(Status.NOT_ACCEPTABLE, "No acceptable content-type found");
  }

  public List<TaskDto> getJsonTasks(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    // get list of tasks
    TaskQueryDto queryDto = new TaskQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryTasks(queryDto, firstResult, maxResults);
  }

  public HalTaskList getHalTasks(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    TaskQueryDto queryDto = new TaskQueryDto(getObjectMapper(), uriInfo.getQueryParameters());

    ProcessEngine engine = getProcessEngine();
    TaskQuery query = queryDto.toQuery(engine);

    // get list of tasks
    List<Task> matchingTasks = executeTaskQuery(firstResult, maxResults, query);

    // get total count
    long count = query.count();

    return HalTaskList.generate(matchingTasks, count, engine);
  }

  @Override
  public List<TaskDto> queryTasks(TaskQueryDto queryDto, Integer firstResult,
                                  Integer maxResults) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    TaskQuery query = queryDto.toQuery(engine);

    List<Task> matchingTasks = executeTaskQuery(firstResult, maxResults, query);

    List<TaskDto> tasks = new ArrayList<TaskDto>();
    if (Boolean.TRUE.equals(queryDto.getWithCommentAttachmentInfo())) {
      tasks = matchingTasks.stream().map(TaskWithAttachmentAndCommentDto::fromEntity).collect(Collectors.toList());
    }
    else {
      tasks = matchingTasks.stream().map(TaskDto::fromEntity).collect(Collectors.toList());
    }
    return tasks;
  }

  protected List<Task> executeTaskQuery(Integer firstResult, Integer maxResults, TaskQuery query) {

    // enable initialization of form key:
    query.initializeFormKeys();
    return QueryUtil.list(query, firstResult, maxResults);
  }

  @Override
  public CountResultDto getTasksCount(UriInfo uriInfo) {
    TaskQueryDto queryDto = new TaskQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryTasksCount(queryDto);
  }

  @Override
  public CountResultDto queryTasksCount(TaskQueryDto queryDto) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    TaskQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

  @Override
  public TaskResource getTask(String id, boolean withCommentAttachmentInfo) {
    return new TaskResourceImpl(getProcessEngine(), id, relativeRootResourcePath, getObjectMapper(), withCommentAttachmentInfo);
  }

  @Override
  public void createTask(TaskDto taskDto) {
    ProcessEngine engine = getProcessEngine();
    TaskService taskService = engine.getTaskService();

    Task newTask = taskService.newTask(taskDto.getId());
    taskDto.updateTask(newTask);

    try {
      taskService.saveTask(newTask);

    } catch (NotValidException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Could not save task: " + e.getMessage());
    }

  }

  @Override
  public TaskReportResource getTaskReportResource() {
    return new TaskReportResourceImpl(getProcessEngine());
  }
}
