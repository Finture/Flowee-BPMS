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
package com.finture.bpm.engine.test.api.identity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.finture.bpm.engine.authorization.Authorization.ANY;
import static com.finture.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static com.finture.bpm.engine.authorization.Permissions.READ;
import static com.finture.bpm.engine.authorization.Resources.USER;

import java.util.Collections;

import com.finture.bpm.engine.AuthorizationException;
import com.finture.bpm.engine.AuthorizationService;
import com.finture.bpm.engine.IdentityService;
import com.finture.bpm.engine.authorization.Authorization;
import com.finture.bpm.engine.identity.Group;
import com.finture.bpm.engine.identity.User;
import com.finture.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import com.finture.bpm.engine.test.util.ProcessEngineBootstrapRule;
import com.finture.bpm.engine.test.util.ProcessEngineTestRule;
import com.finture.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class AdminGroupsTest {

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule();

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected IdentityService identityService;
  protected AuthorizationService authorizationService;

  @Before
  public void init() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    identityService = engineRule.getIdentityService();
    authorizationService = engineRule.getAuthorizationService();

  }

  @After
  public void tearDown() throws Exception {
    processEngineConfiguration.setAuthorizationEnabled(false);
    cleanupAfterTest();
  }

  protected void cleanupAfterTest() {
    for (Group group : identityService.createGroupQuery().list()) {
      identityService.deleteGroup(group.getId());
    }
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
  }

  @Test
  public void testWithoutAdminGroup() {
    processEngineConfiguration.setAuthorizationEnabled(false);
    identityService.newUser("jonny1");

    // no admin group
    identityService.setAuthentication("nonAdmin", null, null);
    processEngineConfiguration.setAuthorizationEnabled(true);

    // when/then
    assertThatThrownBy(() -> identityService.unlockUser("jonny1"))
      .isInstanceOf(AuthorizationException.class)
      .hasMessageContaining("Required admin authenticated group or user.");
  }

  @Test
  public void testWithAdminGroup() {
    processEngineConfiguration.getAdminGroups().add("adminGroup");

    processEngineConfiguration.setAuthorizationEnabled(false);

    identityService.setAuthentication("admin", Collections.singletonList("adminGroup"), null);
    Authorization userAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    userAuth.setUserId("admin");
    userAuth.setResource(USER);
    userAuth.setResourceId(ANY);
    userAuth.addPermission(READ);
    authorizationService.saveAuthorization(userAuth);
    processEngineConfiguration.setAuthorizationEnabled(true);

    // when
    identityService.unlockUser("jonny1");

    // then no exception
  }
}
