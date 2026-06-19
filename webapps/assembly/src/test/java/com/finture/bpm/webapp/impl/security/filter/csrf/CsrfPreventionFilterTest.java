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
package com.finture.bpm.webapp.impl.security.filter.csrf;

import com.finture.bpm.webapp.impl.security.filter.CsrfPreventionFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.finture.bpm.webapp.impl.security.filter.util.CsrfConstants.CSRF_PATH_FIELD_NAME;
import static com.finture.bpm.webapp.impl.security.filter.util.CookieConstants.SET_COOKIE_HEADER_NAME;

/**
 * @author Nikola Koevski
 */
@RunWith(Parameterized.class)
public class CsrfPreventionFilterTest {

  /**
   * Custom response type that adds getErrorMessage() for test assertions.
   * Spring's MockHttpServletResponse had this method; javax.servlet.http.HttpServletResponse does not.
   */
  abstract static class TestHttpServletResponse implements HttpServletResponse {
    abstract String getErrorMessage();
  }

  protected static final String SERVICE_PATH = "/camunda";
  protected static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
  protected static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
  protected static final String CSRF_HEADER_REQUIRED = "Required";

  protected Filter csrfPreventionFilter;

  protected String nonModifyingRequestUrl;
  protected String modifyingRequestUrl;

  // flags a modifying request (POST/PUT/DELETE) as a non-modifying one
  protected boolean isModifyingFetchRequest;

  @Parameterized.Parameters
  public static Collection<Object[]> getRequestUrls() {
    return Arrays.asList(new Object[][]{
      {"/app/cockpit/default/", "/api/admin/auth/user/default/login/cockpit", true},
      {"/app/cockpit/engine1/", "/api/admin/auth/user/engine1/login/cockpit", true},

      {"/app/cockpit/default/", "/api/engine/engine/default/history/task/count", false},
      {"/app/cockpit/engine1/", "/api/engine/engine/engine1/history/task/count", false},

      {"/app/tasklist/default/", "/api/admin/auth/user/default/login/tasklist", true},
      {"/app/tasklist/engine1/", "/api/admin/auth/user/engine1/login/tasklist", true},

      {"/app/tasklist/default/", "api/engine/engine/default/task/task-id/submit-form", false},
      {"/app/tasklist/engine2/", "api/engine/engine/engine2/task/task-id/submit-form", false},

      {"/app/admin/default/", "/api/admin/auth/user/default/login/admin", true},
      {"/app/admin/engine1/", "/api/admin/auth/user/engine1/login/admin", true},

      {"/app/admin/default/", "api/admin/setup/default/user/create", false},
      {"/app/admin/engine3/", "api/admin/setup/engine3/user/create", false},

      {"/app/welcome/default/", "/api/admin/auth/user/default/login/welcome", true},
      {"/app/welcome/engine1/", "/api/admin/auth/user/engine1/login/welcome", true}
    });
  }

  public CsrfPreventionFilterTest(String nonModifyingRequestUrl, String modifyingRequestUrl, boolean isModifyingFetchRequest) {
    this.nonModifyingRequestUrl = nonModifyingRequestUrl;
    this.modifyingRequestUrl = modifyingRequestUrl;
    this.isModifyingFetchRequest = isModifyingFetchRequest;
  }

  @Before
  public void setup() throws Exception {
    setupFilter();
  }

  protected void setupFilter() throws ServletException {
    FilterConfig config = mock(FilterConfig.class);
    when(config.getServletContext()).thenReturn(mock(ServletContext.class));
    csrfPreventionFilter = new CsrfPreventionFilter();
    csrfPreventionFilter.init(config);
  }

  protected void applyFilter(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    FilterChain filterChain = mock(FilterChain.class);
    csrfPreventionFilter.doFilter(request, response, filterChain);
  }

  @Test
  public void testNonModifyingRequestTokenGeneration() throws IOException, ServletException {
    HttpServletResponse response = performNonModifyingRequest(nonModifyingRequestUrl, createMockSession());

    String cookieToken = (String) response.getHeader(SET_COOKIE_HEADER_NAME);
    String headerToken = (String) response.getHeader(CSRF_HEADER_NAME);

    Assert.assertNotNull(cookieToken);
    Assert.assertNotNull(headerToken);

    String regex = CSRF_COOKIE_NAME + "=[A-Z0-9]{32}" + CSRF_PATH_FIELD_NAME + getCookiePath(SERVICE_PATH) + ";SameSite=Lax";
    assertThat(cookieToken).matches(regex.replace(";", ";\\s*"));

    Assert.assertEquals("No HTTP Header Token!",false, headerToken.isEmpty());
    assertThat(cookieToken).contains(headerToken);
  }

  @Test
  public void testNonModifyingRequestTokenGenerationWithRootContextPath() throws IOException, ServletException {
    // given
    HttpSession session = createMockSession();
    HttpServletRequest nonModifyingRequest = getMockedRequest();
    when(nonModifyingRequest.getMethod()).thenReturn("GET");
    when(nonModifyingRequest.getSession()).thenReturn(session);

    // set root context path in request
    when(nonModifyingRequest.getRequestURI()).thenReturn("/"  + nonModifyingRequestUrl);
    when(nonModifyingRequest.getContextPath()).thenReturn("");

    // when
    HttpServletResponse response = createMockResponse();
    applyFilter(nonModifyingRequest, response);

    // then
    String cookieToken = (String) response.getHeader(SET_COOKIE_HEADER_NAME);
    String headerToken = (String) response.getHeader(CSRF_HEADER_NAME);

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    Assert.assertNotNull(cookieToken);
    Assert.assertNotNull(headerToken);

    String regex = CSRF_COOKIE_NAME + "=[A-Z0-9]{32}" + CSRF_PATH_FIELD_NAME + getCookiePath("") + ";SameSite=Lax";
    assertThat(cookieToken).matches(regex.replace(";", ";\\s*"));

    Assert.assertEquals("No HTTP Header Token!",false, headerToken.isEmpty());
    assertThat(cookieToken).contains(headerToken);
  }

  @Test
  public void testConsecutiveNonModifyingRequestTokens() throws IOException, ServletException {
    HttpSession session = createMockSession();

    // first non-modifying request
    HttpServletResponse firstResponse = performNonModifyingRequest(nonModifyingRequestUrl, session);
    // second non-modifying request
    HttpServletResponse secondResponse = performNonModifyingRequest(nonModifyingRequestUrl, session);

    String headerToken1 = (String) firstResponse.getHeader(CSRF_HEADER_NAME);
    String headerToken2 = (String) secondResponse.getHeader(CSRF_HEADER_NAME);

    Assert.assertNotNull(headerToken1);
    Assert.assertNull(headerToken2);
  }

  @Test
  public void testModifyingRequestTokenValidation() throws IOException, ServletException {
    HttpSession session = createMockSession();

    // first a non-modifying request to obtain a token
    HttpServletResponse nonModifyingResponse = performNonModifyingRequest(nonModifyingRequestUrl, session);

    if (!isModifyingFetchRequest) {
      String token = (String) nonModifyingResponse.getHeader(CSRF_HEADER_NAME);
      TestHttpServletResponse modifyingResponse = performModifyingRequest(token, session);
      Assert.assertEquals(Response.Status.OK.getStatusCode(), modifyingResponse.getStatus());
    }
  }

  @Test
  public void testModifyingRequestInvalidToken() throws IOException, ServletException {
    HttpSession session = createMockSession("session-1");
    performNonModifyingRequest(nonModifyingRequestUrl, session);

    if (!isModifyingFetchRequest) {
      // invalid header token
      TestHttpServletResponse response = performModifyingRequest("invalid header token", session);
      Assert.assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
      Assert.assertEquals("CSRFPreventionFilter: Invalid HTTP Header Token.", response.getErrorMessage());

      // no token in header
      TestHttpServletResponse response2 = createMockResponse();
      HttpServletRequest modifyingRequest = getMockedRequest();
      when(modifyingRequest.getMethod()).thenReturn("POST");
      when(modifyingRequest.getSession()).thenReturn(session);
      when(modifyingRequest.getRequestURI()).thenReturn(SERVICE_PATH  + modifyingRequestUrl);
      when(modifyingRequest.getContextPath()).thenReturn(SERVICE_PATH);

      applyFilter(modifyingRequest, response2);
      Assert.assertEquals(CSRF_HEADER_REQUIRED, response2.getHeader(CSRF_HEADER_NAME));
      Assert.assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response2.getStatus());
      Assert.assertEquals("CSRFPreventionFilter: Token provided via HTTP Header is absent/empty.", response2.getErrorMessage());
    }
  }

  protected TestHttpServletResponse performNonModifyingRequest(String requestUrl, HttpSession session) throws IOException, ServletException {
    TestHttpServletResponse response = createMockResponse();

    HttpServletRequest nonModifyingRequest = getMockedRequest();
    when(nonModifyingRequest.getMethod()).thenReturn("GET");
    when(nonModifyingRequest.getSession()).thenReturn(session);
    when(nonModifyingRequest.getRequestURI()).thenReturn(SERVICE_PATH  + requestUrl);
    when(nonModifyingRequest.getContextPath()).thenReturn(SERVICE_PATH);

    applyFilter(nonModifyingRequest, response);

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    return response;
  }

  protected TestHttpServletResponse performModifyingRequest(String token, HttpSession session) throws IOException, ServletException {
    TestHttpServletResponse response = createMockResponse();

    HttpServletRequest modifyingRequest = getMockedRequest();

    when(modifyingRequest.getMethod()).thenReturn("POST");
    when(modifyingRequest.getSession()).thenReturn(session);
    when(modifyingRequest.getRequestURI()).thenReturn(SERVICE_PATH  + modifyingRequestUrl);
    when(modifyingRequest.getContextPath()).thenReturn(SERVICE_PATH);

    when(modifyingRequest.getHeader(CSRF_HEADER_NAME)).thenReturn(token);
    Cookie[] cookies = {new Cookie(CSRF_COOKIE_NAME, token)};
    when(modifyingRequest.getCookies()).thenReturn(cookies);

    applyFilter(modifyingRequest, response);

    return response;
  }

  protected HttpServletRequest getMockedRequest() {
    return createMockRequest();
  }

  protected String getCookiePath(String contextPath) {
    if (contextPath.isEmpty()) {
      return "/";

    } else {
      return contextPath;

    }
  }

  // mock helper methods ///////////////////////////////////////////////////////////////////////////////////////////////

  protected HttpSession createMockSession() {
    return createMockSession("test-session-id");
  }

  protected HttpSession createMockSession(String id) {
    HttpSession session = mock(HttpSession.class);
    Map<String, Object> attrs = new HashMap<>();
    when(session.getId()).thenReturn(id);
    when(session.getAttribute(any())).thenAnswer(invocation -> attrs.get(invocation.getArgument(0)));
    doAnswer(invocation -> {
      attrs.put(invocation.getArgument(0), invocation.getArgument(1));
      return null;
    }).when(session).setAttribute(anyString(), any());
    return session;
  }

  protected TestHttpServletResponse createMockResponse() {
    Map<String, String> headers = new HashMap<>();
    AtomicInteger status = new AtomicInteger(200);
    AtomicReference<String> error = new AtomicReference<>();

    TestHttpServletResponse response = mock(TestHttpServletResponse.class);
    doAnswer(invocation -> {
      headers.put(invocation.getArgument(0), invocation.getArgument(1));
      return null;
    }).when(response).setHeader(anyString(), anyString());
    doAnswer(invocation -> {
      headers.put(invocation.getArgument(0), invocation.getArgument(1));
      return null;
    }).when(response).addHeader(anyString(), anyString());
    doAnswer(invocation -> {
      status.set(invocation.getArgument(0));
      return null;
    }).when(response).setStatus(anyInt());
    stubSendError(response, status, error);
    when(response.getStatus()).thenAnswer(invocation -> status.get());
    when(response.getHeader(anyString())).thenAnswer(invocation -> headers.get(invocation.getArgument(0)));
    when(response.getErrorMessage()).thenAnswer(invocation -> error.get());
    return response;
  }

  private void stubSendError(TestHttpServletResponse response, AtomicInteger status, AtomicReference<String> error) {
    try {
      doAnswer(invocation -> {
        status.set(invocation.getArgument(0));
        error.set(invocation.getArgument(1));
        return null;
      }).when(response).sendError(anyInt(), anyString());
    } catch (IOException ignored) {
      // stubbing only — exception is not real
    }
  }

  protected HttpServletRequest createMockRequest() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getServletPath()).thenReturn("");
    when(request.getPathInfo()).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(request.getServletContext()).thenReturn(mock(ServletContext.class));
    return request;
  }

}
