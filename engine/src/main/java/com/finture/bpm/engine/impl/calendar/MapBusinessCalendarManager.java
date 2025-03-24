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
package com.finture.bpm.engine.impl.calendar;

import com.finture.bpm.engine.impl.calendar.BusinessCalendar;
import com.finture.bpm.engine.impl.calendar.BusinessCalendarManager;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Tom Baeyens
 */
public class MapBusinessCalendarManager implements com.finture.bpm.engine.impl.calendar.BusinessCalendarManager {
  
  private Map<String, com.finture.bpm.engine.impl.calendar.BusinessCalendar> businessCalendars = new HashMap<String, com.finture.bpm.engine.impl.calendar.BusinessCalendar>();

  public com.finture.bpm.engine.impl.calendar.BusinessCalendar getBusinessCalendar(String businessCalendarRef) {
    return businessCalendars.get(businessCalendarRef);
  }
  
  public BusinessCalendarManager addBusinessCalendar(String businessCalendarRef, BusinessCalendar businessCalendar) {
    businessCalendars.put(businessCalendarRef, businessCalendar);
    return this;
  }
}
