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
package com.finture.bpm.engine.rest.sub.runtime;

import com.finture.bpm.engine.rest.dto.runtime.VariableInstanceDto;
import com.finture.bpm.engine.rest.sub.VariableResource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Daniel Meyer
 * @author Ronny Bräunlich
 */
public interface VariableInstanceResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public VariableInstanceDto getResource(
      @QueryParam(VariableResource.DESERIALIZE_VALUE_QUERY_PARAM) @DefaultValue("true") boolean deserializeObjectValue);


  @GET
  @Path("/data")
  public Response getResourceBinary();

}
