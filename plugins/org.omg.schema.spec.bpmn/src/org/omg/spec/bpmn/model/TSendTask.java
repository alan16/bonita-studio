/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.omg.spec.bpmn.model;

import javax.xml.namespace.QName;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>TSend Task</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.omg.spec.bpmn.model.TSendTask#getImplementation <em>Implementation</em>}</li>
 *   <li>{@link org.omg.spec.bpmn.model.TSendTask#getMessageRef <em>Message Ref</em>}</li>
 *   <li>{@link org.omg.spec.bpmn.model.TSendTask#getOperationRef <em>Operation Ref</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.omg.spec.bpmn.model.ModelPackage#getTSendTask()
 * @model extendedMetaData="name='tSendTask' kind='elementOnly'"
 * @generated
 */
public interface TSendTask extends TTask {
	/**
	 * Returns the value of the '<em><b>Implementation</b></em>' attribute.
	 * The default value is <code>"##WebService"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Implementation</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Implementation</em>' attribute.
	 * @see #isSetImplementation()
	 * @see #unsetImplementation()
	 * @see #setImplementation(Object)
	 * @see org.omg.spec.bpmn.model.ModelPackage#getTSendTask_Implementation()
	 * @model default="##WebService" unsettable="true" dataType="org.omg.spec.bpmn.model.TImplementation"
	 *        extendedMetaData="kind='attribute' name='implementation'"
	 * @generated
	 */
	Object getImplementation();

	/**
	 * Sets the value of the '{@link org.omg.spec.bpmn.model.TSendTask#getImplementation <em>Implementation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Implementation</em>' attribute.
	 * @see #isSetImplementation()
	 * @see #unsetImplementation()
	 * @see #getImplementation()
	 * @generated
	 */
	void setImplementation(Object value);

	/**
	 * Unsets the value of the '{@link org.omg.spec.bpmn.model.TSendTask#getImplementation <em>Implementation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetImplementation()
	 * @see #getImplementation()
	 * @see #setImplementation(Object)
	 * @generated
	 */
	void unsetImplementation();

	/**
	 * Returns whether the value of the '{@link org.omg.spec.bpmn.model.TSendTask#getImplementation <em>Implementation</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Implementation</em>' attribute is set.
	 * @see #unsetImplementation()
	 * @see #getImplementation()
	 * @see #setImplementation(Object)
	 * @generated
	 */
	boolean isSetImplementation();

	/**
	 * Returns the value of the '<em><b>Message Ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Message Ref</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Message Ref</em>' attribute.
	 * @see #setMessageRef(QName)
	 * @see org.omg.spec.bpmn.model.ModelPackage#getTSendTask_MessageRef()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.QName"
	 *        extendedMetaData="kind='attribute' name='messageRef'"
	 * @generated
	 */
	QName getMessageRef();

	/**
	 * Sets the value of the '{@link org.omg.spec.bpmn.model.TSendTask#getMessageRef <em>Message Ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Message Ref</em>' attribute.
	 * @see #getMessageRef()
	 * @generated
	 */
	void setMessageRef(QName value);

	/**
	 * Returns the value of the '<em><b>Operation Ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Operation Ref</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Operation Ref</em>' attribute.
	 * @see #setOperationRef(QName)
	 * @see org.omg.spec.bpmn.model.ModelPackage#getTSendTask_OperationRef()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.QName"
	 *        extendedMetaData="kind='attribute' name='operationRef'"
	 * @generated
	 */
	QName getOperationRef();

	/**
	 * Sets the value of the '{@link org.omg.spec.bpmn.model.TSendTask#getOperationRef <em>Operation Ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Operation Ref</em>' attribute.
	 * @see #getOperationRef()
	 * @generated
	 */
	void setOperationRef(QName value);

} // TSendTask
