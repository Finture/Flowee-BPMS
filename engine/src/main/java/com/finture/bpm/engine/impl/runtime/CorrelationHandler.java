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
package com.finture.bpm.engine.impl.runtime;

import java.util.List;

import com.finture.bpm.engine.impl.interceptor.CommandContext;
import com.finture.bpm.engine.impl.runtime.CorrelationHandlerResult;
import com.finture.bpm.engine.impl.runtime.CorrelationSet;

/**
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 * @author Michael Scholz
 */
public interface CorrelationHandler {

  /**
   * Correlate the given message to either a waiting execution or a process
   * definition with a message start event.
   *
   * @param correlationSet
   *          any of its members may be <code>null</code>
   *
   * @return the matched correlation target or <code>null</code> if the message
   *         could not be correlated.
   */
  public com.finture.bpm.engine.impl.runtime.CorrelationHandlerResult correlateMessage(CommandContext commandContext, String messageName, CorrelationSet correlationSet);

  /**
   * Correlate the given message to all waiting executions and all process
   * definitions which have a message start event.
   *
   * @param correlationSet
   *          any of its members may be <code>null</code>
   *
   * @return all matched correlation targets or an empty List if the message
   *         could not be correlated.
   */
  public List<com.finture.bpm.engine.impl.runtime.CorrelationHandlerResult> correlateMessages(CommandContext commandContext, String messageName, CorrelationSet correlationSet);

  /**
   * Correlate the given message to process definitions with a message start
   * event.
   *
   * @param correlationSet
   *          any of its members may be <code>null</code>
   *
   * @return the matched correlation targets or an empty list if the message
   *         could not be correlated.
   */
  public List<CorrelationHandlerResult> correlateStartMessages(CommandContext commandContext, String messageName, CorrelationSet correlationSet);

}
