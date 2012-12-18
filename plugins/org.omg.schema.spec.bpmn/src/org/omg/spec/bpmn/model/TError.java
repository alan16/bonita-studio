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
 * A representation of the model object '<em><b>TError</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.omg.spec.bpmn.model.TError#getErrorCode <em>Error Code</em>}</li>
 *   <li>{@link org.omg.spec.bpmn.model.TError#getName <em>Name</em>}</li>
 *   <li>{@link org.omg.spec.bpmn.model.TError#getStructureRef <em>Structure Ref</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.omg.spec.bpmn.model.ModelPackage#getTError()
 * @model extendedMetaData="name='tError' kind='elementOnly'"
 * @generated
 */
public interface TError extends TRootElement {
	/**
	 * Returns the value of the '<em><b>Error Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Error Code</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Error Code</em>' attribute.
	 * @see #setErrorCode(String)
	 * @see org.omg.spec.bpmn.model.ModelPackage#getTError_ErrorCode()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='attribute' name='errorCode'"
	 * @generated
	 */
	String getErrorCode();

	/**
	 * Sets the value of the '{@link org.omg.spec.bpmn.model.TError#getErrorCode <em>Error Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Error Code</em>' attribute.
	 * @see #getErrorCode()
	 * @generated
	 */
	void setErrorCode(String value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.omg.spec.bpmn.model.ModelPackage#getTError_Name()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='attribute' name='name'"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.omg.spec.bpmn.model.TError#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Structure Ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Structure Ref</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Structure Ref</em>' attribute.
	 * @see #setStructureRef(QName)
	 * @see org.omg.spec.bpmn.model.ModelPackage#getTError_StructureRef()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.QName"
	 *        extendedMetaData="kind='attribute' name='structureRef'"
	 * @generated
	 */
	QName getStructureRef();

	/**
	 * Sets the value of the '{@link org.omg.spec.bpmn.model.TError#getStructureRef <em>Structure Ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Structure Ref</em>' attribute.
	 * @see #getStructureRef()
	 * @generated
	 */
	void setStructureRef(QName value);

} // TError
