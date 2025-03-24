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
package com.finture.bpm.integrationtest.functional.spin;

import com.finture.bpm.application.ProcessApplicationContext;
import com.finture.bpm.engine.runtime.ProcessInstance;
import com.finture.bpm.engine.variable.Variables;
import com.finture.bpm.engine.variable.value.ObjectValue;
import com.finture.bpm.integrationtest.functional.spin.dataformat.Foo;
import com.finture.bpm.integrationtest.functional.spin.dataformat.FooDataFormat;
import com.finture.bpm.integrationtest.functional.spin.dataformat.FooDataFormatProvider;
import com.finture.bpm.integrationtest.functional.spin.dataformat.FooSpin;
import com.finture.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import com.finture.spin.spi.DataFormatProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.finture.bpm.engine.variable.Variables.serializedObjectValue;

/**
 * @author Thorben Lindhauer
 *
 */
@RunWith(Arquillian.class)
public class PaDataFormatProviderTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createDeployment() {
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "PaDataFormatTest.war")
        .addAsResource("META-INF/processes.xml")
        .addClass(AbstractFoxPlatformIntegrationTest.class)
        .addAsResource("com/finture/bpm/integrationtest/oneTaskProcess.bpmn")
        .addClass(Foo.class)
        .addClass(FooDataFormat.class)
        .addClass(FooDataFormatProvider.class)
        .addClass(FooSpin.class)
        .addAsServiceProvider(DataFormatProvider.class, FooDataFormatProvider.class)
        .addClass(ReferenceStoringProcessApplication.class);

    return webArchive;
  }

  /**
   * Tests that
   * 1) a serialized value can be set OUT OF process application context
   *   even if the data format is not available (using the fallback serializer)
   * 2) and that this value can be deserialized IN process application context
   *   by using the PA-local serializer
   */
  @Test
  public void customFormatCanBeUsedForVariableSerialization() {
    final ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess",
        Variables.createVariables()
          .putValue("serializedObject",
              serializedObjectValue("foo")
              .serializationDataFormat(FooDataFormat.NAME)
              .objectTypeName(Foo.class.getName())));

    ObjectValue objectValue = null;
    try {
      ProcessApplicationContext.setCurrentProcessApplication(ReferenceStoringProcessApplication.INSTANCE);
      objectValue = runtimeService.getVariableTyped(pi.getId(), "serializedObject", true);
    } finally {
      ProcessApplicationContext.clear();
    }

    Object value = objectValue.getValue();
    Assert.assertNotNull(value);
    Assert.assertTrue(value instanceof Foo);
  }

}
