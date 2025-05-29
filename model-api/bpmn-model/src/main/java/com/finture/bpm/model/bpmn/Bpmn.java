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
package com.finture.bpm.model.bpmn;

import static com.finture.bpm.model.bpmn.impl.BpmnModelConstants.ACTIVITI_NS;
import static com.finture.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static com.finture.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;
import static com.finture.bpm.model.bpmn.impl.instance.ProcessImpl.DEFAULT_HISTORY_TIME_TO_LIVE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import com.finture.bpm.model.bpmn.builder.ProcessBuilder;
import com.finture.bpm.model.bpmn.impl.BpmnParser;
import com.finture.bpm.model.bpmn.impl.instance.ActivationConditionImpl;
import com.finture.bpm.model.bpmn.impl.instance.ActivityImpl;
import com.finture.bpm.model.bpmn.impl.instance.ArtifactImpl;
import com.finture.bpm.model.bpmn.impl.instance.AssignmentImpl;
import com.finture.bpm.model.bpmn.impl.instance.AssociationImpl;
import com.finture.bpm.model.bpmn.impl.instance.AuditingImpl;
import com.finture.bpm.model.bpmn.impl.instance.BaseElementImpl;
import com.finture.bpm.model.bpmn.impl.instance.BoundaryEventImpl;
import com.finture.bpm.model.bpmn.impl.instance.BusinessRuleTaskImpl;
import com.finture.bpm.model.bpmn.impl.instance.CallActivityImpl;
import com.finture.bpm.model.bpmn.impl.instance.CallConversationImpl;
import com.finture.bpm.model.bpmn.impl.instance.CallableElementImpl;
import com.finture.bpm.model.bpmn.impl.instance.CancelEventDefinitionImpl;
import com.finture.bpm.model.bpmn.impl.instance.CatchEventImpl;
import com.finture.bpm.model.bpmn.impl.instance.CategoryImpl;
import com.finture.bpm.model.bpmn.impl.instance.CategoryValueImpl;
import com.finture.bpm.model.bpmn.impl.instance.CategoryValueRef;
import com.finture.bpm.model.bpmn.impl.instance.ChildLaneSet;
import com.finture.bpm.model.bpmn.impl.instance.CollaborationImpl;
import com.finture.bpm.model.bpmn.impl.instance.CompensateEventDefinitionImpl;
import com.finture.bpm.model.bpmn.impl.instance.CompletionConditionImpl;
import com.finture.bpm.model.bpmn.impl.instance.ComplexBehaviorDefinitionImpl;
import com.finture.bpm.model.bpmn.impl.instance.ComplexGatewayImpl;
import com.finture.bpm.model.bpmn.impl.instance.ConditionExpressionImpl;
import com.finture.bpm.model.bpmn.impl.instance.ConditionImpl;
import com.finture.bpm.model.bpmn.impl.instance.ConditionalEventDefinitionImpl;
import com.finture.bpm.model.bpmn.impl.instance.ConversationAssociationImpl;
import com.finture.bpm.model.bpmn.impl.instance.ConversationImpl;
import com.finture.bpm.model.bpmn.impl.instance.ConversationLinkImpl;
import com.finture.bpm.model.bpmn.impl.instance.ConversationNodeImpl;
import com.finture.bpm.model.bpmn.impl.instance.CorrelationKeyImpl;
import com.finture.bpm.model.bpmn.impl.instance.CorrelationPropertyBindingImpl;
import com.finture.bpm.model.bpmn.impl.instance.CorrelationPropertyImpl;
import com.finture.bpm.model.bpmn.impl.instance.CorrelationPropertyRef;
import com.finture.bpm.model.bpmn.impl.instance.CorrelationPropertyRetrievalExpressionImpl;
import com.finture.bpm.model.bpmn.impl.instance.CorrelationSubscriptionImpl;
import com.finture.bpm.model.bpmn.impl.instance.DataAssociationImpl;
import com.finture.bpm.model.bpmn.impl.instance.DataInputAssociationImpl;
import com.finture.bpm.model.bpmn.impl.instance.DataInputImpl;
import com.finture.bpm.model.bpmn.impl.instance.DataInputRefs;
import com.finture.bpm.model.bpmn.impl.instance.DataObjectImpl;
import com.finture.bpm.model.bpmn.impl.instance.DataObjectReferenceImpl;
import com.finture.bpm.model.bpmn.impl.instance.DataOutputAssociationImpl;
import com.finture.bpm.model.bpmn.impl.instance.DataOutputImpl;
import com.finture.bpm.model.bpmn.impl.instance.DataOutputRefs;
import com.finture.bpm.model.bpmn.impl.instance.DataPath;
import com.finture.bpm.model.bpmn.impl.instance.DataStateImpl;
import com.finture.bpm.model.bpmn.impl.instance.DataStoreImpl;
import com.finture.bpm.model.bpmn.impl.instance.DataStoreReferenceImpl;
import com.finture.bpm.model.bpmn.impl.instance.DefinitionsImpl;
import com.finture.bpm.model.bpmn.impl.instance.DocumentationImpl;
import com.finture.bpm.model.bpmn.impl.instance.EndEventImpl;
import com.finture.bpm.model.bpmn.impl.instance.EndPointImpl;
import com.finture.bpm.model.bpmn.impl.instance.EndPointRef;
import com.finture.bpm.model.bpmn.impl.instance.ErrorEventDefinitionImpl;
import com.finture.bpm.model.bpmn.impl.instance.ErrorImpl;
import com.finture.bpm.model.bpmn.impl.instance.ErrorRef;
import com.finture.bpm.model.bpmn.impl.instance.EscalationEventDefinitionImpl;
import com.finture.bpm.model.bpmn.impl.instance.EscalationImpl;
import com.finture.bpm.model.bpmn.impl.instance.EventBasedGatewayImpl;
import com.finture.bpm.model.bpmn.impl.instance.EventDefinitionImpl;
import com.finture.bpm.model.bpmn.impl.instance.EventDefinitionRef;
import com.finture.bpm.model.bpmn.impl.instance.EventImpl;
import com.finture.bpm.model.bpmn.impl.instance.ExclusiveGatewayImpl;
import com.finture.bpm.model.bpmn.impl.instance.ExpressionImpl;
import com.finture.bpm.model.bpmn.impl.instance.ExtensionElementsImpl;
import com.finture.bpm.model.bpmn.impl.instance.ExtensionImpl;
import com.finture.bpm.model.bpmn.impl.instance.FlowElementImpl;
import com.finture.bpm.model.bpmn.impl.instance.FlowNodeImpl;
import com.finture.bpm.model.bpmn.impl.instance.FlowNodeRef;
import com.finture.bpm.model.bpmn.impl.instance.FormalExpressionImpl;
import com.finture.bpm.model.bpmn.impl.instance.From;
import com.finture.bpm.model.bpmn.impl.instance.GatewayImpl;
import com.finture.bpm.model.bpmn.impl.instance.GlobalConversationImpl;
import com.finture.bpm.model.bpmn.impl.instance.GroupImpl;
import com.finture.bpm.model.bpmn.impl.instance.HumanPerformerImpl;
import com.finture.bpm.model.bpmn.impl.instance.ImportImpl;
import com.finture.bpm.model.bpmn.impl.instance.InMessageRef;
import com.finture.bpm.model.bpmn.impl.instance.InclusiveGatewayImpl;
import com.finture.bpm.model.bpmn.impl.instance.Incoming;
import com.finture.bpm.model.bpmn.impl.instance.InnerParticipantRef;
import com.finture.bpm.model.bpmn.impl.instance.InputDataItemImpl;
import com.finture.bpm.model.bpmn.impl.instance.InputSetImpl;
import com.finture.bpm.model.bpmn.impl.instance.InputSetRefs;
import com.finture.bpm.model.bpmn.impl.instance.InteractionNodeImpl;
import com.finture.bpm.model.bpmn.impl.instance.InterfaceImpl;
import com.finture.bpm.model.bpmn.impl.instance.InterfaceRef;
import com.finture.bpm.model.bpmn.impl.instance.IntermediateCatchEventImpl;
import com.finture.bpm.model.bpmn.impl.instance.IntermediateThrowEventImpl;
import com.finture.bpm.model.bpmn.impl.instance.IoBindingImpl;
import com.finture.bpm.model.bpmn.impl.instance.IoSpecificationImpl;
import com.finture.bpm.model.bpmn.impl.instance.ItemAwareElementImpl;
import com.finture.bpm.model.bpmn.impl.instance.ItemDefinitionImpl;
import com.finture.bpm.model.bpmn.impl.instance.LaneImpl;
import com.finture.bpm.model.bpmn.impl.instance.LaneSetImpl;
import com.finture.bpm.model.bpmn.impl.instance.LinkEventDefinitionImpl;
import com.finture.bpm.model.bpmn.impl.instance.LoopCardinalityImpl;
import com.finture.bpm.model.bpmn.impl.instance.LoopCharacteristicsImpl;
import com.finture.bpm.model.bpmn.impl.instance.LoopDataInputRef;
import com.finture.bpm.model.bpmn.impl.instance.LoopDataOutputRef;
import com.finture.bpm.model.bpmn.impl.instance.ManualTaskImpl;
import com.finture.bpm.model.bpmn.impl.instance.MessageEventDefinitionImpl;
import com.finture.bpm.model.bpmn.impl.instance.MessageFlowAssociationImpl;
import com.finture.bpm.model.bpmn.impl.instance.MessageFlowImpl;
import com.finture.bpm.model.bpmn.impl.instance.MessageFlowRef;
import com.finture.bpm.model.bpmn.impl.instance.MessageImpl;
import com.finture.bpm.model.bpmn.impl.instance.MessagePath;
import com.finture.bpm.model.bpmn.impl.instance.MonitoringImpl;
import com.finture.bpm.model.bpmn.impl.instance.MultiInstanceLoopCharacteristicsImpl;
import com.finture.bpm.model.bpmn.impl.instance.OperationImpl;
import com.finture.bpm.model.bpmn.impl.instance.OperationRef;
import com.finture.bpm.model.bpmn.impl.instance.OptionalInputRefs;
import com.finture.bpm.model.bpmn.impl.instance.OptionalOutputRefs;
import com.finture.bpm.model.bpmn.impl.instance.OutMessageRef;
import com.finture.bpm.model.bpmn.impl.instance.OuterParticipantRef;
import com.finture.bpm.model.bpmn.impl.instance.Outgoing;
import com.finture.bpm.model.bpmn.impl.instance.OutputDataItemImpl;
import com.finture.bpm.model.bpmn.impl.instance.OutputSetImpl;
import com.finture.bpm.model.bpmn.impl.instance.OutputSetRefs;
import com.finture.bpm.model.bpmn.impl.instance.ParallelGatewayImpl;
import com.finture.bpm.model.bpmn.impl.instance.ParticipantAssociationImpl;
import com.finture.bpm.model.bpmn.impl.instance.ParticipantImpl;
import com.finture.bpm.model.bpmn.impl.instance.ParticipantMultiplicityImpl;
import com.finture.bpm.model.bpmn.impl.instance.ParticipantRef;
import com.finture.bpm.model.bpmn.impl.instance.PartitionElement;
import com.finture.bpm.model.bpmn.impl.instance.PerformerImpl;
import com.finture.bpm.model.bpmn.impl.instance.PotentialOwnerImpl;
import com.finture.bpm.model.bpmn.impl.instance.ProcessImpl;
import com.finture.bpm.model.bpmn.impl.instance.PropertyImpl;
import com.finture.bpm.model.bpmn.impl.instance.ReceiveTaskImpl;
import com.finture.bpm.model.bpmn.impl.instance.RelationshipImpl;
import com.finture.bpm.model.bpmn.impl.instance.RenderingImpl;
import com.finture.bpm.model.bpmn.impl.instance.ResourceAssignmentExpressionImpl;
import com.finture.bpm.model.bpmn.impl.instance.ResourceImpl;
import com.finture.bpm.model.bpmn.impl.instance.ResourceParameterBindingImpl;
import com.finture.bpm.model.bpmn.impl.instance.ResourceParameterImpl;
import com.finture.bpm.model.bpmn.impl.instance.ResourceRef;
import com.finture.bpm.model.bpmn.impl.instance.ResourceRoleImpl;
import com.finture.bpm.model.bpmn.impl.instance.RootElementImpl;
import com.finture.bpm.model.bpmn.impl.instance.ScriptImpl;
import com.finture.bpm.model.bpmn.impl.instance.ScriptTaskImpl;
import com.finture.bpm.model.bpmn.impl.instance.SendTaskImpl;
import com.finture.bpm.model.bpmn.impl.instance.SequenceFlowImpl;
import com.finture.bpm.model.bpmn.impl.instance.ServiceTaskImpl;
import com.finture.bpm.model.bpmn.impl.instance.SignalEventDefinitionImpl;
import com.finture.bpm.model.bpmn.impl.instance.SignalImpl;
import com.finture.bpm.model.bpmn.impl.instance.Source;
import com.finture.bpm.model.bpmn.impl.instance.SourceRef;
import com.finture.bpm.model.bpmn.impl.instance.StartEventImpl;
import com.finture.bpm.model.bpmn.impl.instance.SubConversationImpl;
import com.finture.bpm.model.bpmn.impl.instance.SubProcessImpl;
import com.finture.bpm.model.bpmn.impl.instance.SupportedInterfaceRef;
import com.finture.bpm.model.bpmn.impl.instance.Supports;
import com.finture.bpm.model.bpmn.impl.instance.Target;
import com.finture.bpm.model.bpmn.impl.instance.TargetRef;
import com.finture.bpm.model.bpmn.impl.instance.TaskImpl;
import com.finture.bpm.model.bpmn.impl.instance.TerminateEventDefinitionImpl;
import com.finture.bpm.model.bpmn.impl.instance.TextAnnotationImpl;
import com.finture.bpm.model.bpmn.impl.instance.TextImpl;
import com.finture.bpm.model.bpmn.impl.instance.ThrowEventImpl;
import com.finture.bpm.model.bpmn.impl.instance.TimeCycleImpl;
import com.finture.bpm.model.bpmn.impl.instance.TimeDateImpl;
import com.finture.bpm.model.bpmn.impl.instance.TimeDurationImpl;
import com.finture.bpm.model.bpmn.impl.instance.TimerEventDefinitionImpl;
import com.finture.bpm.model.bpmn.impl.instance.To;
import com.finture.bpm.model.bpmn.impl.instance.TransactionImpl;
import com.finture.bpm.model.bpmn.impl.instance.Transformation;
import com.finture.bpm.model.bpmn.impl.instance.UserTaskImpl;
import com.finture.bpm.model.bpmn.impl.instance.WhileExecutingInputRefs;
import com.finture.bpm.model.bpmn.impl.instance.WhileExecutingOutputRefs;
import com.finture.bpm.model.bpmn.impl.instance.bpmndi.BpmnDiagramImpl;
import com.finture.bpm.model.bpmn.impl.instance.bpmndi.BpmnEdgeImpl;
import com.finture.bpm.model.bpmn.impl.instance.bpmndi.BpmnLabelImpl;
import com.finture.bpm.model.bpmn.impl.instance.bpmndi.BpmnLabelStyleImpl;
import com.finture.bpm.model.bpmn.impl.instance.bpmndi.BpmnPlaneImpl;
import com.finture.bpm.model.bpmn.impl.instance.bpmndi.BpmnShapeImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaConnectorIdImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaConnectorImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaConstraintImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaEntryImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaErrorEventDefinitionImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaExecutionListenerImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaExpressionImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaFailedJobRetryTimeCycleImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaFieldImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaFormDataImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaFormFieldImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaFormPropertyImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaInImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaInputOutputImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaInputParameterImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaListImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaMapImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaOutImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaOutputParameterImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaPotentialStarterImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaPropertiesImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaPropertyImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaScriptImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaStringImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaTaskListenerImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaValidationImpl;
import com.finture.bpm.model.bpmn.impl.instance.camunda.CamundaValueImpl;
import com.finture.bpm.model.bpmn.impl.instance.dc.BoundsImpl;
import com.finture.bpm.model.bpmn.impl.instance.dc.FontImpl;
import com.finture.bpm.model.bpmn.impl.instance.dc.PointImpl;
import com.finture.bpm.model.bpmn.impl.instance.di.DiagramElementImpl;
import com.finture.bpm.model.bpmn.impl.instance.di.DiagramImpl;
import com.finture.bpm.model.bpmn.impl.instance.di.EdgeImpl;
import com.finture.bpm.model.bpmn.impl.instance.di.LabelImpl;
import com.finture.bpm.model.bpmn.impl.instance.di.LabeledEdgeImpl;
import com.finture.bpm.model.bpmn.impl.instance.di.LabeledShapeImpl;
import com.finture.bpm.model.bpmn.impl.instance.di.NodeImpl;
import com.finture.bpm.model.bpmn.impl.instance.di.PlaneImpl;
import com.finture.bpm.model.bpmn.impl.instance.di.ShapeImpl;
import com.finture.bpm.model.bpmn.impl.instance.di.StyleImpl;
import com.finture.bpm.model.bpmn.impl.instance.di.WaypointImpl;
import com.finture.bpm.model.bpmn.instance.Definitions;
import com.finture.bpm.model.bpmn.instance.Process;
import com.finture.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import com.finture.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import com.finture.bpm.model.xml.Model;
import com.finture.bpm.model.xml.ModelBuilder;
import com.finture.bpm.model.xml.ModelException;
import com.finture.bpm.model.xml.ModelParseException;
import com.finture.bpm.model.xml.ModelValidationException;
import com.finture.bpm.model.xml.impl.instance.ModelElementInstanceImpl;
import com.finture.bpm.model.xml.impl.util.IoUtil;

/**
 * <p>Provides access to the camunda BPMN model api.</p>
 *
 * @author Daniel Meyer
 *
 */
public class Bpmn {

  /** the singleton instance of {@link Bpmn}. If you want to customize the behavior of Bpmn,
   * replace this instance with an instance of a custom subclass of {@link Bpmn}. */
  public static Bpmn INSTANCE = new Bpmn();

  /** the parser used by the Bpmn implementation. */
  private BpmnParser bpmnParser = new BpmnParser();
  private final ModelBuilder bpmnModelBuilder;

  /** The {@link Model}
   */
  private Model bpmnModel;

  /**
   * Allows reading a {@link BpmnModelInstance} from a File.
   *
   * @param file the {@link File} to read the {@link BpmnModelInstance} from
   * @return the model read
   * @throws BpmnModelException if the model cannot be read
   */
  public static BpmnModelInstance readModelFromFile(File file) {
    return INSTANCE.doReadModelFromFile(file);
  }

  /**
   * Allows reading a {@link BpmnModelInstance} from an {@link InputStream}
   *
   * @param stream the {@link InputStream} to read the {@link BpmnModelInstance} from
   * @return the model read
   * @throws ModelParseException if the model cannot be read
   */
  public static BpmnModelInstance readModelFromStream(InputStream stream) {
    return INSTANCE.doReadModelFromInputStream(stream);
  }

  /**
   * Allows writing a {@link BpmnModelInstance} to a File. It will be
   * validated before writing.
   *
   * @param file the {@link File} to write the {@link BpmnModelInstance} to
   * @param modelInstance the {@link BpmnModelInstance} to write
   * @throws BpmnModelException if the model cannot be written
   * @throws ModelValidationException if the model is not valid
   */
  public static void writeModelToFile(File file, BpmnModelInstance modelInstance) {
    INSTANCE.doWriteModelToFile(file, modelInstance);
  }

  /**
   * Allows writing a {@link BpmnModelInstance} to an {@link OutputStream}. It will be
   * validated before writing.
   *
   * @param stream the {@link OutputStream} to write the {@link BpmnModelInstance} to
   * @param modelInstance the {@link BpmnModelInstance} to write
   * @throws ModelException if the model cannot be written
   * @throws ModelValidationException if the model is not valid
   */
  public static void writeModelToStream(OutputStream stream, BpmnModelInstance modelInstance) {
    INSTANCE.doWriteModelToOutputStream(stream, modelInstance);
  }

  /**
   * Allows the conversion of a {@link BpmnModelInstance} to an {@link String}. It will
   * be validated before conversion.
   *
   * @param modelInstance  the model instance to convert
   * @return the XML string representation of the model instance
   */
  public static String convertToString(BpmnModelInstance modelInstance) {
    return INSTANCE.doConvertToString(modelInstance);
  }

  /**
   * Validate model DOM document
   *
   * @param modelInstance the {@link BpmnModelInstance} to validate
   * @throws ModelValidationException if the model is not valid
   */
  public static void validateModel(BpmnModelInstance modelInstance) {
    INSTANCE.doValidateModel(modelInstance);
  }

  /**
   * Allows creating an new, empty {@link BpmnModelInstance}.
   *
   * @return the empty model.
   */
  public static BpmnModelInstance createEmptyModel() {
    return INSTANCE.doCreateEmptyModel();
  }

  public static ProcessBuilder createProcess() {
    BpmnModelInstance modelInstance = INSTANCE.doCreateEmptyModel();
    Definitions definitions = modelInstance.newInstance(Definitions.class);
    definitions.setTargetNamespace(BPMN20_NS);
    definitions.getDomElement().registerNamespace("camunda", CAMUNDA_NS);
    modelInstance.setDefinitions(definitions);
    Process process = modelInstance.newInstance(Process.class);
    definitions.addChildElement(process);

    BpmnDiagram bpmnDiagram = modelInstance.newInstance(BpmnDiagram.class);

    BpmnPlane bpmnPlane = modelInstance.newInstance(BpmnPlane.class);
    bpmnPlane.setBpmnElement(process);

    bpmnDiagram.addChildElement(bpmnPlane);
    definitions.addChildElement(bpmnDiagram);

    return process.builder().camundaHistoryTimeToLiveString(DEFAULT_HISTORY_TIME_TO_LIVE);
  }

  public static ProcessBuilder createProcess(String processId) {
    return createProcess().id(processId);
  }

  public static ProcessBuilder createExecutableProcess() {
    return createProcess().executable();
  }

  public static ProcessBuilder createExecutableProcess(String processId) {
    return createProcess(processId).executable();
  }


  /**
   * Register known types of the BPMN model
   */
  protected Bpmn() {
    bpmnModelBuilder = ModelBuilder.createInstance("BPMN Model");
    bpmnModelBuilder.alternativeNamespace(ACTIVITI_NS, CAMUNDA_NS);
    doRegisterTypes(bpmnModelBuilder);
    bpmnModel = bpmnModelBuilder.build();
  }

  protected BpmnModelInstance doReadModelFromFile(File file) {
    InputStream is = null;
    try {
      is = new FileInputStream(file);
      return doReadModelFromInputStream(is);

    } catch (FileNotFoundException e) {
      throw new BpmnModelException("Cannot read model from file "+file+": file does not exist.");

    } finally {
      IoUtil.closeSilently(is);

    }
  }

  protected BpmnModelInstance doReadModelFromInputStream(InputStream is) {
    return bpmnParser.parseModelFromStream(is);
  }

  protected void doWriteModelToFile(File file, BpmnModelInstance modelInstance) {
    OutputStream os = null;
    try {
      os = new FileOutputStream(file);
      doWriteModelToOutputStream(os, modelInstance);
    }
    catch (FileNotFoundException e) {
      throw new BpmnModelException("Cannot write model to file "+file+": file does not exist.");
    } finally {
      IoUtil.closeSilently(os);
    }
  }

  protected void doWriteModelToOutputStream(OutputStream os, BpmnModelInstance modelInstance) {
    // validate DOM document
    doValidateModel(modelInstance);
    // write XML
    IoUtil.writeDocumentToOutputStream(modelInstance.getDocument(), os);
  }

  protected String doConvertToString(BpmnModelInstance modelInstance) {
    // validate DOM document
    doValidateModel(modelInstance);
    // convert to XML string
    return IoUtil.convertXmlDocumentToString(modelInstance.getDocument());
  }

  protected void doValidateModel(BpmnModelInstance modelInstance) {
    bpmnParser.validateModel(modelInstance.getDocument());
  }

  protected BpmnModelInstance doCreateEmptyModel() {
    return bpmnParser.getEmptyModel();
  }

  protected void doRegisterTypes(ModelBuilder bpmnModelBuilder) {
    ActivationConditionImpl.registerType(bpmnModelBuilder);
    ActivityImpl.registerType(bpmnModelBuilder);
    ArtifactImpl.registerType(bpmnModelBuilder);
    AssignmentImpl.registerType(bpmnModelBuilder);
    AssociationImpl.registerType(bpmnModelBuilder);
    AuditingImpl.registerType(bpmnModelBuilder);
    BaseElementImpl.registerType(bpmnModelBuilder);
    BoundaryEventImpl.registerType(bpmnModelBuilder);
    BusinessRuleTaskImpl.registerType(bpmnModelBuilder);
    CallableElementImpl.registerType(bpmnModelBuilder);
    CallActivityImpl.registerType(bpmnModelBuilder);
    CallConversationImpl.registerType(bpmnModelBuilder);
    CancelEventDefinitionImpl.registerType(bpmnModelBuilder);
    CatchEventImpl.registerType(bpmnModelBuilder);
    CategoryImpl.registerType(bpmnModelBuilder);
    CategoryValueImpl.registerType(bpmnModelBuilder);
    CategoryValueRef.registerType(bpmnModelBuilder);
    ChildLaneSet.registerType(bpmnModelBuilder);
    CollaborationImpl.registerType(bpmnModelBuilder);
    CompensateEventDefinitionImpl.registerType(bpmnModelBuilder);
    ConditionImpl.registerType(bpmnModelBuilder);
    ConditionalEventDefinitionImpl.registerType(bpmnModelBuilder);
    CompletionConditionImpl.registerType(bpmnModelBuilder);
    ComplexBehaviorDefinitionImpl.registerType(bpmnModelBuilder);
    ComplexGatewayImpl.registerType(bpmnModelBuilder);
    ConditionExpressionImpl.registerType(bpmnModelBuilder);
    ConversationAssociationImpl.registerType(bpmnModelBuilder);
    ConversationImpl.registerType(bpmnModelBuilder);
    ConversationLinkImpl.registerType(bpmnModelBuilder);
    ConversationNodeImpl.registerType(bpmnModelBuilder);
    CorrelationKeyImpl.registerType(bpmnModelBuilder);
    CorrelationPropertyBindingImpl.registerType(bpmnModelBuilder);
    CorrelationPropertyImpl.registerType(bpmnModelBuilder);
    CorrelationPropertyRef.registerType(bpmnModelBuilder);
    CorrelationPropertyRetrievalExpressionImpl.registerType(bpmnModelBuilder);
    CorrelationSubscriptionImpl.registerType(bpmnModelBuilder);
    DataAssociationImpl.registerType(bpmnModelBuilder);
    DataInputAssociationImpl.registerType(bpmnModelBuilder);
    DataInputImpl.registerType(bpmnModelBuilder);
    DataInputRefs.registerType(bpmnModelBuilder);
    DataOutputAssociationImpl.registerType(bpmnModelBuilder);
    DataOutputImpl.registerType(bpmnModelBuilder);
    DataOutputRefs.registerType(bpmnModelBuilder);
    DataPath.registerType(bpmnModelBuilder);
    DataStateImpl.registerType(bpmnModelBuilder);
    DataObjectImpl.registerType(bpmnModelBuilder);
    DataObjectReferenceImpl.registerType(bpmnModelBuilder);
    DataStoreImpl.registerType(bpmnModelBuilder);
    DataStoreReferenceImpl.registerType(bpmnModelBuilder);
    DefinitionsImpl.registerType(bpmnModelBuilder);
    DocumentationImpl.registerType(bpmnModelBuilder);
    EndEventImpl.registerType(bpmnModelBuilder);
    EndPointImpl.registerType(bpmnModelBuilder);
    EndPointRef.registerType(bpmnModelBuilder);
    ErrorEventDefinitionImpl.registerType(bpmnModelBuilder);
    ErrorImpl.registerType(bpmnModelBuilder);
    ErrorRef.registerType(bpmnModelBuilder);
    EscalationImpl.registerType(bpmnModelBuilder);
    EscalationEventDefinitionImpl.registerType(bpmnModelBuilder);
    EventBasedGatewayImpl.registerType(bpmnModelBuilder);
    EventDefinitionImpl.registerType(bpmnModelBuilder);
    EventDefinitionRef.registerType(bpmnModelBuilder);
    EventImpl.registerType(bpmnModelBuilder);
    ExclusiveGatewayImpl.registerType(bpmnModelBuilder);
    ExpressionImpl.registerType(bpmnModelBuilder);
    ExtensionElementsImpl.registerType(bpmnModelBuilder);
    ExtensionImpl.registerType(bpmnModelBuilder);
    FlowElementImpl.registerType(bpmnModelBuilder);
    FlowNodeImpl.registerType(bpmnModelBuilder);
    FlowNodeRef.registerType(bpmnModelBuilder);
    FormalExpressionImpl.registerType(bpmnModelBuilder);
    From.registerType(bpmnModelBuilder);
    GatewayImpl.registerType(bpmnModelBuilder);
    GlobalConversationImpl.registerType(bpmnModelBuilder);
    GroupImpl.registerType(bpmnModelBuilder);
    HumanPerformerImpl.registerType(bpmnModelBuilder);
    ImportImpl.registerType(bpmnModelBuilder);
    InclusiveGatewayImpl.registerType(bpmnModelBuilder);
    Incoming.registerType(bpmnModelBuilder);
    InMessageRef.registerType(bpmnModelBuilder);
    InnerParticipantRef.registerType(bpmnModelBuilder);
    InputDataItemImpl.registerType(bpmnModelBuilder);
    InputSetImpl.registerType(bpmnModelBuilder);
    InputSetRefs.registerType(bpmnModelBuilder);
    InteractionNodeImpl.registerType(bpmnModelBuilder);
    InterfaceImpl.registerType(bpmnModelBuilder);
    InterfaceRef.registerType(bpmnModelBuilder);
    IntermediateCatchEventImpl.registerType(bpmnModelBuilder);
    IntermediateThrowEventImpl.registerType(bpmnModelBuilder);
    IoBindingImpl.registerType(bpmnModelBuilder);
    IoSpecificationImpl.registerType(bpmnModelBuilder);
    ItemAwareElementImpl.registerType(bpmnModelBuilder);
    ItemDefinitionImpl.registerType(bpmnModelBuilder);
    LaneImpl.registerType(bpmnModelBuilder);
    LaneSetImpl.registerType(bpmnModelBuilder);
    LinkEventDefinitionImpl.registerType(bpmnModelBuilder);
    LoopCardinalityImpl.registerType(bpmnModelBuilder);
    LoopCharacteristicsImpl.registerType(bpmnModelBuilder);
    LoopDataInputRef.registerType(bpmnModelBuilder);
    LoopDataOutputRef.registerType(bpmnModelBuilder);
    ManualTaskImpl.registerType(bpmnModelBuilder);
    MessageEventDefinitionImpl.registerType(bpmnModelBuilder);
    MessageFlowAssociationImpl.registerType(bpmnModelBuilder);
    MessageFlowImpl.registerType(bpmnModelBuilder);
    MessageFlowRef.registerType(bpmnModelBuilder);
    MessageImpl.registerType(bpmnModelBuilder);
    MessagePath.registerType(bpmnModelBuilder);
    ModelElementInstanceImpl.registerType(bpmnModelBuilder);
    MonitoringImpl.registerType(bpmnModelBuilder);
    MultiInstanceLoopCharacteristicsImpl.registerType(bpmnModelBuilder);
    OperationImpl.registerType(bpmnModelBuilder);
    OperationRef.registerType(bpmnModelBuilder);
    OptionalInputRefs.registerType(bpmnModelBuilder);
    OptionalOutputRefs.registerType(bpmnModelBuilder);
    OuterParticipantRef.registerType(bpmnModelBuilder);
    OutMessageRef.registerType(bpmnModelBuilder);
    Outgoing.registerType(bpmnModelBuilder);
    OutputDataItemImpl.registerType(bpmnModelBuilder);
    OutputSetImpl.registerType(bpmnModelBuilder);
    OutputSetRefs.registerType(bpmnModelBuilder);
    ParallelGatewayImpl.registerType(bpmnModelBuilder);
    ParticipantAssociationImpl.registerType(bpmnModelBuilder);
    ParticipantImpl.registerType(bpmnModelBuilder);
    ParticipantMultiplicityImpl.registerType(bpmnModelBuilder);
    ParticipantRef.registerType(bpmnModelBuilder);
    PartitionElement.registerType(bpmnModelBuilder);
    PerformerImpl.registerType(bpmnModelBuilder);
    PotentialOwnerImpl.registerType(bpmnModelBuilder);
    ProcessImpl.registerType(bpmnModelBuilder);
    PropertyImpl.registerType(bpmnModelBuilder);
    ReceiveTaskImpl.registerType(bpmnModelBuilder);
    RelationshipImpl.registerType(bpmnModelBuilder);
    RenderingImpl.registerType(bpmnModelBuilder);
    ResourceAssignmentExpressionImpl.registerType(bpmnModelBuilder);
    ResourceImpl.registerType(bpmnModelBuilder);
    ResourceParameterBindingImpl.registerType(bpmnModelBuilder);
    ResourceParameterImpl.registerType(bpmnModelBuilder);
    ResourceRef.registerType(bpmnModelBuilder);
    ResourceRoleImpl.registerType(bpmnModelBuilder);
    RootElementImpl.registerType(bpmnModelBuilder);
    ScriptImpl.registerType(bpmnModelBuilder);
    ScriptTaskImpl.registerType(bpmnModelBuilder);
    SendTaskImpl.registerType(bpmnModelBuilder);
    SequenceFlowImpl.registerType(bpmnModelBuilder);
    ServiceTaskImpl.registerType(bpmnModelBuilder);
    SignalEventDefinitionImpl.registerType(bpmnModelBuilder);
    SignalImpl.registerType(bpmnModelBuilder);
    Source.registerType(bpmnModelBuilder);
    SourceRef.registerType(bpmnModelBuilder);
    StartEventImpl.registerType(bpmnModelBuilder);
    SubConversationImpl.registerType(bpmnModelBuilder);
    SubProcessImpl.registerType(bpmnModelBuilder);
    SupportedInterfaceRef.registerType(bpmnModelBuilder);
    Supports.registerType(bpmnModelBuilder);
    Target.registerType(bpmnModelBuilder);
    TargetRef.registerType(bpmnModelBuilder);
    TaskImpl.registerType(bpmnModelBuilder);
    TerminateEventDefinitionImpl.registerType(bpmnModelBuilder);
    TextImpl.registerType(bpmnModelBuilder);
    TextAnnotationImpl.registerType(bpmnModelBuilder);
    ThrowEventImpl.registerType(bpmnModelBuilder);
    TimeCycleImpl.registerType(bpmnModelBuilder);
    TimeDateImpl.registerType(bpmnModelBuilder);
    TimeDurationImpl.registerType(bpmnModelBuilder);
    TimerEventDefinitionImpl.registerType(bpmnModelBuilder);
    To.registerType(bpmnModelBuilder);
    TransactionImpl.registerType(bpmnModelBuilder);
    Transformation.registerType(bpmnModelBuilder);
    UserTaskImpl.registerType(bpmnModelBuilder);
    WhileExecutingInputRefs.registerType(bpmnModelBuilder);
    WhileExecutingOutputRefs.registerType(bpmnModelBuilder);

    /** DC */
    FontImpl.registerType(bpmnModelBuilder);
    PointImpl.registerType(bpmnModelBuilder);
    BoundsImpl.registerType(bpmnModelBuilder);

    /** DI */
    DiagramImpl.registerType(bpmnModelBuilder);
    DiagramElementImpl.registerType(bpmnModelBuilder);
    EdgeImpl.registerType(bpmnModelBuilder);
    com.finture.bpm.model.bpmn.impl.instance.di.ExtensionImpl.registerType(bpmnModelBuilder);
    LabelImpl.registerType(bpmnModelBuilder);
    LabeledEdgeImpl.registerType(bpmnModelBuilder);
    LabeledShapeImpl.registerType(bpmnModelBuilder);
    NodeImpl.registerType(bpmnModelBuilder);
    PlaneImpl.registerType(bpmnModelBuilder);
    ShapeImpl.registerType(bpmnModelBuilder);
    StyleImpl.registerType(bpmnModelBuilder);
    WaypointImpl.registerType(bpmnModelBuilder);

    /** BPMNDI */
    BpmnDiagramImpl.registerType(bpmnModelBuilder);
    BpmnEdgeImpl.registerType(bpmnModelBuilder);
    BpmnLabelImpl.registerType(bpmnModelBuilder);
    BpmnLabelStyleImpl.registerType(bpmnModelBuilder);
    BpmnPlaneImpl.registerType(bpmnModelBuilder);
    BpmnShapeImpl.registerType(bpmnModelBuilder);

    /** camunda extensions */
    CamundaConnectorImpl.registerType(bpmnModelBuilder);
    CamundaConnectorIdImpl.registerType(bpmnModelBuilder);
    CamundaConstraintImpl.registerType(bpmnModelBuilder);
    CamundaEntryImpl.registerType(bpmnModelBuilder);
    CamundaErrorEventDefinitionImpl.registerType(bpmnModelBuilder);
    CamundaExecutionListenerImpl.registerType(bpmnModelBuilder);
    CamundaExpressionImpl.registerType(bpmnModelBuilder);
    CamundaFailedJobRetryTimeCycleImpl.registerType(bpmnModelBuilder);
    CamundaFieldImpl.registerType(bpmnModelBuilder);
    CamundaFormDataImpl.registerType(bpmnModelBuilder);
    CamundaFormFieldImpl.registerType(bpmnModelBuilder);
    CamundaFormPropertyImpl.registerType(bpmnModelBuilder);
    CamundaInImpl.registerType(bpmnModelBuilder);
    CamundaInputOutputImpl.registerType(bpmnModelBuilder);
    CamundaInputParameterImpl.registerType(bpmnModelBuilder);
    CamundaListImpl.registerType(bpmnModelBuilder);
    CamundaMapImpl.registerType(bpmnModelBuilder);
    CamundaOutputParameterImpl.registerType(bpmnModelBuilder);
    CamundaOutImpl.registerType(bpmnModelBuilder);
    CamundaPotentialStarterImpl.registerType(bpmnModelBuilder);
    CamundaPropertiesImpl.registerType(bpmnModelBuilder);
    CamundaPropertyImpl.registerType(bpmnModelBuilder);
    CamundaScriptImpl.registerType(bpmnModelBuilder);
    CamundaStringImpl.registerType(bpmnModelBuilder);
    CamundaTaskListenerImpl.registerType(bpmnModelBuilder);
    CamundaValidationImpl.registerType(bpmnModelBuilder);
    CamundaValueImpl.registerType(bpmnModelBuilder);
  }

  /**
   * @return the {@link Model} instance to use
   */
  public Model getBpmnModel() {
    return bpmnModel;
  }

  public ModelBuilder getBpmnModelBuilder() {
    return bpmnModelBuilder;
  }

  /**
   * @param bpmnModel the bpmnModel to set
   */
  public void setBpmnModel(Model bpmnModel) {
    this.bpmnModel = bpmnModel;
  }

}
