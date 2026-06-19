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
package com.finture.bpm.webapp.impl.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.finture.bpm.engine.AuthorizationService;
import com.finture.bpm.engine.IdentityService;
import com.finture.bpm.engine.ProcessEngine;
import com.finture.bpm.engine.ProcessEngineConfiguration;
import com.finture.bpm.engine.authorization.Authorization;
import com.finture.bpm.engine.authorization.Permissions;
import com.finture.bpm.engine.authorization.Resources;
import com.finture.bpm.engine.identity.User;
import com.finture.bpm.engine.impl.util.ClockUtil;
import com.finture.bpm.engine.test.ProcessEngineRule;
import com.finture.bpm.webapp.impl.util.ServletContextUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Thorben Lindhauer
 *
 */
public class UserAuthenticationResourceTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule("camunda-test-engine.cfg.xml");

  protected ProcessEngine processEngine;
  protected ProcessEngineConfiguration processEngineConfiguration;
  protected IdentityService identityService;
  protected AuthorizationService authorizationService;

  @Before
  public void setUp() {
    this.processEngine = processEngineRule.getProcessEngine();
    this.processEngineConfiguration = processEngine.getProcessEngineConfiguration();
    this.identityService = processEngine.getIdentityService();
    this.authorizationService = processEngine.getAuthorizationService();
  }

  @After
  public void tearDown() {
    ClockUtil.reset();
    processEngineConfiguration.setAuthorizationEnabled(false);

    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }

    clearAuthentication();
  }

  private HttpSession createMockSession() {
    HttpSession session = mock(HttpSession.class);
    Map<String, Object> attrs = new HashMap<>();
    when(session.getAttribute(any())).thenAnswer(invocation -> attrs.get(invocation.getArgument(0)));
    doAnswer(invocation -> {
      attrs.put(invocation.getArgument(0), invocation.getArgument(1));
      return null;
    }).when(session).setAttribute(anyString(), any());
    return session;
  }

  private ServletContext createTrackingServletContext() {
    ServletContext ctx = mock(ServletContext.class);
    Map<String, Object> attrs = new HashMap<>();
    when(ctx.getAttribute(any())).thenAnswer(invocation -> attrs.get(invocation.getArgument(0)));
    doAnswer(invocation -> {
      attrs.put(invocation.getArgument(0), invocation.getArgument(1));
      return null;
    }).when(ctx).setAttribute(anyString(), any());
    return ctx;
  }

  private HttpServletRequest createMockRequest() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = createMockSession();
    ServletContext servletContext = createTrackingServletContext();
    when(request.getSession()).thenReturn(session);
    when(request.getSession(true)).thenReturn(session);
    when(request.getSession(false)).thenReturn(session);
    when(request.getServletContext()).thenReturn(servletContext);
    return request;
  }

  private HttpServletRequest createSessionTrackingRequest() {
    HttpSession[] sessions = new HttpSession[]{
        createMockSession(),
        createMockSession(),
        createMockSession()
    };
    for (int i = 0; i < sessions.length; i++) {
      when(sessions[i].getId()).thenReturn("session-" + (i + 1));
    }

    int[] sessionIdx = {0};
    boolean[] invalidated = {false};

    for (HttpSession s : sessions) {
      doAnswer(inv -> {
        invalidated[0] = true;
        return null;
      }).when(s).invalidate();
    }

    HttpServletRequest request = mock(HttpServletRequest.class);
    ServletContext trackingCtx = createTrackingServletContext();
    when(request.getServletContext()).thenReturn(trackingCtx);

    when(request.getSession()).thenAnswer(inv -> {
      if (invalidated[0]) {
        invalidated[0] = false;
        sessionIdx[0]++;
      }
      return sessions[Math.min(sessionIdx[0], sessions.length - 1)];
    });

    when(request.getSession(true)).thenAnswer(inv -> {
      if (invalidated[0]) {
        invalidated[0] = false;
        sessionIdx[0]++;
      }
      return sessions[Math.min(sessionIdx[0], sessions.length - 1)];
    });

    when(request.getSession(false)).thenAnswer(inv -> {
      if (invalidated[0]) {
        invalidated[0] = false;
        sessionIdx[0]++;
      }
      return sessions[Math.min(sessionIdx[0], sessions.length - 1)];
    });

    return request;
  }

  @Test
  public void testAuthorizationCheckGranted() {
    // given
    User jonny = identityService.newUser("jonny");
    jonny.setPassword("jonnyspassword");
    identityService.saveUser(jonny);

    Authorization authorization = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    authorization.setResource(Resources.APPLICATION);
    authorization.setResourceId("tasklist");
    authorization.setPermissions(new Permissions[] {Permissions.ACCESS});
    authorization.setUserId(jonny.getId());
    authorizationService.saveAuthorization(authorization);

    processEngineConfiguration.setAuthorizationEnabled(true);
    setAuthentication("jonny", "webapps-test-engine");

    // when
    UserAuthenticationResource authResource = new UserAuthenticationResource();
    authResource.request = createMockRequest();
    Response response = authResource.doLogin("webapps-test-engine", "tasklist", "jonny", "jonnyspassword");

    // then
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void testSessionRevalidationOnAuthorization() {
    // given
    User jonny = identityService.newUser("jonny");
    jonny.setPassword("jonnyspassword");
    identityService.saveUser(jonny);

    Authorization authorization = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    authorization.setResource(Resources.APPLICATION);
    authorization.setResourceId("tasklist");
    authorization.setPermissions(new Permissions[] {Permissions.ACCESS});
    authorization.setUserId(jonny.getId());
    authorizationService.saveAuthorization(authorization);

    processEngineConfiguration.setAuthorizationEnabled(true);
    setAuthentication("jonny", "webapps-test-engine");

    // when
    UserAuthenticationResource authResource = new UserAuthenticationResource();
    authResource.request = createSessionTrackingRequest();
    String oldSessionId = authResource.request.getSession().getId();

    // first login session
    Response response = authResource.doLogin("webapps-test-engine", "tasklist", "jonny", "jonnyspassword");
    String newSessionId = authResource.request.getSession().getId();

    authResource.doLogout("webapps-test-engine");

    // second login session
    response = authResource.doLogin("webapps-test-engine", "tasklist", "jonny", "jonnyspassword");
    String newestSessionId = authResource.request.getSession().getId();

    // then
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
    Assert.assertNotEquals(oldSessionId, newSessionId);
    Assert.assertNotEquals(newSessionId, newestSessionId);
  }

  @Test
  public void testAuthorizationCheckNotGranted() {
    // given
    User jonny = identityService.newUser("jonny");
    jonny.setPassword("jonnyspassword");
    identityService.saveUser(jonny);

    processEngineConfiguration.setAuthorizationEnabled(true);
    setAuthentication("jonny", "webapps-test-engine");

    // when
    UserAuthenticationResource authResource = new UserAuthenticationResource();
    authResource.request = createMockRequest();
    Response response = authResource.doLogin("webapps-test-engine", "tasklist", "jonny", "jonnyspassword");

    // then
    Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
  }

  @Test
  public void testAuthorizationCheckDeactivated() {
    // given
    User jonny = identityService.newUser("jonny");
    jonny.setPassword("jonnyspassword");
    identityService.saveUser(jonny);

    processEngineConfiguration.setAuthorizationEnabled(false);
    setAuthentication("jonny", "webapps-test-engine");

    // when
    UserAuthenticationResource authResource = new UserAuthenticationResource();
    authResource.request = createMockRequest();
    Response response = authResource.doLogin("webapps-test-engine", "tasklist", "jonny", "jonnyspassword");

    // then
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldSetAuthCacheValidationTime() {
    // given
    ClockUtil.setCurrentTime(ClockUtil.getCurrentTime());
    User jonny = identityService.newUser("jonny");
    jonny.setPassword("jonnyspassword");
    identityService.saveUser(jonny);

    HttpServletRequest request = createMockRequest();
    ServletContextUtil.setCacheTTLForLogin(1000 * 60 * 5, request.getServletContext());

    // when
    UserAuthenticationResource authResource = new UserAuthenticationResource();
    authResource.request = request;
    authResource.doLogin("webapps-test-engine", "tasklist", "jonny", "jonnyspassword");

    // then
    UserAuthentication userAuthentication = AuthenticationUtil.getAuthsFromSession(request.getSession())
      .getAuthentications()
      .get(0);
    assertThat(userAuthentication.getCacheValidationTime())
      .isEqualTo(new Date(ClockUtil.getCurrentTime().getTime() + 1000 * 60 * 5));
  }

  @Test
  public void shouldReturnUnauthorizedOnNullAuthentication() {
    // given
    User jonny = identityService.newUser("jonny");
    jonny.setPassword("jonnyspassword");
    identityService.saveUser(jonny);
    UserAuthenticationResource authResource = new UserAuthenticationResource();
    authResource.request = createMockRequest();

    try (MockedStatic<AuthenticationUtil> authenticationUtilMock = mockStatic(AuthenticationUtil.class)) {
      authenticationUtilMock.when(() -> AuthenticationUtil.createAuthentication("webapps-test-engine", "jonny")).thenReturn(null);

      // when
      Response response = authResource.doLogin("webapps-test-engine", "tasklist", "jonny", "jonnyspassword");

      // then
      Assert.assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }
  }

  protected void setAuthentication(String user, String engineName) {
    Authentications authentications = new Authentications();
    authentications.addOrReplace(new UserAuthentication(user, engineName));
    Authentications.setCurrent(authentications);
  }

  protected void clearAuthentication() {
    Authentications.clearCurrent();
  }


}
