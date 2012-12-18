/**
 * Copyright (C) 2010 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.properties.form.sections.actions.contributions;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.properties.ExtensibleGridPropertySection;
import org.bonitasoft.studio.common.properties.IExtensibleGridPropertySectionContribution;
import org.bonitasoft.studio.data.provider.DataExpressionProviderForFormOutput;
import org.bonitasoft.studio.expression.editor.filter.HiddenExpressionTypeFilter;
import org.bonitasoft.studio.expression.editor.operation.OperationViewer;
import org.bonitasoft.studio.form.properties.i18n.Messages;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionFactory;
import org.bonitasoft.studio.model.expression.Operation;
import org.bonitasoft.studio.model.expression.Operator;
import org.bonitasoft.studio.model.form.FileWidget;
import org.bonitasoft.studio.model.form.Form;
import org.bonitasoft.studio.model.form.FormField;
import org.bonitasoft.studio.model.form.FormPackage;
import org.bonitasoft.studio.model.form.ViewForm;
import org.bonitasoft.studio.model.form.Widget;
import org.bonitasoft.studio.properties.form.provider.ExpressionViewerVariableFilter;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

/**
 * @author Baptiste Mesta
 * @author Aurelien Pupier - allow to create a data from here
 */
public class OutputSectionContribution implements IExtensibleGridPropertySectionContribution {

    protected Widget element;
    protected TransactionalEditingDomain editingDomain;
    protected EMFDataBindingContext dataBinding;
    private OperationViewer operationViewer;



    public void createControl(Composite composite, TabbedPropertySheetWidgetFactory widgetFactory, ExtensibleGridPropertySection extensibleGridPropertySection) {
        composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).create());

        HiddenExpressionTypeFilter expressionFilter = new HiddenExpressionTypeFilter(new String[]{
                ExpressionConstants.I18N_TYPE,
                ExpressionConstants.CONNECTOR_TYPE,
                ExpressionConstants.PARAMETER_TYPE,
                ExpressionConstants.SIMULATION_VARIABLE_TYPE,
                ExpressionConstants.CONNECTOR_OUTPUT_TYPE
        }) ;

        operationViewer = new OperationViewer(composite, widgetFactory,getEditingDomain(), expressionFilter, new ExpressionViewerVariableFilter()) ;
        operationViewer.setStorageExpressionContentProvider(new DataExpressionProviderForFormOutput());
        operationViewer.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create()) ;

        bindWidgets();
    }



    protected TransactionalEditingDomain getEditingDomain() {
        return editingDomain;
    }

    protected void bindWidgets() {
        if(operationViewer != null && !operationViewer.isDisposed()){
            if(dataBinding != null){
                dataBinding.dispose();
            }
            dataBinding = new EMFDataBindingContext();
            operationViewer.setContext(dataBinding) ;
            Operation action = element.getAction() ;
            if(action == null){
                action = ExpressionFactory.eINSTANCE.createOperation() ;
                Operator op = ExpressionFactory.eINSTANCE.createOperator() ;
                op.setType(ExpressionConstants.ASSIGNMENT_OPERATOR) ;
                op.setExpression("=") ;
                action.setOperator(op) ;

                Expression variableExp = ExpressionFactory.eINSTANCE.createExpression() ;
                Expression actionExp = ExpressionFactory.eINSTANCE.createExpression() ;
                action.setLeftOperand(variableExp) ;
                action.setRightOperand(actionExp) ;
                editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, element, FormPackage.Literals.WIDGET__ACTION, action));
            }
            operationViewer.setEditingDomain(getEditingDomain()) ;
            operationViewer.setEObject(element) ;
        }
    }


    public void dispose() {
        if(dataBinding!= null){
            dataBinding.dispose();
        }
        if(operationViewer != null){
            operationViewer.dispose() ;
        }
    }

    public String getLabel() {
        return Messages.outputOperation;
    }

    public boolean isRelevantFor(EObject eObject) {
        if(eObject instanceof FormField && !(eObject instanceof FileWidget) && ! ModelHelper.isInDuplicatedGrp(eObject)){
            Form form = ModelHelper.getForm((Widget) eObject);
            return !(form instanceof ViewForm);
        }else{
            return false;
        }

    }

    public void refresh() {
        if(operationViewer != null){
            operationViewer.refresh() ;
        }
    }

    public void setEObject(EObject object) {
        element = (Widget) object;
    }

    public void setEditingDomain(TransactionalEditingDomain editingDomain) {
        this.editingDomain = editingDomain;
    }

    public void setSelection(ISelection selection) {

    }

}
