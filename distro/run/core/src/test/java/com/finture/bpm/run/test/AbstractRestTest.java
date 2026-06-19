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
package com.finture.bpm.run.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.finture.bpm.run.FloweeBPMSBpmRun;
import com.finture.bpm.run.test.util.LoggingInterceptor;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { FloweeBPMSBpmRun.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test-auth-disabled" })
public abstract class AbstractRestTest {

  public static String CONTEXT_PATH = "/engine-rest";

  protected RestTemplate testRestTemplate;

  @LocalServerPort
  protected int localPort;

  @Before
  public void enableRequestResponseLogging() {
    testRestTemplate = new RestTemplate();
    testRestTemplate.setUriTemplateHandler(
        new DefaultUriBuilderFactory("http://localhost:" + localPort));
    testRestTemplate.setInterceptors(Collections.singletonList(new LoggingInterceptor()));
    // Don't throw on 4xx responses so tests can assert on status codes directly
    testRestTemplate.setErrorHandler(new NoOpErrorHandler());
  }

  protected RestTemplate withBasicAuth(String username, String password) {
    RestTemplate authTemplate = new RestTemplate();
    authTemplate.setUriTemplateHandler(testRestTemplate.getUriTemplateHandler());
    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(testRestTemplate.getInterceptors());
    interceptors.add((request, body, execution) -> {
      request.getHeaders().setBasicAuth(username, password);
      return execution.execute(request, body);
    });
    authTemplate.setInterceptors(interceptors);
    authTemplate.setErrorHandler(new NoOpErrorHandler());
    return authTemplate;
  }

  /**
   * Error handler that never throws, allowing tests to assert on status codes directly.
   */
  private static class NoOpErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(org.springframework.http.client.ClientHttpResponse response) {
      return false;
    }

    @Override
    public void handleError(java.net.URI url, org.springframework.http.HttpMethod method,
        org.springframework.http.client.ClientHttpResponse response) {
      // no-op
    }
  }
}
