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
package com.finture.bpm.engine.impl.migration.batch;

import com.finture.bpm.engine.batch.Batch;
import com.finture.bpm.engine.impl.batch.AbstractBatchJobHandler;
import com.finture.bpm.engine.impl.batch.BatchJobContext;
import com.finture.bpm.engine.impl.batch.BatchJobDeclaration;
import com.finture.bpm.engine.impl.context.Context;
import com.finture.bpm.engine.impl.core.variable.VariableUtil;
import com.finture.bpm.engine.impl.interceptor.CommandContext;
import com.finture.bpm.engine.impl.jobexecutor.JobDeclaration;
import com.finture.bpm.engine.impl.json.MigrationBatchConfigurationJsonConverter;
import com.finture.bpm.engine.impl.migration.MigrateProcessInstanceCmd;
import com.finture.bpm.engine.impl.migration.MigrationPlanExecutionBuilderImpl;
import com.finture.bpm.engine.impl.migration.MigrationPlanImpl;
import com.finture.bpm.engine.impl.persistence.entity.ExecutionEntity;
import com.finture.bpm.engine.impl.persistence.entity.JobEntity;
import com.finture.bpm.engine.impl.persistence.entity.MessageEntity;
import com.finture.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import com.finture.bpm.engine.migration.MigrationPlanExecutionBuilder;
import com.finture.bpm.engine.variable.impl.VariableMapImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Job handler for batch migration jobs. The batch migration job
 * migrates a list of process instances.
 */
public class MigrationBatchJobHandler extends AbstractBatchJobHandler<MigrationBatchConfiguration> {

  public static final BatchJobDeclaration JOB_DECLARATION = new BatchJobDeclaration(Batch.TYPE_PROCESS_INSTANCE_MIGRATION);

  public String getType() {
    return Batch.TYPE_PROCESS_INSTANCE_MIGRATION;
  }

  public JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration() {
    return JOB_DECLARATION;
  }

  protected MigrationBatchConfigurationJsonConverter getJsonConverterInstance() {
    return MigrationBatchConfigurationJsonConverter.INSTANCE;
  }

  @Override
  protected MigrationBatchConfiguration createJobConfiguration(MigrationBatchConfiguration configuration, List<String> processIdsForJob) {
    return new MigrationBatchConfiguration(
        processIdsForJob,
        configuration.getMigrationPlan(),
        configuration.isSkipCustomListeners(),
        configuration.isSkipIoMappings(),
        configuration.getBatchId()
    );
  }

  @Override
  protected void postProcessJob(MigrationBatchConfiguration configuration, JobEntity job, MigrationBatchConfiguration jobConfiguration) {
    if (job.getDeploymentId() == null) {
      CommandContext commandContext = Context.getCommandContext();
      String sourceProcessDefinitionId = configuration.getMigrationPlan().getSourceProcessDefinitionId();

      ProcessDefinitionEntity processDefinition = getProcessDefinition(commandContext, sourceProcessDefinitionId);
      job.setDeploymentId(processDefinition.getDeploymentId());
    }
  }

  @Override
  public void executeHandler(MigrationBatchConfiguration batchConfiguration,
                             ExecutionEntity execution,
                             CommandContext commandContext,
                             String tenantId) {

    MigrationPlanImpl migrationPlan = (MigrationPlanImpl) batchConfiguration.getMigrationPlan();

    String batchId = batchConfiguration.getBatchId();
    setVariables(batchId, migrationPlan, commandContext);

    MigrationPlanExecutionBuilder executionBuilder = commandContext.getProcessEngineConfiguration()
        .getRuntimeService()
        .newMigration(migrationPlan)
        .processInstanceIds(batchConfiguration.getIds());

    if (batchConfiguration.isSkipCustomListeners()) {
      executionBuilder.skipCustomListeners();
    }
    if (batchConfiguration.isSkipIoMappings()) {
      executionBuilder.skipIoMappings();
    }

    commandContext.executeWithOperationLogPrevented(
        new MigrateProcessInstanceCmd((MigrationPlanExecutionBuilderImpl)executionBuilder, true));
  }

  protected void setVariables(String batchId,
                              MigrationPlanImpl migrationPlan,
                              CommandContext commandContext) {
    Map<String, ?> variables = null;
    if (batchId != null) {
      variables = VariableUtil.findBatchVariablesSerialized(batchId, commandContext);
      if (variables != null) {
        migrationPlan.setVariables(new VariableMapImpl(new HashMap<>(variables)));
      }
    }
  }

  protected ProcessDefinitionEntity getProcessDefinition(CommandContext commandContext, String processDefinitionId) {
    return commandContext.getProcessEngineConfiguration()
        .getDeploymentCache()
        .findDeployedProcessDefinitionById(processDefinitionId);
  }

}
