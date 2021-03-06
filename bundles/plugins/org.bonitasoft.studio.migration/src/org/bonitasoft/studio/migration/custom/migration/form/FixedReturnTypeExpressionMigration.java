/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.studio.migration.custom.migration.form;

import org.eclipse.emf.edapt.migration.CustomMigration;
import org.eclipse.emf.edapt.migration.Instance;
import org.eclipse.emf.edapt.migration.Metamodel;
import org.eclipse.emf.edapt.migration.MigrationException;
import org.eclipse.emf.edapt.migration.Model;

/**
 * @author Florine Boudin
 *
 */
public class FixedReturnTypeExpressionMigration extends CustomMigration {

	
	@Override
	public void migrateAfter(Model model, Metamodel metamodel)
			throws MigrationException {
		
		// TextFormField
		for(Instance instance : model.getAllInstances("form.TextFormField")){
			
			Instance exp = instance.get("inputExpression");
			if(exp!=null){
				exp.set("returnTypeFixed", false);
			}
		}
		
		// TextAreaFormField
		for(Instance instance : model.getAllInstances("form.TextAreaFormField")){
			
			Instance exp = instance.get("inputExpression");
			if(exp!=null){
				exp.set("returnTypeFixed", false);
			}
		}
		
		// RichTextAreaFormField
		for(Instance instance : model.getAllInstances("form.RichTextAreaFormField")){
			
			Instance exp = instance.get("inputExpression");
			if(exp!=null){
				exp.set("returnTypeFixed", false);
			}
		}
			
		
		// CheckBoxMultipleFormField
		for(Instance instance : model.getAllInstances("form.CheckBoxMultipleFormField")){
			
			Instance exp = instance.get("inputExpression");
			if(exp!=null){
				exp.set("returnTypeFixed", false);
			}
		}
			
		// CheckBoxSingleFormField
		for(Instance instance : model.getAllInstances("form.CheckBoxSingleFormField")){
			
			Instance exp = instance.get("inputExpression");
			if(exp!=null){
				exp.set("returnTypeFixed", false);
				exp.set("returnType", Boolean.class.getName());
			}
		}
			
		// DateFormField
		for(Instance instance : model.getAllInstances("form.DateFormField")){
			
			Instance exp = instance.get("inputExpression");
			if(exp!=null){
				exp.set("returnTypeFixed", false);
			}
		}		
	}
	
	
}
