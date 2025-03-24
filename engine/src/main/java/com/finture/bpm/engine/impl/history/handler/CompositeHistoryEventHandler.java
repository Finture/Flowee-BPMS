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
package com.finture.bpm.engine.impl.history.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.finture.bpm.engine.impl.history.event.HistoryEvent;
import com.finture.bpm.engine.impl.history.handler.HistoryEventHandler;
import com.finture.bpm.engine.impl.util.EnsureUtil;

/**
 * A {@link com.finture.bpm.engine.impl.history.handler.HistoryEventHandler} implementation which delegates to a list of
 * {@link com.finture.bpm.engine.impl.history.handler.HistoryEventHandler}.
 * 
 * @author Alexander Tyatenkov
 * 
 */
public class CompositeHistoryEventHandler implements com.finture.bpm.engine.impl.history.handler.HistoryEventHandler {

  /**
   * The list of {@link com.finture.bpm.engine.impl.history.handler.HistoryEventHandler} which consume the event.
   */
  protected final List<com.finture.bpm.engine.impl.history.handler.HistoryEventHandler> historyEventHandlers = new ArrayList<>();

  /**
   * Non-argument constructor for default initialization.
   */
  public CompositeHistoryEventHandler() {
  }

  /**
   * Constructor that takes a varargs parameter {@link com.finture.bpm.engine.impl.history.handler.HistoryEventHandler} that
   * consume the event.
   * 
   * @param historyEventHandlers
   *          the list of {@link com.finture.bpm.engine.impl.history.handler.HistoryEventHandler} that consume the event.
   */
  public CompositeHistoryEventHandler(final com.finture.bpm.engine.impl.history.handler.HistoryEventHandler... historyEventHandlers) {
    initializeHistoryEventHandlers(Arrays.asList(historyEventHandlers));
  }

  /**
   * Constructor that takes a list of {@link com.finture.bpm.engine.impl.history.handler.HistoryEventHandler} that consume
   * the event.
   * 
   * @param historyEventHandlers
   *          the list of {@link com.finture.bpm.engine.impl.history.handler.HistoryEventHandler} that consume the event.
   */
  public CompositeHistoryEventHandler(final List<com.finture.bpm.engine.impl.history.handler.HistoryEventHandler> historyEventHandlers) {
    initializeHistoryEventHandlers(historyEventHandlers);
  }

  /**
   * Initialize {@link #historyEventHandlers} with data transfered from constructor
   * 
   * @param historyEventHandlers
   */
  private void initializeHistoryEventHandlers(final List<com.finture.bpm.engine.impl.history.handler.HistoryEventHandler> historyEventHandlers) {
    EnsureUtil.ensureNotNull("History event handler", historyEventHandlers);
    for (com.finture.bpm.engine.impl.history.handler.HistoryEventHandler historyEventHandler : historyEventHandlers) {
      EnsureUtil.ensureNotNull("History event handler", historyEventHandler);
      this.historyEventHandlers.add(historyEventHandler);
    }
  }

  /**
   * Adds the {@link com.finture.bpm.engine.impl.history.handler.HistoryEventHandler} to the list of
   * {@link com.finture.bpm.engine.impl.history.handler.HistoryEventHandler} that consume the event.
   * 
   * @param historyEventHandler
   *          the {@link com.finture.bpm.engine.impl.history.handler.HistoryEventHandler} that consume the event.
   */
  public void add(final com.finture.bpm.engine.impl.history.handler.HistoryEventHandler historyEventHandler) {
    EnsureUtil.ensureNotNull("History event handler", historyEventHandler);
    historyEventHandlers.add(historyEventHandler);
  }

  @Override
  public void handleEvent(final HistoryEvent historyEvent) {
    for (HistoryEventHandler historyEventHandler : historyEventHandlers) {
      historyEventHandler.handleEvent(historyEvent);
    }
  }

  @Override
  public void handleEvents(final List<HistoryEvent> historyEvents) {
    for (HistoryEvent historyEvent : historyEvents) {
      handleEvent(historyEvent);
    }
  }

}
