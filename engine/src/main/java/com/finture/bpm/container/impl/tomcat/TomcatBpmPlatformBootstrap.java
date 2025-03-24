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
package com.finture.bpm.container.impl.tomcat;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardServer;
import com.finture.bpm.container.RuntimeContainerDelegate;
import com.finture.bpm.container.impl.ContainerIntegrationLogger;
import com.finture.bpm.container.impl.RuntimeContainerDelegateImpl;
import com.finture.bpm.container.impl.deployment.DiscoverBpmPlatformPluginsStep;
import com.finture.bpm.container.impl.deployment.PlatformXmlStartProcessEnginesStep;
import com.finture.bpm.container.impl.deployment.StopProcessApplicationsStep;
import com.finture.bpm.container.impl.deployment.StopProcessEnginesStep;
import com.finture.bpm.container.impl.deployment.UnregisterBpmPlatformPluginsStep;
import com.finture.bpm.container.impl.deployment.jobexecutor.StartJobExecutorStep;
import com.finture.bpm.container.impl.deployment.jobexecutor.StartManagedThreadPoolStep;
import com.finture.bpm.container.impl.deployment.jobexecutor.StopJobExecutorStep;
import com.finture.bpm.container.impl.deployment.jobexecutor.StopManagedThreadPoolStep;
import com.finture.bpm.container.impl.tomcat.deployment.TomcatAttachments;
import com.finture.bpm.container.impl.tomcat.deployment.TomcatParseBpmPlatformXmlStep;
import com.finture.bpm.engine.ProcessEngine;
import com.finture.bpm.engine.impl.ProcessEngineLogger;

/**
 * <p>Apache Tomcat server listener responsible for deploying the Camunda Platform.</p>
 *
 * @author Daniel Meyer
 *
 */
public class TomcatBpmPlatformBootstrap implements LifecycleListener {

  private final static ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  protected ProcessEngine processEngine;

  protected RuntimeContainerDelegateImpl containerDelegate;

  public void lifecycleEvent(LifecycleEvent event) {

    if (Lifecycle.START_EVENT.equals(event.getType())) {

      // the Apache Tomcat integration uses the Jmx Container for managing process engines and applications.
      containerDelegate = (RuntimeContainerDelegateImpl) RuntimeContainerDelegate.INSTANCE.get();

      deployBpmPlatform(event);

    }
    else if (Lifecycle.STOP_EVENT.equals(event.getType())) {

      undeployBpmPlatform(event);

    }

  }

  protected void deployBpmPlatform(LifecycleEvent event) {

    final StandardServer server = (StandardServer) event.getSource();

    containerDelegate.getServiceContainer().createDeploymentOperation("deploy BPM platform")
      .addAttachment(TomcatAttachments.SERVER, server)
      .addStep(new TomcatParseBpmPlatformXmlStep())
      .addStep(new DiscoverBpmPlatformPluginsStep())
      .addStep(new StartManagedThreadPoolStep())
      .addStep(new StartJobExecutorStep())
      .addStep(new PlatformXmlStartProcessEnginesStep())
      .execute();

    LOG.camundaBpmPlatformSuccessfullyStarted(server.getServerInfo());

  }


  protected void undeployBpmPlatform(LifecycleEvent event) {

    final StandardServer server = (StandardServer) event.getSource();

    containerDelegate.getServiceContainer().createUndeploymentOperation("undeploy BPM platform")
      .addAttachment(TomcatAttachments.SERVER, server)
      .addStep(new StopJobExecutorStep())
      .addStep(new StopManagedThreadPoolStep())
      .addStep(new StopProcessApplicationsStep())
      .addStep(new StopProcessEnginesStep())
      .addStep(new UnregisterBpmPlatformPluginsStep())
      .execute();

    LOG.camundaBpmPlatformStopped(server.getServerInfo());
  }

}
