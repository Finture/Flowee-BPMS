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
package com.finture.connect.httpclient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Map;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import com.finture.connect.ConnectorRequestException;
import com.finture.connect.httpclient.impl.HttpConnectorImpl;
import com.finture.connect.httpclient.impl.RequestConfigOption;
import com.finture.connect.httpclient.impl.util.ParseUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class HttpRequestConfigTest {

  public static final String EXAMPLE_URL = "http://camunda.org/example";

  public static final int PORT = 51234;

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(
          WireMockConfiguration.wireMockConfig().port(PORT));

  protected HttpConnector connector;

  @Before
  public void createConnector() {
    connector = new HttpConnectorImpl();
  }

  @Test
  public void shouldParseAuthenticationEnabled() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(RequestConfigOption.AUTHENTICATION_ENABLED.getName(), false);
    Map<String, Object> configOptions = request.getConfigOptions();

    RequestConfig.Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.isAuthenticationEnabled()).isFalse();
  }

  @Test
  public void shouldParseCircularRedirectsAllowed() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(RequestConfigOption.CIRCULAR_REDIRECTS_ALLOWED.getName(), true);
    Map<String, Object> configOptions = request.getConfigOptions();

    RequestConfig.Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.isCircularRedirectsAllowed()).isTrue();
  }

  @Test
  public void shouldParseConnectTimeout() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(RequestConfigOption.CONNECTION_TIMEOUT.getName(), Timeout.ofSeconds(10));
    Map<String, Object> configOptions = request.getConfigOptions();

    RequestConfig.Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getConnectTimeout()).isEqualTo(Timeout.ofSeconds(10));
  }

  @Test
  public void shouldParseConnectionRequestTimeout() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(RequestConfigOption.CONNECTION_REQUEST_TIMEOUT.getName(), Timeout.ofSeconds(10));
    Map<String, Object> configOptions = request.getConfigOptions();

    RequestConfig.Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getConnectionRequestTimeout()).isEqualTo(Timeout.ofSeconds(10));
  }

  @Test
  public void shouldParseContentCompressionEnabled() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(RequestConfigOption.CONTENT_COMPRESSION_ENABLED.getName(), false);
    Map<String, Object> configOptions = request.getConfigOptions();

    RequestConfig.Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.isContentCompressionEnabled()).isFalse();
  }

  @Test
  public void shouldParseCookieSpec() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(RequestConfigOption.COOKIE_SPEC.getName(), "test");
    Map<String, Object> configOptions = request.getConfigOptions();

    RequestConfig.Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getCookieSpec()).isEqualTo("test");
  }

  @Test
  public void shouldParseMaxRedirects() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(RequestConfigOption.MAX_REDIRECTS.getName(), -2);
    Map<String, Object> configOptions = request.getConfigOptions();

    RequestConfig.Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getMaxRedirects()).isEqualTo(-2);
  }

  @Test
  public void shouldParseProxy() {
    // given
    HttpHost testHost = new HttpHost("test");
    HttpRequest request = connector.createRequest()
        .configOption(RequestConfigOption.PROXY.getName(), testHost);
    Map<String, Object> configOptions = request.getConfigOptions();

    RequestConfig.Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getProxy()).isEqualTo(testHost);
  }

  @Test
  public void shouldParseProxyPreferredAuthSchemes() {
    // given
    ArrayList<String> testArray = new ArrayList<>();
    HttpRequest request = connector.createRequest()
        .configOption(RequestConfigOption.PROXY_PREFERRED_AUTH_SCHEMES.getName(), testArray);
    Map<String, Object> configOptions = request.getConfigOptions();

    RequestConfig.Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getProxyPreferredAuthSchemes()).isEqualTo(testArray);
  }

  @Test
  public void shouldParseRedirectsEnabled() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(RequestConfigOption.REDIRECTS_ENABLED.getName(), false);
    Map<String, Object> configOptions = request.getConfigOptions();

    RequestConfig.Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.isRedirectsEnabled()).isFalse();
  }

  @Test
  public void shouldParseTargetPreferredAuthSchemes() {
    // given
    ArrayList<String> testArray = new ArrayList<>();
    HttpRequest request = connector.createRequest()
        .configOption(RequestConfigOption.TARGET_PREFERRED_AUTH_SCHEMES.getName(), testArray);
    Map<String, Object> configOptions = request.getConfigOptions();

    RequestConfig.Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getTargetPreferredAuthSchemes()).isEqualTo(testArray);
  }

  @Test
  public void shouldParseConnectionKeepAlive() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(RequestConfigOption.CONNECTION_KEEP_ALIVE.getName(), Timeout.ofSeconds(10));
    Map<String, Object> configOptions = request.getConfigOptions();

    RequestConfig.Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getConnectionKeepAlive()).isEqualTo(Timeout.ofSeconds(10));
  }

  @Test
  public void shouldParseExpectContinueEnabled() {
    // given
    HttpRequest request = connector.createRequest()
            .configOption(RequestConfigOption.EXPECT_CONTINUE_ENABLED.getName(), true);
    Map<String, Object> configOptions = request.getConfigOptions();

    RequestConfig.Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.isExpectContinueEnabled()).isTrue();
  }

  @Test
  public void shouldParseHardCancellationEnabled() {
    // given
    HttpRequest request = connector.createRequest()
            .configOption(RequestConfigOption.HARD_CANCELLATION_ENABLED.getName(), true);
    Map<String, Object> configOptions = request.getConfigOptions();

    RequestConfig.Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.isHardCancellationEnabled()).isTrue();
  }

  @Test
  public void shouldParseResponseTimeout() {
    // given
    HttpRequest request = connector.createRequest()
            .configOption(RequestConfigOption.RESPONSE_TIMEOUT.getName(), Timeout.ofSeconds(10));
    Map<String, Object> configOptions = request.getConfigOptions();

    RequestConfig.Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getResponseTimeout()).isEqualTo(Timeout.ofSeconds(10));
  }

  @Test
  public void shouldNotChangeDefaultConfig() {
    // given
    HttpClient client;
    try {
      Field httpClientField = findField(connector.getClass(), "httpClient");
      httpClientField.setAccessible(true);
      client = (HttpClient) httpClientField.get(connector);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    connector.createRequest().url(EXAMPLE_URL).get()
        .configOption(RequestConfigOption.CONNECTION_TIMEOUT.getName(), Timeout.ofSeconds(10))
        .configOption(RequestConfigOption.CONNECTION_REQUEST_TIMEOUT.getName(), Timeout.ofSeconds(10))
        .configOption(RequestConfigOption.CONNECTION_KEEP_ALIVE.getName(), Timeout.ofSeconds(10))
        .configOption(RequestConfigOption.MAX_REDIRECTS.getName(), 0)
        .execute();

    // when
    RequestConfig config;
    try {
      Field defaultConfigField = findField(client.getClass(), "defaultConfig");
      defaultConfigField.setAccessible(true);
      config = (RequestConfig) defaultConfigField.get(client);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // then
    assertThat(config.getMaxRedirects()).isEqualTo(50);
    assertThat(config.getConnectTimeout()).isNull();
    assertThat(config.getConnectionRequestTimeout()).isEqualTo(Timeout.ofMinutes(3));
    assertThat(config.getConnectionKeepAlive()).isEqualTo(Timeout.ofMinutes(3));
  }

  /**
   * Walks up the class hierarchy to find a declared field by name.
   * This is more robust than {@link Class#getDeclaredField(String)} which only
   * looks at fields declared directly in the given class.
   */
  private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
    Class<?> current = clazz;
    while (current != null) {
      try {
        return current.getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        current = current.getSuperclass();
      }
    }
    throw new NoSuchFieldException(fieldName);
  }

  @Test
  public void shouldThrowTimeoutException() {
    try {
      // given
      wireMockRule.stubFor(get(urlEqualTo("/")).willReturn(aResponse().withFixedDelay(1000).withStatus(200)));

      // when
      connector.createRequest().url("http://localhost:" + PORT).get()
          .configOption(RequestConfigOption.RESPONSE_TIMEOUT.getName(), Timeout.ofMilliseconds(100))
          .execute();
      fail("No exception thrown");
    } catch (ConnectorRequestException e) {
      // then
      assertThat(e).hasMessageContaining("Unable to execute HTTP request");
      assertThat(e).hasCauseExactlyInstanceOf(SocketTimeoutException.class);
    }
  }

  @Test
  public void shouldThrowClassCastExceptionStringToTimeout() {
    try {
      // when
      connector.createRequest().url(EXAMPLE_URL).get()
          .configOption(RequestConfigOption.CONNECTION_TIMEOUT.getName(), "0")
          .execute();
      fail("No exception thrown");
    } catch (ConnectorRequestException e) {
      // then
      assertThat(e).hasMessageContaining("Invalid value for request configuration option: " + RequestConfigOption.CONNECTION_TIMEOUT.getName());
      assertThat(e).hasCauseInstanceOf(ClassCastException.class);
      assertThat(e.getCause()).hasMessageContaining("java.lang.String cannot be cast to class org.apache.hc.core5.util.Timeout");
    }
  }

  @Test
  public void shouldThrowClassCastExceptionStringToBoolean() {
    try {
      // when
      connector.createRequest().url(EXAMPLE_URL).get()
          .configOption(RequestConfigOption.AUTHENTICATION_ENABLED.getName(), "true")
          .execute();
      fail("No exception thrown");
    } catch (ConnectorRequestException e) {
      // then
      assertThat(e).hasMessageContaining("Invalid value for request configuration option: " + RequestConfigOption.AUTHENTICATION_ENABLED.getName());
      assertThat(e).hasCauseInstanceOf(ClassCastException.class);
      assertThat(e.getCause()).hasMessageContaining("java.lang.String cannot be cast to class java.lang.Boolean");
    }
  }

  @Test
  public void shouldThrowClassCastExceptionStringToHttpHost() {
    try {
      // when
      connector.createRequest().url(EXAMPLE_URL).get()
          .configOption(RequestConfigOption.PROXY.getName(), "proxy")
          .execute();
      fail("No exception thrown");
    } catch (ConnectorRequestException e) {
      // then
      assertThat(e).hasMessageContaining("Invalid value for request configuration option: " + RequestConfigOption.PROXY.getName());
      assertThat(e).hasCauseInstanceOf(ClassCastException.class);
      assertThat(e.getCause()).hasMessageContaining("java.lang.String cannot be cast to class org.apache.hc.core5.http.HttpHost");
    }
  }

  @Test
  public void shouldThrowClassCastExceptionIntToHttpHost() {
    try {
      // when
      connector.createRequest().url(EXAMPLE_URL).get()
          .configOption(RequestConfigOption.PROXY_PREFERRED_AUTH_SCHEMES.getName(), 0)
          .execute();
      fail("No exception thrown");
    } catch (ConnectorRequestException e) {
      // then
      assertThat(e).hasMessageContaining("Invalid value for request configuration option: " + RequestConfigOption.PROXY_PREFERRED_AUTH_SCHEMES.getName());
      assertThat(e).hasCauseInstanceOf(ClassCastException.class);
      assertThat(e.getCause()).hasMessageContaining("java.lang.Integer cannot be cast to class java.util.Collection");
    }
  }

}
