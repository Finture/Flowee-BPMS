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

import java.util.Arrays;
import java.util.List;

import com.finture.bpm.engine.HistoryService;
import com.finture.bpm.engine.batch.Batch;
import com.finture.bpm.engine.batch.history.HistoricBatchQuery;
import com.finture.bpm.engine.history.CleanableHistoricBatchReport;
import com.finture.bpm.engine.history.CleanableHistoricCaseInstanceReport;
import com.finture.bpm.engine.history.CleanableHistoricDecisionInstanceReport;
import com.finture.bpm.engine.history.CleanableHistoricProcessInstanceReport;
import com.finture.bpm.engine.history.HistoricActivityInstanceQuery;
import com.finture.bpm.engine.history.HistoricActivityStatisticsQuery;
import com.finture.bpm.engine.history.HistoricCaseActivityInstanceQuery;
import com.finture.bpm.engine.history.HistoricCaseActivityStatisticsQuery;
import com.finture.bpm.engine.history.HistoricCaseInstanceQuery;
import com.finture.bpm.engine.history.HistoricDecisionInstanceQuery;
import com.finture.bpm.engine.history.HistoricDecisionInstanceStatisticsQuery;
import com.finture.bpm.engine.history.HistoricDetailQuery;
import com.finture.bpm.engine.history.HistoricExternalTaskLogQuery;
import com.finture.bpm.engine.history.HistoricIncidentQuery;
import com.finture.bpm.engine.history.HistoricJobLogQuery;
import com.finture.bpm.engine.history.HistoricProcessInstanceQuery;
import com.finture.bpm.engine.history.HistoricProcessInstanceReport;
import com.finture.bpm.engine.history.HistoricTaskInstanceQuery;
import com.finture.bpm.engine.history.HistoricTaskInstanceReport;
import com.finture.bpm.engine.history.HistoricVariableInstanceQuery;
import com.finture.bpm.engine.history.NativeHistoricActivityInstanceQuery;
import com.finture.bpm.engine.history.NativeHistoricCaseActivityInstanceQuery;
import com.finture.bpm.engine.history.NativeHistoricCaseInstanceQuery;
import com.finture.bpm.engine.history.NativeHistoricDecisionInstanceQuery;
import com.finture.bpm.engine.history.NativeHistoricProcessInstanceQuery;
import com.finture.bpm.engine.history.NativeHistoricTaskInstanceQuery;
import com.finture.bpm.engine.history.NativeHistoricVariableInstanceQuery;
import com.finture.bpm.engine.history.SetRemovalTimeSelectModeForHistoricBatchesBuilder;
import com.finture.bpm.engine.history.SetRemovalTimeSelectModeForHistoricDecisionInstancesBuilder;
import com.finture.bpm.engine.history.SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder;
import com.finture.bpm.engine.history.SetRemovalTimeToHistoricBatchesBuilder;
import com.finture.bpm.engine.history.SetRemovalTimeToHistoricDecisionInstancesBuilder;
import com.finture.bpm.engine.history.UserOperationLogQuery;
import com.finture.bpm.engine.impl.*;
import com.finture.bpm.engine.impl.CleanableHistoricBatchReportImpl;
import com.finture.bpm.engine.impl.CleanableHistoricProcessInstanceReportImpl;
import com.finture.bpm.engine.impl.HistoricActivityInstanceQueryImpl;
import com.finture.bpm.engine.impl.HistoricActivityStatisticsQueryImpl;
import com.finture.bpm.engine.impl.HistoricCaseActivityStatisticsQueryImpl;
import com.finture.bpm.engine.impl.HistoricCaseInstanceQueryImpl;
import com.finture.bpm.engine.impl.HistoricDetailQueryImpl;
import com.finture.bpm.engine.impl.HistoricIdentityLinkLogQueryImpl;
import com.finture.bpm.engine.impl.HistoricIncidentQueryImpl;
import com.finture.bpm.engine.impl.HistoricProcessInstanceReportImpl;
import com.finture.bpm.engine.impl.HistoricTaskInstanceQueryImpl;
import com.finture.bpm.engine.impl.HistoricTaskInstanceReportImpl;
import com.finture.bpm.engine.impl.NativeHistoricActivityInstanceQueryImpl;
import com.finture.bpm.engine.impl.NativeHistoricCaseActivityInstanceQueryImpl;
import com.finture.bpm.engine.impl.NativeHistoricCaseInstanceQueryImpl;
import com.finture.bpm.engine.impl.NativeHistoricProcessInstanceQueryImpl;
import com.finture.bpm.engine.impl.NativeHistoricVariableInstanceQueryImpl;
import com.finture.bpm.engine.impl.NativeHistoryDecisionInstanceQueryImpl;
import com.finture.bpm.engine.impl.SetAnnotationForOperationLog;
import com.finture.bpm.engine.impl.UserOperationLogQueryImpl;
import com.finture.bpm.engine.impl.batch.history.DeleteHistoricBatchCmd;
import com.finture.bpm.engine.impl.batch.history.HistoricBatchQueryImpl;
import com.finture.bpm.engine.impl.cmd.DeleteHistoricCaseInstanceCmd;
import com.finture.bpm.engine.impl.cmd.DeleteHistoricCaseInstancesBulkCmd;
import com.finture.bpm.engine.impl.cmd.DeleteHistoricProcessInstancesCmd;
import com.finture.bpm.engine.impl.cmd.DeleteHistoricTaskInstanceCmd;
import com.finture.bpm.engine.impl.cmd.DeleteHistoricVariableInstanceCmd;
import com.finture.bpm.engine.impl.cmd.DeleteHistoricVariableInstancesByProcessInstanceIdCmd;
import com.finture.bpm.engine.impl.cmd.DeleteUserOperationLogEntryCmd;
import com.finture.bpm.engine.impl.cmd.FindHistoryCleanupJobsCmd;
import com.finture.bpm.engine.impl.cmd.GetHistoricExternalTaskLogErrorDetailsCmd;
import com.finture.bpm.engine.impl.cmd.GetHistoricJobLogExceptionStacktraceCmd;
import com.finture.bpm.engine.impl.cmd.HistoryCleanupCmd;
import com.finture.bpm.engine.impl.cmd.batch.DeleteHistoricProcessInstancesBatchCmd;
import com.finture.bpm.engine.impl.dmn.cmd.DeleteHistoricDecisionInstanceByDefinitionIdCmd;
import com.finture.bpm.engine.impl.dmn.cmd.DeleteHistoricDecisionInstanceByInstanceIdCmd;
import com.finture.bpm.engine.impl.dmn.cmd.DeleteHistoricDecisionInstancesBatchCmd;
import com.finture.bpm.engine.impl.dmn.cmd.DeleteHistoricDecisionInstancesBulkCmd;
import com.finture.bpm.engine.history.SetRemovalTimeToHistoricProcessInstancesBuilder;
import com.finture.bpm.engine.impl.history.SetRemovalTimeToHistoricBatchesBuilderImpl;
import com.finture.bpm.engine.impl.history.SetRemovalTimeToHistoricDecisionInstancesBuilderImpl;
import com.finture.bpm.engine.impl.history.SetRemovalTimeToHistoricProcessInstancesBuilderImpl;
import com.finture.bpm.engine.runtime.Job;

/**
 * @author Tom Baeyens
 * @author Bernd Ruecker (camunda)
 * @author Christian Stettler
 */
public class HistoryServiceImpl extends ServiceImpl implements HistoryService {

  public HistoricProcessInstanceQuery createHistoricProcessInstanceQuery() {
    return new HistoricProcessInstanceQueryImpl(commandExecutor);
  }

  public HistoricActivityInstanceQuery createHistoricActivityInstanceQuery() {
    return new HistoricActivityInstanceQueryImpl(commandExecutor);
  }

  public HistoricActivityStatisticsQuery createHistoricActivityStatisticsQuery(String processDefinitionId) {
    return new HistoricActivityStatisticsQueryImpl(processDefinitionId, commandExecutor);
  }

  public HistoricCaseActivityStatisticsQuery createHistoricCaseActivityStatisticsQuery(String caseDefinitionId) {
    return new HistoricCaseActivityStatisticsQueryImpl(caseDefinitionId, commandExecutor);
  }

  public HistoricTaskInstanceQuery createHistoricTaskInstanceQuery() {
    return new HistoricTaskInstanceQueryImpl(commandExecutor);
  }

  public HistoricDetailQuery createHistoricDetailQuery() {
    return new HistoricDetailQueryImpl(commandExecutor);
  }

  public UserOperationLogQuery createUserOperationLogQuery() {
    return new UserOperationLogQueryImpl(commandExecutor);
  }

  public HistoricVariableInstanceQuery createHistoricVariableInstanceQuery() {
    return new HistoricVariableInstanceQueryImpl(commandExecutor);
  }

  public HistoricIncidentQuery createHistoricIncidentQuery() {
    return new HistoricIncidentQueryImpl(commandExecutor);
  }

  public com.finture.bpm.engine.impl.HistoricIdentityLinkLogQueryImpl createHistoricIdentityLinkLogQuery() {
    return new HistoricIdentityLinkLogQueryImpl(commandExecutor);
  }

  public HistoricCaseInstanceQuery createHistoricCaseInstanceQuery() {
    return new HistoricCaseInstanceQueryImpl(commandExecutor);
  }

  public HistoricCaseActivityInstanceQuery createHistoricCaseActivityInstanceQuery() {
    return new HistoricCaseActivityInstanceQueryImpl(commandExecutor);
  }

  public HistoricDecisionInstanceQuery createHistoricDecisionInstanceQuery() {
    return new HistoricDecisionInstanceQueryImpl(commandExecutor);
  }

  public void deleteHistoricTaskInstance(String taskId) {
    commandExecutor.execute(new DeleteHistoricTaskInstanceCmd(taskId));
  }

  public void deleteHistoricProcessInstance(String processInstanceId) {
    deleteHistoricProcessInstances(Arrays.asList(processInstanceId));
  }

  public void deleteHistoricProcessInstanceIfExists(String processInstanceId) {
    deleteHistoricProcessInstancesIfExists(Arrays.asList(processInstanceId));
  }

  public void deleteHistoricProcessInstances(List<String> processInstanceIds) {
    commandExecutor.execute(new DeleteHistoricProcessInstancesCmd(processInstanceIds, true));
  }

  public void deleteHistoricProcessInstancesIfExists(List<String> processInstanceIds) {
    commandExecutor.execute(new DeleteHistoricProcessInstancesCmd(processInstanceIds, false));
  }

  public void deleteHistoricProcessInstancesBulk(List<String> processInstanceIds){
    deleteHistoricProcessInstances(processInstanceIds);
  }

  public Job cleanUpHistoryAsync() {
    return cleanUpHistoryAsync(false);
  }

  public Job cleanUpHistoryAsync(boolean immediatelyDue) {
    return commandExecutor.execute(new HistoryCleanupCmd(immediatelyDue));
  }

  @Override
  public Job findHistoryCleanupJob() {
    final List<Job> jobs = commandExecutor.execute(new FindHistoryCleanupJobsCmd());
    if (jobs.size() > 0) {
      return jobs.get(0);
    } else {
      return null;
    }
  }

  @Override
  public List<Job> findHistoryCleanupJobs() {
    return commandExecutor.execute(new FindHistoryCleanupJobsCmd());
  }

  public Batch deleteHistoricProcessInstancesAsync(List<String> processInstanceIds, String deleteReason) {
    return this.deleteHistoricProcessInstancesAsync(processInstanceIds,null,deleteReason);
  }

  public Batch deleteHistoricProcessInstancesAsync(HistoricProcessInstanceQuery query, String deleteReason) {
    return this.deleteHistoricProcessInstancesAsync(null,query,deleteReason);
  }

  public Batch deleteHistoricProcessInstancesAsync(List<String> processInstanceIds, HistoricProcessInstanceQuery query, String deleteReason){
    return commandExecutor.execute(new DeleteHistoricProcessInstancesBatchCmd(processInstanceIds, query, deleteReason));
  }

  public void deleteUserOperationLogEntry(String entryId) {
    commandExecutor.execute(new DeleteUserOperationLogEntryCmd(entryId));
  }

  public void deleteHistoricCaseInstance(String caseInstanceId) {
    commandExecutor.execute(new DeleteHistoricCaseInstanceCmd(caseInstanceId));
  }

  public void deleteHistoricCaseInstancesBulk(List<String> caseInstanceIds) {
    commandExecutor.execute(new DeleteHistoricCaseInstancesBulkCmd(caseInstanceIds));
  }

  public void deleteHistoricDecisionInstance(String decisionDefinitionId) {
    deleteHistoricDecisionInstanceByDefinitionId(decisionDefinitionId);
  }

  @Override
  public void deleteHistoricDecisionInstancesBulk(List<String> decisionInstanceIds) {
    commandExecutor.execute(new DeleteHistoricDecisionInstancesBulkCmd(decisionInstanceIds));
  }

  public void deleteHistoricDecisionInstanceByDefinitionId(String decisionDefinitionId) {
    commandExecutor.execute(new DeleteHistoricDecisionInstanceByDefinitionIdCmd(decisionDefinitionId));
  }

  public void deleteHistoricDecisionInstanceByInstanceId(String historicDecisionInstanceId){
    commandExecutor.execute(new DeleteHistoricDecisionInstanceByInstanceIdCmd(historicDecisionInstanceId));
  }

  public Batch deleteHistoricDecisionInstancesAsync(List<String> decisionInstanceIds, String deleteReason) {
    return deleteHistoricDecisionInstancesAsync(decisionInstanceIds, null, deleteReason);
  }

  public Batch deleteHistoricDecisionInstancesAsync(HistoricDecisionInstanceQuery query, String deleteReason) {
    return deleteHistoricDecisionInstancesAsync(null, query, deleteReason);
  }

  public Batch deleteHistoricDecisionInstancesAsync(List<String> decisionInstanceIds, HistoricDecisionInstanceQuery query, String deleteReason) {
    return commandExecutor.execute(new DeleteHistoricDecisionInstancesBatchCmd(decisionInstanceIds, query, deleteReason));
  }
  
  @Override
  public void deleteHistoricVariableInstance(String variableInstanceId) {
    commandExecutor.execute(new DeleteHistoricVariableInstanceCmd(variableInstanceId));
  }
  
  @Override
  public void deleteHistoricVariableInstancesByProcessInstanceId(String processInstanceId) {
    commandExecutor.execute(new DeleteHistoricVariableInstancesByProcessInstanceIdCmd(processInstanceId));
  }

  public NativeHistoricProcessInstanceQuery createNativeHistoricProcessInstanceQuery() {
    return new NativeHistoricProcessInstanceQueryImpl(commandExecutor);
  }

  public NativeHistoricTaskInstanceQuery createNativeHistoricTaskInstanceQuery() {
    return new NativeHistoricTaskInstanceQueryImpl(commandExecutor);
  }

  public NativeHistoricActivityInstanceQuery createNativeHistoricActivityInstanceQuery() {
    return new NativeHistoricActivityInstanceQueryImpl(commandExecutor);
  }

  public NativeHistoricCaseInstanceQuery createNativeHistoricCaseInstanceQuery() {
    return new NativeHistoricCaseInstanceQueryImpl(commandExecutor);
  }

  public NativeHistoricCaseActivityInstanceQuery createNativeHistoricCaseActivityInstanceQuery() {
    return new NativeHistoricCaseActivityInstanceQueryImpl(commandExecutor);
  }

  public NativeHistoricDecisionInstanceQuery createNativeHistoricDecisionInstanceQuery() {
    return new NativeHistoryDecisionInstanceQueryImpl(commandExecutor);
  }

  public NativeHistoricVariableInstanceQuery createNativeHistoricVariableInstanceQuery() {
    return new NativeHistoricVariableInstanceQueryImpl(commandExecutor);
  }

  public HistoricJobLogQuery createHistoricJobLogQuery() {
    return new HistoricJobLogQueryImpl(commandExecutor);
  }

  public String getHistoricJobLogExceptionStacktrace(String historicJobLogId) {
    return commandExecutor.execute(new GetHistoricJobLogExceptionStacktraceCmd(historicJobLogId));
  }

  public HistoricProcessInstanceReport createHistoricProcessInstanceReport() {
    return new HistoricProcessInstanceReportImpl(commandExecutor);
  }

  public HistoricTaskInstanceReport createHistoricTaskInstanceReport() {
    return new HistoricTaskInstanceReportImpl(commandExecutor);
  }

  public CleanableHistoricProcessInstanceReport createCleanableHistoricProcessInstanceReport() {
    return new CleanableHistoricProcessInstanceReportImpl(commandExecutor);
  }

  public CleanableHistoricDecisionInstanceReport createCleanableHistoricDecisionInstanceReport() {
    return new CleanableHistoricDecisionInstanceReportImpl(commandExecutor);
  }

  public CleanableHistoricCaseInstanceReport createCleanableHistoricCaseInstanceReport() {
    return new CleanableHistoricCaseInstanceReportImpl(commandExecutor);
  }

  public CleanableHistoricBatchReport createCleanableHistoricBatchReport() {
    return new CleanableHistoricBatchReportImpl(commandExecutor);
  }

  public HistoricBatchQuery createHistoricBatchQuery() {
    return new HistoricBatchQueryImpl(commandExecutor);
  }

  public void deleteHistoricBatch(String batchId) {
    commandExecutor.execute(new DeleteHistoricBatchCmd(batchId));
  }

  @Override
  public HistoricDecisionInstanceStatisticsQuery createHistoricDecisionInstanceStatisticsQuery(String decisionRequirementsDefinitionId) {
    return new HistoricDecisionInstanceStatisticsQueryImpl(decisionRequirementsDefinitionId, commandExecutor);
  }

  @Override
  public HistoricExternalTaskLogQuery createHistoricExternalTaskLogQuery() {
    return new HistoricExternalTaskLogQueryImpl(commandExecutor);
  }

  @Override
  public String getHistoricExternalTaskLogErrorDetails(String historicExternalTaskLogId) {
    return commandExecutor.execute(new GetHistoricExternalTaskLogErrorDetailsCmd(historicExternalTaskLogId));
  }

  public SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder setRemovalTimeToHistoricProcessInstances() {
    return new SetRemovalTimeToHistoricProcessInstancesBuilderImpl(commandExecutor);
  }

  public SetRemovalTimeSelectModeForHistoricDecisionInstancesBuilder setRemovalTimeToHistoricDecisionInstances() {
    return new SetRemovalTimeToHistoricDecisionInstancesBuilderImpl(commandExecutor);
  }

  public SetRemovalTimeSelectModeForHistoricBatchesBuilder setRemovalTimeToHistoricBatches() {
    return new SetRemovalTimeToHistoricBatchesBuilderImpl(commandExecutor);
  }

  public void setAnnotationForOperationLogById(String operationId, String annotation) {
    commandExecutor.execute(new com.finture.bpm.engine.impl.SetAnnotationForOperationLog(operationId, annotation));
  }

  public void clearAnnotationForOperationLogById(String operationId) {
    commandExecutor.execute(new SetAnnotationForOperationLog(operationId, null));
  }

}
