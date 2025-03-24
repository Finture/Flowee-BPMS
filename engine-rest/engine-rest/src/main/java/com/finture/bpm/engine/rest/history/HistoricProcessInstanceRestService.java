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
package com.finture.bpm.engine.rest.history;

import com.finture.bpm.engine.rest.dto.CountResultDto;
import com.finture.bpm.engine.rest.dto.batch.BatchDto;
import com.finture.bpm.engine.rest.dto.history.DeleteHistoricProcessInstancesDto;
import com.finture.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import com.finture.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import com.finture.bpm.engine.rest.dto.history.batch.removaltime.SetRemovalTimeToHistoricProcessInstancesDto;
import com.finture.bpm.engine.rest.sub.history.HistoricProcessInstanceResource;
import com.finture.bpm.engine.history.HistoricProcessInstanceQuery;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path(HistoricProcessInstanceRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface HistoricProcessInstanceRestService {

  public static final String PATH = "/process-instance";

  @Path("/{id}")
  HistoricProcessInstanceResource getHistoricProcessInstance(@PathParam("id") String processInstanceId);

  /**
   * Exposes the {@link HistoricProcessInstanceQuery} interface as a REST
   * service.
   *
   * @param uriInfo
   * @param firstResult
   * @param maxResults
   * @return
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<HistoricProcessInstanceDto> getHistoricProcessInstances(@Context UriInfo uriInfo, @QueryParam("firstResult") Integer firstResult,
                                                               @QueryParam("maxResults") Integer maxResults);

  /**
   * @param query
   * @param firstResult
   * @param maxResults
   * @return
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  List<HistoricProcessInstanceDto> queryHistoricProcessInstances(HistoricProcessInstanceQueryDto query, @QueryParam("firstResult") Integer firstResult,
                                                                 @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto getHistoricProcessInstancesCount(@Context UriInfo uriInfo);

  @POST
  @Path("/count")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto queryHistoricProcessInstancesCount(HistoricProcessInstanceQueryDto query);

  @GET
  @Path("/report")
  @Produces({ MediaType.APPLICATION_JSON, "text/csv", "application/csv" })
  Response getHistoricProcessInstancesReport(@Context UriInfo uriInfo, @Context Request request);

  @POST
  @Path("/delete")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  BatchDto deleteAsync(DeleteHistoricProcessInstancesDto dto);

  @POST
  @Path("/set-removal-time")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  BatchDto setRemovalTimeAsync(SetRemovalTimeToHistoricProcessInstancesDto dto);
  
  @DELETE
  @Path("/{id}/variable-instances")
  Response deleteHistoricVariableInstancesByProcessInstanceId(@PathParam("id") String processInstanceId);
}
