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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.UriInfo;
import com.finture.bpm.engine.HistoryService;
import com.finture.bpm.engine.ProcessEngine;
import com.finture.bpm.engine.batch.Batch;
import com.finture.bpm.engine.batch.history.HistoricBatch;
import com.finture.bpm.engine.batch.history.HistoricBatchQuery;
import com.finture.bpm.engine.history.CleanableHistoricBatchReport;
import com.finture.bpm.engine.history.CleanableHistoricBatchReportResult;
import com.finture.bpm.engine.history.SetRemovalTimeSelectModeForHistoricBatchesBuilder;
import com.finture.bpm.engine.rest.dto.CountResultDto;
import com.finture.bpm.engine.rest.dto.batch.BatchDto;
import com.finture.bpm.engine.rest.dto.history.batch.CleanableHistoricBatchReportDto;
import com.finture.bpm.engine.rest.dto.history.batch.CleanableHistoricBatchReportResultDto;
import com.finture.bpm.engine.rest.dto.history.batch.HistoricBatchDto;
import com.finture.bpm.engine.rest.dto.history.batch.HistoricBatchQueryDto;
import com.finture.bpm.engine.rest.dto.history.batch.removaltime.SetRemovalTimeToHistoricBatchesDto;
import com.finture.bpm.engine.rest.history.HistoricBatchRestService;
import com.finture.bpm.engine.rest.sub.history.HistoricBatchResource;
import com.finture.bpm.engine.rest.sub.history.impl.HistoricBatchResourceImpl;
import com.finture.bpm.engine.rest.util.QueryUtil;

public class HistoricBatchRestServiceImpl implements HistoricBatchRestService {

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricBatchRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  @Override
  public HistoricBatchResource getHistoricBatch(String batchId) {
    return new HistoricBatchResourceImpl(processEngine, batchId);
  }

  @Override
  public List<HistoricBatchDto> getHistoricBatches(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    HistoricBatchQueryDto queryDto = new HistoricBatchQueryDto(objectMapper, uriInfo.getQueryParameters());
    HistoricBatchQuery query = queryDto.toQuery(processEngine);

    List<HistoricBatch> matchingBatches = QueryUtil.list(query, firstResult, maxResults);

    List<HistoricBatchDto> batchResults = new ArrayList<HistoricBatchDto>();
    for (HistoricBatch matchingBatch : matchingBatches) {
      batchResults.add(HistoricBatchDto.fromBatch(matchingBatch));
    }
    return batchResults;
  }

  @Override
  public CountResultDto getHistoricBatchesCount(UriInfo uriInfo) {
    HistoricBatchQueryDto queryDto = new HistoricBatchQueryDto(objectMapper, uriInfo.getQueryParameters());
    HistoricBatchQuery query = queryDto.toQuery(processEngine);

    long count = query.count();
    return new CountResultDto(count);
  }

  @Override
  public List<CleanableHistoricBatchReportResultDto> getCleanableHistoricBatchesReport(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    CleanableHistoricBatchReportDto queryDto = new CleanableHistoricBatchReportDto(objectMapper, uriInfo.getQueryParameters());
    CleanableHistoricBatchReport query = queryDto.toQuery(processEngine);

    List<CleanableHistoricBatchReportResult> reportResult = QueryUtil.list(query, firstResult, maxResults);

    return CleanableHistoricBatchReportResultDto.convert(reportResult);
  }

  @Override
  public CountResultDto getCleanableHistoricBatchesReportCount(UriInfo uriInfo) {
    CleanableHistoricBatchReportDto queryDto = new CleanableHistoricBatchReportDto(objectMapper, uriInfo.getQueryParameters());
    queryDto.setObjectMapper(objectMapper);
    CleanableHistoricBatchReport query = queryDto.toQuery(processEngine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

  @Override
  public BatchDto setRemovalTimeAsync(SetRemovalTimeToHistoricBatchesDto dto) {
    HistoryService historyService = processEngine.getHistoryService();

    HistoricBatchQuery historicBatchQuery = null;

    if (dto.getHistoricBatchQuery() != null) {
      historicBatchQuery = dto.getHistoricBatchQuery().toQuery(processEngine);

    }

    SetRemovalTimeSelectModeForHistoricBatchesBuilder builder =
      historyService.setRemovalTimeToHistoricBatches();

    if (dto.isCalculatedRemovalTime()) {
      builder.calculatedRemovalTime();

    }

    Date removalTime = dto.getAbsoluteRemovalTime();
    if (dto.getAbsoluteRemovalTime() != null) {
      builder.absoluteRemovalTime(removalTime);

    }

    if (dto.isClearedRemovalTime()) {
      builder.clearedRemovalTime();

    }

    builder.byIds(dto.getHistoricBatchIds());
    builder.byQuery(historicBatchQuery);

    Batch batch = builder.executeAsync();
    return BatchDto.fromBatch(batch);
  }

}
