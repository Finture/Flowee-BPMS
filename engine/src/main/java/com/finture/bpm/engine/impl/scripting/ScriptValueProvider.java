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
package com.finture.bpm.engine.impl.scripting;

import com.finture.bpm.engine.ProcessEngineException;
import com.finture.bpm.engine.delegate.VariableScope;
import com.finture.bpm.engine.impl.context.Context;
import com.finture.bpm.engine.impl.core.variable.mapping.IoParameter;
import com.finture.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import com.finture.bpm.engine.impl.delegate.ScriptInvocation;
import com.finture.bpm.engine.impl.scripting.ExecutableScript;

/**
 * Makes it possible to use scripts in {@link IoParameter} mappings.
 *
 * @author Daniel Meyer
 *
 */
public class ScriptValueProvider implements ParameterValueProvider {

  protected com.finture.bpm.engine.impl.scripting.ExecutableScript script;

  public ScriptValueProvider(com.finture.bpm.engine.impl.scripting.ExecutableScript script) {
    this.script = script;
  }

  public Object getValue(VariableScope variableScope) {
    ScriptInvocation invocation = new ScriptInvocation(script, variableScope);
    try {
      Context
      .getProcessEngineConfiguration()
      .getDelegateInterceptor()
      .handleInvocation(invocation);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new ProcessEngineException(e);
    }

    return invocation.getInvocationResult();
  }

  @Override
  public boolean isDynamic() {
    return true;
  }

  public com.finture.bpm.engine.impl.scripting.ExecutableScript getScript() {
    return script;
  }

  public void setScript(ExecutableScript script) {
    this.script = script;
  }

}
