/*******************************************************************************
 * Copyright (c) 2007, 2010 BMW Car IT, Technische Universitaet Muenchen, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BMW Car IT - Initial API and implementation
 *     Technische Universitaet Muenchen - Major refactoring and extension
 *******************************************************************************/
package org.eclipse.emf.edapt.migration;

import org.eclipse.emf.common.util.Diagnostic;

/**
 * Exception that encapsulates constraint violations.
 * 
 * @author herrmama
 * @author $Author: mherrmannsd $
 * @version $Rev: 138 $
 * @levd.rating YELLOW Hash: 373C04E5EB3EEFC650BD79DF0EC39901
 */
public class DiagnosticException extends Exception {

	/** Constraint violation. */
	private final Diagnostic diagnostic;

	/** Constructor. */
	public DiagnosticException(String message, Diagnostic diagnostic) {
		super(message);
		this.diagnostic = diagnostic;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getMessage() {
		return super.getMessage() + "\n" + assembleViolations();
	}
	
	/** Assemble textual representation of violations. */
	protected String assembleViolations() {
		StringBuffer buffer = new StringBuffer();
		for(Diagnostic d : diagnostic.getChildren()) {
			buffer.append(d.getMessage() + "\n");
		}
		return buffer.toString();
	}

	/** Get the diagnostic. */
	public Diagnostic getDiagnostic() {
		return diagnostic;
	}
}
