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
package com.finture.bpm.engine.rest.sub.task;

import com.finture.bpm.engine.rest.dto.VariableValueDto;
import com.finture.bpm.engine.rest.dto.task.*;
import com.finture.bpm.engine.rest.hal.Hal;
import com.finture.bpm.engine.rest.sub.VariableResource;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

public interface TaskResource {

  @GET
  @Produces({MediaType.APPLICATION_JSON, Hal.APPLICATION_HAL_JSON})
  Object getTask(@Context Request request);

  @GET
  @Path("/form")
  @Produces(MediaType.APPLICATION_JSON)
  FormDto getForm();

  @POST
  @Path("/submit-form")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  Response submit(CompleteTaskDto dto);

  @GET
  @Path("/rendered-form")
  @Produces(MediaType.APPLICATION_XHTML_XML)
  Response getRenderedForm();

  @GET
  @Path("/deployed-form")
  Response getDeployedForm();

  @POST
  @Path("/claim")
  @Consumes(MediaType.APPLICATION_JSON)
  void claim(UserIdDto dto);

  @POST
  @Path("/unclaim")
  void unclaim();

  @POST
  @Path("/complete")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  Response complete(CompleteTaskDto dto);

  @POST
  @Path("/resolve")
  @Consumes(MediaType.APPLICATION_JSON)
  void resolve(CompleteTaskDto dto);

  @POST
  @Path("/delegate")
  @Consumes(MediaType.APPLICATION_JSON)
  void delegate(UserIdDto delegatedUser);

  @POST
  @Path("/assignee")
  @Consumes(MediaType.APPLICATION_JSON)
  void setAssignee(UserIdDto dto);

  @GET
  @Path("/identity-links")
  @Produces(MediaType.APPLICATION_JSON)
  List<IdentityLinkDto> getIdentityLinks(@QueryParam("type") String type);

  @POST
  @Path("/identity-links")
  @Consumes(MediaType.APPLICATION_JSON)
  void addIdentityLink(IdentityLinkDto identityLink);

  @POST
  @Path("/identity-links/delete")
  @Consumes(MediaType.APPLICATION_JSON)
  void deleteIdentityLink(IdentityLinkDto identityLink);

  @Path("/comment")
  TaskCommentResource getTaskCommentResource();

  @Path("/attachment")
  TaskAttachmentResource getAttachmentResource();

  @Path("/variables")
  VariableResource getVariables();

  @Path("/localVariables")
  VariableResource getLocalVariables();

  @GET
  @Path("/form-variables")
  @Produces(MediaType.APPLICATION_JSON)
  Map<String, VariableValueDto> getFormVariables(@QueryParam("variableNames") String variableNames,
                                                 @QueryParam(VariableResource.DESERIALIZE_VALUES_QUERY_PARAM) @DefaultValue("true") boolean deserializeValues);

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public void updateTask(TaskDto task);

  @DELETE
  void deleteTask(@PathParam("id") String id);

  @POST
  @Path("/bpmnError")
  @Consumes(MediaType.APPLICATION_JSON)
  void handleBpmnError(TaskBpmnErrorDto dto);

  @POST
  @Path("/bpmnEscalation")
  @Consumes(MediaType.APPLICATION_JSON)
  void handleEscalation(TaskEscalationDto dto);
}
