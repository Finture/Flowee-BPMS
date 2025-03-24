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
package com.finture.bpm.model.bpmn.impl.instance;

import com.finture.bpm.model.bpmn.instance.BaseElement;
import com.finture.bpm.model.bpmn.instance.Lane;
import com.finture.bpm.model.bpmn.instance.LaneSet;
import com.finture.bpm.model.xml.ModelBuilder;
import com.finture.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import com.finture.bpm.model.xml.type.ModelElementTypeBuilder;
import com.finture.bpm.model.xml.type.attribute.Attribute;
import com.finture.bpm.model.xml.type.child.ChildElementCollection;
import com.finture.bpm.model.xml.type.child.SequenceBuilder;

import java.util.Collection;

import static com.finture.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static com.finture.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN laneSet element
 *
 * @author Sebastian Menski
 */
public class LaneSetImpl extends BaseElementImpl implements LaneSet {

  protected static Attribute<String> nameAttribute;
  protected static ChildElementCollection<Lane> laneCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(LaneSet.class, BPMN_ELEMENT_LANE_SET)
      .namespaceUri(BPMN20_NS)
      .extendsType(BaseElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<LaneSet>() {
        public LaneSet newInstance(ModelTypeInstanceContext instanceContext) {
          return new LaneSetImpl(instanceContext);
        }
      });

    nameAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_NAME)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    laneCollection = sequenceBuilder.elementCollection(Lane.class)
      .build();

    typeBuilder.build();
  }

  public LaneSetImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getName() {
    return nameAttribute.getValue(this);
  }

  public void setName(String name) {
    nameAttribute.setValue(this, name);
  }

  public Collection<Lane> getLanes() {
    return laneCollection.get(this);
  }
}
