/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.studio.scripting.provider;

import org.bonitasoft.studio.common.jface.databinding.validator.EmptyInputValidator;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.expression.editor.constant.ExpressionReturnTypeContentProvider;
import org.bonitasoft.studio.expression.editor.provider.IExpressionEditor;
import org.bonitasoft.studio.expression.editor.provider.SelectionAwareExpressionEditor;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionPackage;
import org.bonitasoft.studio.scripting.extensions.IScriptLanguageProvider;
import org.bonitasoft.studio.scripting.extensions.ScriptLanguageService;
import org.bonitasoft.studio.scripting.i18n.Messages;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogTray;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * @author Romain Bioteau
 *
 */
public class ScriptExpressionEditor extends SelectionAwareExpressionEditor implements IExpressionEditor {

    private final String languageId;
    private Composite mainComposite;
    private Text expressionNameText;
    private Combo expressionInterpreterCombo;
    private IExpressionEditor editor;
    private ComboViewer typeCombo;
    private Button browseClassesButton;
    private Expression inputExpression;
    public ScriptExpressionEditor(Expression expression) {
        if(expression.getInterpreter() == null || expression.getInterpreter().isEmpty()){
            expression.setInterpreter(ScriptLanguageService.getInstance().getDefaultLanguage()) ;
        }
        languageId = expression.getInterpreter() ;
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.expression.editor.provider.IExpressionEditor#createExpressionEditor(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public Control createExpressionEditor(Composite parent) {
        mainComposite = new Composite(parent,SWT.NONE) ;
        mainComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create()) ;
        GridLayout layout = new GridLayout(4, false) ;
        layout.marginBottom = 0 ;
        layout.marginHeight = 0 ;
        layout.marginWidth = 0 ;
        layout.marginTop = 10 ;
        layout.marginRight = 0 ;
        layout.marginLeft = 0 ;
        mainComposite.setLayout(layout) ;

        Label scriptNameLabel = new Label(mainComposite, SWT.NONE) ;
        scriptNameLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).create());
        scriptNameLabel.setText(Messages.name+" *") ;

        expressionNameText = new Text(mainComposite, SWT.BORDER | SWT.SINGLE) ;
        expressionNameText.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true,false).create()) ;

        Label interpreterLabel = new Label(mainComposite, SWT.NONE) ;
        interpreterLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).create());
        interpreterLabel.setText(Messages.interpreter) ;

        expressionInterpreterCombo = new Combo(mainComposite, SWT.READ_ONLY | SWT.BORDER) ;
        expressionInterpreterCombo.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true,false).create()) ;

        for(IScriptLanguageProvider provider : ScriptLanguageService.getInstance().getScriptLanguageProviders()){
            expressionInterpreterCombo.add(provider.getLanguageName()) ;
        }

        if(expressionInterpreterCombo.getItemCount() < 2){
            expressionInterpreterCombo.setEnabled(false);
        }

        IScriptLanguageProvider provider = ScriptLanguageService.getInstance().getScriptLanguageProvider(languageId) ;
        editor = provider.getExpressionEditor() ;

        Composite editorComposite = new Composite(mainComposite, SWT.NONE);
        editorComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true,true).span(4, 1).create()) ;
        layout = new GridLayout(1, false) ;
        layout.marginBottom = 0 ;
        layout.marginHeight = 0 ;
        layout.marginWidth = 0 ;
        layout.marginTop = 10 ;
        layout.marginRight = 0 ;
        layout.marginLeft = 0 ;
        editorComposite.setLayout(layout) ;
        editor.createExpressionEditor(editorComposite) ;

        createReturnTypeComposite(editorComposite);

        return mainComposite;
    }

    protected void createReturnTypeComposite(Composite parent) {
        Composite typeComposite = new Composite(parent,SWT.NONE) ;
        typeComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true,false).create()) ;
        GridLayout gl = new GridLayout(3,false) ;
        gl.marginWidth = 0 ;
        gl.marginHeight = 0 ;
        typeComposite.setLayout(gl) ;

        final Label typeLabel = new Label(typeComposite, SWT.NONE) ;
        typeLabel.setText(Messages.returnType) ;
        typeLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).create()) ;

        typeCombo = new ComboViewer(typeComposite, SWT.BORDER) ;
        typeCombo.getCombo().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).create()) ;
        typeCombo.setContentProvider(new ExpressionReturnTypeContentProvider()) ;
        typeCombo.setLabelProvider(new LabelProvider()) ;
        typeCombo.setSorter(new ViewerSorter(){
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                String t1 = (String) e1 ;
                String t2 = (String) e2 ;
                return t1.compareTo(t2);
            }
        }) ;
        typeCombo.getCombo().addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                fireSelectionChanged();
            }
        }) ;

        browseClassesButton = new Button(typeComposite, SWT.PUSH);
        browseClassesButton.setText(Messages.browse);
        browseClassesButton.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).create()) ;
        browseClassesButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                openClassSelectionDialog();
            }
        });
    }

    /**
     * @param classText
     */
    @SuppressWarnings("restriction")
    private void openClassSelectionDialog() {
        JavaSearchScope scope = new JavaSearchScope();
        try {
            scope.add(RepositoryManager.getInstance().getCurrentRepository().getJavaProject());
        } catch (Exception ex) {
            BonitaStudioLog.error(ex);
        }
        FilteredTypesSelectionDialog searchDialog = new FilteredTypesSelectionDialog(Display.getDefault().getActiveShell(), false, null, scope, IJavaSearchConstants.TYPE);
        if (searchDialog.open() == Dialog.OK) {
            String selectedTypeName = ((IType) searchDialog.getFirstResult()).getFullyQualifiedName();
            typeCombo.setInput(selectedTypeName) ;
            inputExpression.setReturnType(selectedTypeName) ;
        }
    }

    @Override
    public void bindExpression(EMFDataBindingContext dataBindingContext,EObject context, Expression inputExpression, ViewerFilter[] filters) {
        this.inputExpression = inputExpression;
        IObservableValue nameModelObservable = EMFObservables.observeValue(inputExpression, ExpressionPackage.Literals.EXPRESSION__NAME) ;
        IObservableValue interpreterModelObservable = EMFObservables.observeValue(inputExpression, ExpressionPackage.Literals.EXPRESSION__INTERPRETER) ;
        //        IObservableValue propagationModelObservable = EMFObservables.observeValue(inputExpression, ExpressionPackage.Literals.EXPRESSION__PROPAGATE_VARIABLE_CHANGE) ;

        UpdateValueStrategy opposite = new UpdateValueStrategy() ;
        opposite.setConverter(new Converter(Boolean.class,Boolean.class) {

            @Override
            public Object convert(Object fromObject) {
                return !(Boolean)fromObject;
            }
        }) ;

        UpdateValueStrategy targetToModel = new UpdateValueStrategy();
        targetToModel.setAfterConvertValidator(new EmptyInputValidator(Messages.name));
        ControlDecorationSupport.create(dataBindingContext.bindValue(SWTObservables.observeText(expressionNameText,SWT.Modify), nameModelObservable,targetToModel,null),SWT.LEFT) ;
        dataBindingContext.bindValue(SWTObservables.observeSelection(expressionInterpreterCombo), interpreterModelObservable) ;
        nameModelObservable.addValueChangeListener(new IValueChangeListener() {

            @Override
            public void handleValueChange(ValueChangeEvent arg0) {
                fireSelectionChanged() ;
            }
        }) ;

        editor.bindExpression(dataBindingContext, context, inputExpression,filters) ;

        if(inputExpression.getReturnType() != null){
            typeCombo.setInput(inputExpression.getReturnType()) ;
        }else{
            typeCombo.setInput(new Object()) ;
        }
        IObservableValue returnTypeModelObservable = EMFObservables.observeValue(inputExpression, ExpressionPackage.Literals.EXPRESSION__RETURN_TYPE) ;
        dataBindingContext.bindValue(ViewersObservables.observeSingleSelection(typeCombo), returnTypeModelObservable) ;
        dataBindingContext.bindValue(SWTObservables.observeText(typeCombo.getCombo()), returnTypeModelObservable) ;
        typeCombo.getCombo().setEnabled(!inputExpression.isReturnTypeFixed()) ;
        browseClassesButton.setEnabled(!inputExpression.isReturnTypeFixed()) ;
    }

    @Override
    public void addListener(Listener listener) {
        super.addListener(listener);
        editor.addListener(listener) ;
    }

    @Override
    public boolean canFinish() {
        return expressionNameText != null && !expressionNameText.isDisposed() && !expressionNameText.getText().isEmpty() && editor.canFinish() && typeCombo != null && !typeCombo.getCombo().isDisposed() && !typeCombo.getCombo().getText().trim().isEmpty();
    }

    @Override
    public void dispose() {
        super.dispose();
        if(editor != null){
            editor.dispose();
        }
    }

    @Override
    public void okPressed() {
        editor.okPressed();
    }

    @Override
    public boolean provideDialogTray() {
        return editor.provideDialogTray();
    }

    @Override
    public DialogTray createDialogTray() {
        return editor.createDialogTray();
    }

    @Override
    public Control getTextControl() {
        return editor.getTextControl();
    }

}
