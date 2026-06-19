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

import com.finture.bpm.webapp.impl.util.ServletContextUtil;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.finture.bpm.webapp.impl.security.filter.util.CsrfConstants.CSRF_PATH_FIELD_NAME;
import static com.finture.bpm.webapp.impl.security.filter.util.CookieConstants.SET_COOKIE_HEADER_NAME;

public class CsrfPreventionFilterAppPathTest extends CsrfPreventionFilterTest {

  protected static final String MY_APP_PATH = "/my/application/path";

  protected ServletContext mockServletContext;

  public CsrfPreventionFilterAppPathTest(String nonModifyingRequestUrl,
                                         String modifyingRequestUrl,
                                         boolean isModifyingFetchRequest) {
    super(nonModifyingRequestUrl, modifyingRequestUrl, isModifyingFetchRequest);
  }

  @Override
  public void setup() throws Exception {
    mockServletContext = createTrackingServletContext();
    ServletContextUtil.setAppPath(MY_APP_PATH, mockServletContext);
    super.setup();
  }

  @Test
  public void shouldCheckNonModifyingRequestTokenGenerationWithRootContextPathAndEmptyAppPath()
    throws IOException, ServletException {
    // given
    ServletContext emptyPathContext = createTrackingServletContext();
    ServletContextUtil.setAppPath("", emptyPathContext);

    HttpSession session = createMockSession();
    HttpServletRequest nonModifyingRequest = createMockRequestWithServletContext(emptyPathContext);
    when(nonModifyingRequest.getMethod()).thenReturn("GET");
    when(nonModifyingRequest.getSession()).thenReturn(session);

    // set root context path in request
    when(nonModifyingRequest.getRequestURI()).thenReturn("/"  + nonModifyingRequestUrl);
    when(nonModifyingRequest.getContextPath()).thenReturn("");

    // when
    HttpServletResponse response = createMockResponse();
    applyFilter(nonModifyingRequest, response);

    // then
    String cookieToken = response.getHeader(SET_COOKIE_HEADER_NAME);
    String headerToken = response.getHeader(CSRF_HEADER_NAME);

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

    assertThat(cookieToken).isNotNull().isNotEmpty();
    assertThat(headerToken).isNotNull().isNotEmpty();

    String regex = CSRF_COOKIE_NAME + "=[A-Z0-9]{32}" + CSRF_PATH_FIELD_NAME + "/;SameSite=Lax";
    assertThat(cookieToken).matches(regex.replace(";", ";\\s*"));

    assertThat(cookieToken).contains(headerToken);
  }

  @Override
  protected String getCookiePath(String contextPath) {
    return super.getCookiePath(contextPath + MY_APP_PATH);
  }

  @Override
  protected HttpServletRequest getMockedRequest() {
    return createMockRequestWithServletContext(mockServletContext);
  }

  // helper methods ///////////////////////////////////////////////////////////////////////////////////////////////////

  private ServletContext createTrackingServletContext() {
    ServletContext ctx = mock(ServletContext.class);
    Map<String, Object> attrs = new HashMap<>();
    doAnswer(invocation -> {
      attrs.put(invocation.getArgument(0), invocation.getArgument(1));
      return null;
    }).when(ctx).setAttribute(anyString(), any());
    when(ctx.getAttribute(any())).thenAnswer(invocation -> attrs.get(invocation.getArgument(0)));
    return ctx;
  }

  private HttpServletRequest createMockRequestWithServletContext(ServletContext servletContext) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getServletContext()).thenReturn(servletContext);
    when(request.getServletPath()).thenReturn("");
    when(request.getPathInfo()).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    return request;
  }

}
