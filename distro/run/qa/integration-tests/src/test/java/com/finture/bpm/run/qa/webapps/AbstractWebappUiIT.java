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
package com.finture.bpm.run.qa.webapps;

import com.finture.bpm.util.SeleniumScreenshotRule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * NOTE: copied from
 * <a href="https://github.com/camunda/camunda-bpm-platform/blob/master/qa/integration-tests-webapps/integration-tests/src/test/java/com/finture/bpm/AbstractWebappUiIntegrationTest.java">platform</a>,
 * might be removed with https://jira.camunda.com/browse/CAM-11379
 */
public class AbstractWebappUiIT extends AbstractWebIT {

  protected static WebDriver driver;

  @Rule
  public SeleniumScreenshotRule screenshotRule = new SeleniumScreenshotRule(driver);

  @BeforeClass
  public static void createDriver() {
    ChromeOptions chromeOptions = new ChromeOptions()
        .addArguments("--headless=new")
        .addArguments("--window-size=1920,1200")
        .addArguments("--remote-allow-origins=*")
        .addArguments("--disable-dev-shm-usage");

    // the chromedriver matching the installed Chrome is resolved by Selenium Manager
    driver = new ChromeDriver(chromeOptions);
  }

  public static ExpectedCondition<Boolean> currentURIIs(final URI pageURI) {

    return new ExpectedCondition<Boolean>() {
      @Override
      public Boolean apply(WebDriver webDriver) {
        try {
          return new URI(webDriver.getCurrentUrl()).equals(pageURI);
        } catch (URISyntaxException e) {
          return false;
        }
      }
    };

  }

  public static ExpectedCondition<Boolean> containsCurrentUrl(final String url) {

    return new ExpectedCondition<Boolean>() {
      @Override
      public Boolean apply(WebDriver webDriver) {
        return webDriver.getCurrentUrl().contains(url);
      }
    };

  }

  @Before
  public void createClient() throws Exception {
    preventRaceConditions();
    createClient(getWebappCtxPath());
    appUrl = testProperties.getApplicationPath("/" + getWebappCtxPath());
  }

  @AfterClass
  public static void quitDriver() {
    driver.quit();
  }

}
