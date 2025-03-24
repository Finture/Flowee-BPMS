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
package com.finture.bpm.qa.upgrade.timestamp;

import com.finture.bpm.engine.IdentityService;
import com.finture.bpm.engine.ProcessEngine;
import com.finture.bpm.engine.identity.User;
import com.finture.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import com.finture.bpm.engine.impl.context.Context;
import com.finture.bpm.engine.impl.interceptor.Command;
import com.finture.bpm.engine.impl.interceptor.CommandContext;
import com.finture.bpm.engine.impl.persistence.entity.IdentityInfoManager;
import com.finture.bpm.engine.impl.persistence.entity.UserEntity;
import com.finture.bpm.qa.upgrade.DescribesScenario;
import com.finture.bpm.qa.upgrade.ScenarioSetup;
import com.finture.bpm.qa.upgrade.Times;

/**
 * @author Nikola Koevski
 */
public class UserLockExpTimeScenario extends AbstractTimestampMigrationScenario {

  protected static final String USER_ID = "lockExpTimeTestUser";
  protected static final String PASSWORD = "testPassword";

  @DescribesScenario("initUserLockExpirationTime")
  @Times(1)
  public static ScenarioSetup initUserLockExpirationTime() {
    return new ScenarioSetup() {
      @Override
      public void execute(ProcessEngine processEngine, String s) {

        final IdentityService identityService = processEngine.getIdentityService();

        User user = identityService.newUser(USER_ID);
        user.setPassword(PASSWORD);
        identityService.saveUser(user);

        ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).getCommandExecutorTxRequired().execute(new Command<Void>() {
          @Override
          public Void execute(CommandContext context) {
            IdentityInfoManager identityInfoManager = Context.getCommandContext()
              .getSession(IdentityInfoManager.class);

            UserEntity userEntity = (UserEntity) identityService.createUserQuery()
              .userId(USER_ID)
              .singleResult();

            identityInfoManager.updateUserLock(userEntity, 10, TIMESTAMP);
            return null;
          }
        });
      }
    };
  }
}