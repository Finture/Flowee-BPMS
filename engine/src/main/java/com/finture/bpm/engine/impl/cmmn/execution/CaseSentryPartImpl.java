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
package com.finture.bpm.engine.impl.cmmn.execution;

import com.finture.bpm.engine.impl.cmmn.execution.CaseExecutionImpl;
import com.finture.bpm.engine.impl.cmmn.execution.CmmnExecution;
import com.finture.bpm.engine.impl.cmmn.execution.CmmnSentryPart;

/**
 * @author Roman Smirnov
 *
 */
public class CaseSentryPartImpl extends CmmnSentryPart {

  private static final long serialVersionUID = 1L;

  protected CaseExecutionImpl caseInstance;
  protected CaseExecutionImpl caseExecution;
  protected CaseExecutionImpl sourceCaseExecution;

  public CaseExecutionImpl getCaseInstance() {
    return caseInstance;
  }

  public void setCaseInstance(com.finture.bpm.engine.impl.cmmn.execution.CmmnExecution caseInstance) {
    this.caseInstance = (CaseExecutionImpl) caseInstance;
  }

  public com.finture.bpm.engine.impl.cmmn.execution.CmmnExecution getCaseExecution() {
    return caseExecution;
  }

  public void setCaseExecution(com.finture.bpm.engine.impl.cmmn.execution.CmmnExecution caseExecution) {
    this.caseExecution = (CaseExecutionImpl) caseExecution;
  }

  public com.finture.bpm.engine.impl.cmmn.execution.CmmnExecution getSourceCaseExecution() {
    return sourceCaseExecution;
  }

  public void setSourceCaseExecution(CmmnExecution sourceCaseExecution) {
    this.sourceCaseExecution = (CaseExecutionImpl) sourceCaseExecution;
  }

  public String getId() {
    return String.valueOf(System.identityHashCode(this));
  }

  public String getCaseInstanceId() {
    if (caseInstance != null) {
      return caseInstance.getId();
    }
    return null;
  }

  public String getCaseExecutionId() {
    if (caseExecution != null) {
      return caseExecution.getId();
    }
    return null;
  }

  public String getSourceCaseExecutionId() {
    if (sourceCaseExecution != null) {
      return sourceCaseExecution.getId();
    }
    return null;
  }

}
