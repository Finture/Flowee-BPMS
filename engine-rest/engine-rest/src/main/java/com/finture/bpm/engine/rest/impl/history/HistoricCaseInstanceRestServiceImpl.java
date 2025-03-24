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
package com.finture.bpm.engine.rest.impl.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finture.bpm.engine.rest.dto.CountResultDto;
import com.finture.bpm.engine.rest.dto.history.HistoricCaseInstanceDto;
import com.finture.bpm.engine.rest.dto.history.HistoricCaseInstanceQueryDto;
import com.finture.bpm.engine.rest.history.HistoricCaseInstanceRestService;
import com.finture.bpm.engine.rest.sub.history.HistoricCaseInstanceResource;
import com.finture.bpm.engine.rest.sub.history.impl.HistoricCaseInstanceResourceImpl;
import com.finture.bpm.engine.rest.util.QueryUtil;
import com.finture.bpm.engine.ProcessEngine;
import com.finture.bpm.engine.history.HistoricCaseInstance;
import com.finture.bpm.engine.history.HistoricCaseInstanceQuery;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

public class HistoricCaseInstanceRestServiceImpl implements HistoricCaseInstanceRestService {

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricCaseInstanceRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  @Override
  public HistoricCaseInstanceResource getHistoricCaseInstance(String caseInstanceId) {
    return new HistoricCaseInstanceResourceImpl(processEngine, caseInstanceId);
  }

  @Override
  public List<HistoricCaseInstanceDto> getHistoricCaseInstances(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    HistoricCaseInstanceQueryDto queryHistoricCaseInstanceDto = new HistoricCaseInstanceQueryDto(objectMapper, uriInfo.getQueryParameters());
    return queryHistoricCaseInstances(queryHistoricCaseInstanceDto, firstResult, maxResults);
  }

  @Override
  public List<HistoricCaseInstanceDto> queryHistoricCaseInstances(HistoricCaseInstanceQueryDto queryDto, Integer firstResult, Integer maxResults) {
    HistoricCaseInstanceQuery query = queryDto.toQuery(processEngine);

    List<HistoricCaseInstance> matchingHistoricCaseInstances = QueryUtil.list(query, firstResult, maxResults);

    List<HistoricCaseInstanceDto> historicCaseInstanceDtoResults = new ArrayList<HistoricCaseInstanceDto>();
    for (HistoricCaseInstance historicCaseInstance : matchingHistoricCaseInstances) {
      HistoricCaseInstanceDto resultHistoricCaseInstanceDto = HistoricCaseInstanceDto.fromHistoricCaseInstance(historicCaseInstance);
      historicCaseInstanceDtoResults.add(resultHistoricCaseInstanceDto);
    }
    return historicCaseInstanceDtoResults;
  }

  @Override
  public CountResultDto getHistoricCaseInstancesCount(UriInfo uriInfo) {
    HistoricCaseInstanceQueryDto queryDto = new HistoricCaseInstanceQueryDto(objectMapper, uriInfo.getQueryParameters());
    return queryHistoricCaseInstancesCount(queryDto);
  }

  @Override
  public CountResultDto queryHistoricCaseInstancesCount(HistoricCaseInstanceQueryDto queryDto) {
    HistoricCaseInstanceQuery query = queryDto.toQuery(processEngine);

    long count = query.count();

    return new CountResultDto(count);
  }

}
