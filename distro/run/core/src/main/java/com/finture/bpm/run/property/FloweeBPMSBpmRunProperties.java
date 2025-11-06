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
package com.finture.bpm.run.property;

import com.finture.bpm.spring.boot.starter.property.FloweeBPMSBpmProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(FloweeBPMSBpmRunProperties.PREFIX)
public class FloweeBPMSBpmRunProperties {

  public static final String PREFIX = FloweeBPMSBpmProperties.PREFIX + ".run";

  @NestedConfigurationProperty
  protected FloweeBPMSBpmRunAuthenticationProperties auth = new FloweeBPMSBpmRunAuthenticationProperties();

  @NestedConfigurationProperty
  protected FloweeBPMSBpmRunCorsProperty cors = new FloweeBPMSBpmRunCorsProperty();

  @NestedConfigurationProperty
  protected FloweeBPMSBpmRunLdapProperties ldap = new FloweeBPMSBpmRunLdapProperties();

  @NestedConfigurationProperty
  protected List<FloweeBPMSBpmRunProcessEnginePluginProperty> processEnginePlugins = new ArrayList<>();

  @NestedConfigurationProperty
  protected FloweeBPMSBpmRunRestProperties rest = new FloweeBPMSBpmRunRestProperties();

  @NestedConfigurationProperty
  protected FloweeBPMSBpmRunDeploymentProperties deployment = new FloweeBPMSBpmRunDeploymentProperties();

  protected FloweeBPMSBpmRunAdministratorAuthorizationProperties adminAuth
      = new FloweeBPMSBpmRunAdministratorAuthorizationProperties();

  public FloweeBPMSBpmRunAuthenticationProperties getAuth() {
    return auth;
  }

  public void setAuth(FloweeBPMSBpmRunAuthenticationProperties auth) {
    this.auth = auth;
  }

  public FloweeBPMSBpmRunCorsProperty getCors() {
    return cors;
  }

  public void setCors(FloweeBPMSBpmRunCorsProperty cors) {
    this.cors = cors;
  }

  public FloweeBPMSBpmRunLdapProperties getLdap() {
    return ldap;
  }

  public void setLdap(FloweeBPMSBpmRunLdapProperties ldap) {
    this.ldap = ldap;
  }

  public FloweeBPMSBpmRunAdministratorAuthorizationProperties getAdminAuth() {
    return adminAuth;
  }

  public void setAdminAuth(FloweeBPMSBpmRunAdministratorAuthorizationProperties adminAuth) {
    this.adminAuth = adminAuth;
  }

  public List<FloweeBPMSBpmRunProcessEnginePluginProperty> getProcessEnginePlugins() {
    return processEnginePlugins;
  }

  public void setProcessEnginePlugins(List<FloweeBPMSBpmRunProcessEnginePluginProperty> processEnginePlugins) {
    this.processEnginePlugins = processEnginePlugins;
  }

  public FloweeBPMSBpmRunRestProperties getRest() {
    return rest;
  }

  public void setRest(FloweeBPMSBpmRunRestProperties rest) {
    this.rest = rest;
  }

  public FloweeBPMSBpmRunDeploymentProperties getDeployment() {
    return deployment;
  }

  public void setDeployment(FloweeBPMSBpmRunDeploymentProperties deployment) {
    this.deployment = deployment;
  }


  @Override
  public String toString() {
    return "FloweeBPMSBpmRunProperties [" +
        "auth=" + auth +
        ", cors=" + cors +
        ", ldap=" + ldap +
        ", adminAuth=" + adminAuth +
        ", plugins=" + processEnginePlugins +
        ", rest=" + rest +
        ", deployment=" + deployment +
        "]";
  }
}
