/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.data.provider;

import java.util.List;

import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.model.form.Form;
import org.bonitasoft.studio.model.process.Data;
import org.bonitasoft.studio.model.process.PageFlow;
import org.eclipse.emf.ecore.EObject;

/**
 * @author Aurelien Pupier
 * /!\ not declared in extension points, we don't want it in the list, it is only used in Operation output
 *
 */
public class DataExpressionProviderForFormOutput extends DataExpressionProvider {

	protected List<Data> getDataInForm(Form form, final EObject formContainer) {
		return ModelHelper.getAccessibleDataInFormsWithNoRestriction((PageFlow) formContainer, form.eContainmentFeature());
	}
	
}
