/**
 * Copyright (C) 2012-2014 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.expression.editor.viewer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.common.IBonitaVariableContext;
import org.bonitasoft.studio.common.databinding.CustomEMFEditObservables;
import org.bonitasoft.studio.common.emf.tools.ExpressionHelper;
import org.bonitasoft.studio.common.emf.tools.WidgetHelper;
import org.bonitasoft.studio.common.extension.BonitaStudioExtensionRegistryManager;
import org.bonitasoft.studio.common.jface.SWTBotConstants;
import org.bonitasoft.studio.common.jface.databinding.validator.EmptyInputValidator;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.expression.editor.ExpressionEditorPlugin;
import org.bonitasoft.studio.expression.editor.autocompletion.AutoCompletionField;
import org.bonitasoft.studio.expression.editor.autocompletion.BonitaContentProposalAdapter;
import org.bonitasoft.studio.expression.editor.autocompletion.ExpressionProposal;
import org.bonitasoft.studio.expression.editor.autocompletion.IBonitaContentProposalListener2;
import org.bonitasoft.studio.expression.editor.autocompletion.IExpressionProposalLabelProvider;
import org.bonitasoft.studio.expression.editor.filter.ExpressionReturnTypeFilter;
import org.bonitasoft.studio.expression.editor.i18n.Messages;
import org.bonitasoft.studio.expression.editor.provider.ExpressionComparator;
import org.bonitasoft.studio.expression.editor.provider.ExpressionContentProvider;
import org.bonitasoft.studio.expression.editor.provider.ExpressionLabelProvider;
import org.bonitasoft.studio.expression.editor.provider.ExpressionTypeLabelProvider;
import org.bonitasoft.studio.expression.editor.provider.IExpressionNatureProvider;
import org.bonitasoft.studio.expression.editor.provider.IExpressionToolbarContribution;
import org.bonitasoft.studio.expression.editor.provider.IExpressionValidator;
import org.bonitasoft.studio.expression.editor.widget.ContentAssistText;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionFactory;
import org.bonitasoft.studio.model.expression.ExpressionPackage;
import org.bonitasoft.studio.model.form.Duplicable;
import org.bonitasoft.studio.model.form.TextFormField;
import org.bonitasoft.studio.model.form.Widget;
import org.bonitasoft.studio.model.process.Element;
import org.bonitasoft.studio.model.process.SearchIndex;
import org.bonitasoft.studio.pics.Pics;
import org.bonitasoft.studio.pics.PicsConstants;
import org.bonitasoft.studio.refactoring.core.AbstractRefactorOperation;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

/**
 * @author Romain Bioteau
 */
public class ExpressionViewer extends ContentViewer implements ExpressionConstants, SWTBotConstants,
        IContentProposalListener, IBonitaContentProposalListener2, IBonitaVariableContext, IExpressionValidationListener, IValueChangeListener {

    protected Composite control;
    private Text textControl;
    protected ToolItem editControl;
    private AutoCompletionField autoCompletion;
    protected EMFDataBindingContext internalDataBindingContext = new EMFDataBindingContext();
    protected final Set<ViewerFilter> filters;
    private String example;
    private ControlDecoration messageDecoration;
    protected String mandatoryFieldName;
    private ControlDecoration typeDecoration;
    //private final boolean editing = false;
    protected EObject context;
    private final List<ISelectionChangedListener> expressionEditorListener = new ArrayList<ISelectionChangedListener>();
    private boolean withConnector = false;
    private final List<IExpressionValidationListener> validationListeners = new ArrayList<IExpressionValidationListener>();
    private ToolItem eraseControl;
    private boolean isPageFlowContext = false;
    private boolean isOverviewContext = false;
    private AbstractRefactorOperation operation;
    private AbstractRefactorOperation removeOperation;
    final ExpressionViewerValidator expressionViewerValidator = new ExpressionViewerValidator();
    protected final DisposeListener disposeListener = new DisposeListener() {

        @Override
        public void widgetDisposed(final DisposeEvent e) {
            handleDispose(e);
        }
    };

    protected IExpressionNatureProvider expressionNatureProvider = new ExpressionContentProvider();
    protected DataBindingContext externalDataBindingContext;
    protected Binding expressionBinding;
    private final Map<Integer, String> messages = new HashMap<Integer, String>();
    protected ToolBar toolbar;
    private final List<IExpressionToolbarContribution> toolbarContributions = new ArrayList<IExpressionToolbarContribution>();
    protected boolean isPassword;
    private DefaultToolTip textTooltip;
    private IExpressionProposalLabelProvider expressionProposalLableProvider;
    private ContentAssistText contentAssistText;
    private ISelection selection;

    public ExpressionViewer(final Composite composite, final int style, final TabbedPropertySheetWidgetFactory widgetFactory) {
        this(composite, style, widgetFactory, null);
    }

    public ExpressionViewer(final Composite composite, final int style) {
        this(composite, style, null, null);
    }

    /**
     * @deprecated use ExpressionViewer(final Composite composite, final int style) instead
     * @param composite
     * @param style
     * @param expressionReference
     */
    @Deprecated
    public ExpressionViewer(final Composite composite, final int style, final EReference expressionReference) {
        this(composite, style, null, null, expressionReference);
    }

    /**
     * @deprecated use ExpressionViewer(final Composite composite, final int style,, final TabbedPropertySheetWidgetFactory widgetFactory) instead
     * @param composite
     * @param style
     * @param expressionReference
     */
    @Deprecated
    public ExpressionViewer(final Composite composite, final int style, final TabbedPropertySheetWidgetFactory widgetFactory,
            final EReference expressionReference) {
        this(composite, style, widgetFactory, null, expressionReference);
    }

    /**
     * @deprecated Editing domain is retrieve in the input EObject
     * @param composite
     * @param style
     * @param widgetFactory
     * @param editingDomain
     * @param expressionReference
     */
    @Deprecated
    public ExpressionViewer(final Composite composite, final int style, final TabbedPropertySheetWidgetFactory widgetFactory,
            final EditingDomain editingDomain, final EReference expressionReference) {
        this(composite, style, widgetFactory, editingDomain, expressionReference, false);
    }

    /**
     * @deprecated Editing domain is retrieve in the input EObject
     * @param composite
     * @param style
     * @param widgetFactory
     * @param editingDomain
     * @param expressionReference
     * @param withConnector
     */
    @Deprecated
    public ExpressionViewer(final Composite composite, final int style, final TabbedPropertySheetWidgetFactory widgetFactory,
            final EditingDomain editingDomain, final EReference expressionReference, final boolean withConnector) {
        Assert.isNotNull(composite, "composite");
        filters = new HashSet<ViewerFilter>();
        this.withConnector = withConnector;
        createControl(composite, style, widgetFactory);
        setContentProvider(new ArrayContentProvider());
        setLabelProvider(new ExpressionLabelProvider());
    }

    protected void createControl(final Composite composite, final int style, final TabbedPropertySheetWidgetFactory widgetFactory) {
        control = new Composite(composite, SWT.INHERIT_DEFAULT) {

            @Override
            public void setEnabled(final boolean enabled) {
                super.setEnabled(enabled);
                updateEnablement(enabled);
            }

        };
        if (widgetFactory != null) {
            widgetFactory.adapt(control);
        }
        control.addDisposeListener(disposeListener);
        control.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 0).spacing(0, 0).create());
        createTextControl(style, widgetFactory);
        createToolbar(style, widgetFactory);
    }

    protected void updateEnablement(final boolean enabled) {
        textControl.setEnabled(enabled);
        contentAssistText.setProposalEnabled(enabled);
        toolbar.setEnabled(enabled);
        if (enabled) {
            typeDecoration.show();
        } else {
            typeDecoration.hide();
        }
    }

    protected void createToolbar(final int style, final TabbedPropertySheetWidgetFactory widgetFactory) {
        toolbar = new ToolBar(control, SWT.FLAT | SWT.NO_FOCUS);
        toolbar.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).create());
        editControl = createEditToolItem(toolbar);
        if (withConnector) {
            createToolBarExtension(toolbar);
        }
        createEraseToolItem(toolbar);
        if (widgetFactory != null) {
            widgetFactory.adapt(toolbar, true, true);
        }
    }

    protected void createToolBarExtension(final ToolBar toolbar) {
        for (final IConfigurationElement elem : BonitaStudioExtensionRegistryManager.getInstance().getConfigurationElements(
                "org.bonitasoft.studio.expression.editor.expressionViewerToolbar")) {
            try {
                final IExpressionToolbarContribution item = (IExpressionToolbarContribution) elem
                        .createExecutableExtension("class");
                if (item.getId() == "ConnectorInExpressionViewer" && withConnector) {
                    item.fill(toolbar, -1);
                    item.setExpressionViewer(this);
                }
                toolbarContributions.add(item);
            } catch (final CoreException e) {
                BonitaStudioLog.error(e, ExpressionEditorPlugin.PLUGIN_ID);
            }
        }
    }

    protected ToolItem createEditToolItem(final ToolBar tb) {
        final ToolItem editControl = new ToolItem(tb, SWT.PUSH | SWT.NO_FOCUS);
        editControl.setImage(Pics.getImage(PicsConstants.edit));
        editControl.setToolTipText(Messages.editAndContinue);

        /* For test purpose */
        editControl.setData(SWTBOT_WIDGET_ID_KEY, SWTBOT_ID_EDITBUTTON);
        editControl.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                boolean connectorEdit = false;
                final Expression selectedExpression = getSelectedExpression();
                if (tb != null && withConnector && selectedExpression != null && ExpressionConstants.CONNECTOR_TYPE.equals(selectedExpression.getType())) {
                    for (final ToolItem ti : tb.getItems()) {
                        final Object data = ti.getData(SWTBotConstants.SWTBOT_WIDGET_ID_KEY);
                        if (data != null && data.equals(SWTBotConstants.SWTBOT_ID_CONNECTORBUTTON)) {
                            connectorEdit = true;
                            ti.notifyListeners(SWT.Selection, event);
                        }
                    }
                }
                if (!connectorEdit) {
                    final EditExpressionDialog dialog = createEditDialog();
                    openEditDialog(dialog);
                }
            }
        });

        editControl.addDisposeListener(disposeListener);
        return editControl;
    }

    protected ToolItem createEraseToolItem(final ToolBar tb) {
        eraseControl = new ToolItem(tb, SWT.PUSH | SWT.NO_FOCUS);
        eraseControl.setImage(Pics.getImage(PicsConstants.clear));
        eraseControl.setToolTipText(Messages.eraseExpression);

        /* For test purpose */
        eraseControl.setData(SWTBOT_WIDGET_ID_KEY, SWTBOT_ID_ERASEBUTTON);
        eraseControl.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                erase(getSelectedExpression());
            }

        });

        eraseControl.addDisposeListener(disposeListener);
        return eraseControl;
    }

    protected void erase(final Expression selectedExpression) {
        String type = selectedExpression.getType();
        if (ExpressionConstants.SCRIPT_TYPE.equals(type)) {
            if (!MessageDialog.openConfirm(Display.getDefault().getActiveShell(), Messages.cleanExpressionTitle,
                    Messages.cleanExpressionMsg)) {
                return;
            }
        }
        if (!ExpressionConstants.CONDITION_TYPE.equals(type)) {
            type = ExpressionConstants.CONSTANT_TYPE;
        }
        clearExpression(type, selectedExpression);

        //        final IObservableValue nameObservable = (IObservableValue) expressionBinding.getTarget();
        //        nameObservable.setValue("");

        validateExternalDatabindingContextTargets(externalDataBindingContext);
        validate();
        refresh();
    }

    protected void validateExternalDatabindingContextTargets(final DataBindingContext dbc) {
        if (dbc != null) {
            final IObservableList bindings = dbc.getBindings();
            final Iterator iterator = bindings.iterator();
            while (iterator.hasNext()) {
                final Binding binding = (Binding) iterator.next();
                binding.validateTargetToModel();
            }
        }
    }

    private void clearExpression(final String type, final Expression selectedExpression) {
        final EditingDomain editingDomain = getEditingDomain();
        if (editingDomain != null) {
            final CompoundCommand cc = ExpressionHelper.clearExpression(selectedExpression, type, editingDomain);
            final boolean hasBeenExecuted = executeRemoveOperation(cc);
            if (!hasBeenExecuted) {
                editingDomain.getCommandStack().execute(cc);
            }
        } else {
            ExpressionHelper.clearExpression(selectedExpression);
        }
    }

    public void setExpressionProposalLableProvider(final IExpressionProposalLabelProvider expressionProposalLableProvider) {
        this.expressionProposalLableProvider = expressionProposalLableProvider;
        if (autoCompletion != null) {
            autoCompletion.setExpressionProposalLabelProvider(expressionProposalLableProvider);
        }
    }

    public ContentAssistText getContentAssistText() {
        return contentAssistText;
    }

    protected void createTextControl(final int style, final TabbedPropertySheetWidgetFactory widgetFactory) {
        if (expressionProposalLableProvider == null) {
            expressionProposalLableProvider = new ExpressionLabelProvider();
        }
        contentAssistText = new ContentAssistText(control, expressionProposalLableProvider, style);
        textControl = contentAssistText.getTextControl();
        if (widgetFactory != null) {
            widgetFactory.adapt(textControl, false, false);
        }
        textControl.addDisposeListener(disposeListener);
        textTooltip = new DefaultToolTip(textControl) {

            @Override
            protected boolean shouldCreateToolTip(final Event event) {
                return super.shouldCreateToolTip(event) && getText(event) != null;
            }
        };
        textTooltip.setShift(new Point(5, 5));
        textTooltip.setRespectMonitorBounds(true);
        textTooltip.setPopupDelay(100);

        typeDecoration = new ControlDecoration(contentAssistText.getToolbar(), SWT.LEFT, control);
        typeDecoration.setMarginWidth(0);

        messageDecoration = new ControlDecoration(contentAssistText, SWT.LEFT, control);
        messageDecoration.setShowHover(true);
        messageDecoration.setMarginWidth(1);
        messageDecoration.hide();

        contentAssistText.addContentAssistListener(this);
        autoCompletion = contentAssistText.getAutocompletion();
        autoCompletion.addExpressionProposalListener(this);

        int indent = 0;
        if ((style & SWT.PASSWORD) != 0) {
            isPassword = true;
        }
        if ((style & SWT.BORDER) != 0) {
            indent = 16;
        }
        contentAssistText.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).indent(indent, 0)
                .grab(true, false).create());
    }

    protected void openEditDialog(final EditExpressionDialog dialog) {
        dialog.setIsPageFlowContext(isPageFlowContext);
        if (dialog.open() == Dialog.OK) {
            final Expression newExpression = dialog.getExpression();
            executeOperation(newExpression.getName());
            updateSelection(new CompoundCommand(), newExpression);
            //    setSelection(new StructuredSelection(selectedExpression));
            final Expression selectedExpression = getSelectedExpression();
            final EditingDomain editingDomain = getEditingDomain();
            if (editingDomain == null) {
                selectedExpression.setReturnType(newExpression.getReturnType());
                selectedExpression.setType(newExpression.getType());
            } else {
                final CompoundCommand cc = new CompoundCommand();
                cc.append(
                        SetCommand.create(editingDomain, selectedExpression,
                                ExpressionPackage.Literals.EXPRESSION__RETURN_TYPE, newExpression.getReturnType()));
                cc.append(
                        SetCommand.create(editingDomain, selectedExpression, ExpressionPackage.Literals.EXPRESSION__TYPE,
                                newExpression.getType()));
                editingDomain.getCommandStack().execute(cc);
            }

            refresh();
            fireExpressionEditorChanged(new SelectionChangedEvent(this, new StructuredSelection(selectedExpression)));
        }
    }

    protected EditingDomain getEditingDomain() {
        final Expression selectedExpression = getSelectedExpression();
        if (selectedExpression != null && selectedExpression.eResource() != null) {
            return TransactionUtil.getEditingDomain(selectedExpression);
        }
        return null;
    }

    protected EditExpressionDialog createEditDialog() {
        final Object input = getInput();
        final EObject editInput = getEditInput(input);
        final EditExpressionDialog dialog = createEditDialog(editInput);
        return dialog;
    }

    private EObject getEditInput(final Object input) {
        EObject editInput = context;
        if (input != null && editInput == null) {
            if (input instanceof EObject) {
                editInput = (EObject) input;
            } else {
                editInput = (EObject) ((IObservableValue) input).getValue();
            }
        }
        return editInput;
    }

    protected EditExpressionDialog createEditDialog(final EObject editInput) {
        return new EditExpressionDialog(control.getShell(), isPassword, EcoreUtil.copy(getSelectedExpression()), editInput,
                getEditingDomain(), filters.toArray(new ViewerFilter[filters.size()]), this);
    }

    protected void fireExpressionEditorChanged(final SelectionChangedEvent selectionChangedEvent) {
        for (final ISelectionChangedListener listener : expressionEditorListener) {
            listener.selectionChanged(selectionChangedEvent);
        }
    }

    @Override
    public Control getControl() {
        return control;
    }

    public ToolBar getToolbar() {
        return toolbar;
    }

    @Override
    public ISelection getSelection() {
        return selection != null ? selection : new StructuredSelection();
    }

    public BonitaContentProposalAdapter getContentProposal() {
        return autoCompletion.getContentProposalAdapter();
    }

    @Override
    public void refresh() {
        if (!getSelection().isEmpty()) {
            internalRefresh();
        }
    }

    private Image getLabelProviderImage(final ILabelProvider labelProvider, final Object input) {
        return labelProvider.getImage(input);
    }

    @Override
    public void setSelection(final ISelection selection, final boolean reveal) {
        Assert.isLegal(selection instanceof IStructuredSelection);
        if (!selection.equals(this.selection)) {
            this.selection = selection;
            final Expression selectedExpression = getSelectedExpression();
            if (selectedExpression != null) {
                bindExpression();
                fireSelectionChanged(new SelectionChangedEvent(this, selection));
                for (final IExpressionToolbarContribution contribution : toolbarContributions) {
                    contribution.setExpression(selectedExpression);
                }
                updateAutoCompletionContentProposalAdapter();
                validate();
            }
        }
    }

    private void updateAutoCompletionContentProposalAdapter() {
        if (ExpressionConstants.CONDITION_TYPE.equals(getSelectedExpression().getType())) {
            autoCompletion.getContentProposalAdapter().setEnabled(false);
            autoCompletion.getContentProposalAdapter().setProposalAcceptanceStyle(
                    ContentProposalAdapter.PROPOSAL_INSERT);
        } else {
            autoCompletion.getContentProposalAdapter().setEnabled(true);
            autoCompletion.getContentProposalAdapter().setProposalAcceptanceStyle(
                    ContentProposalAdapter.PROPOSAL_REPLACE);
        }
    }

    protected void updateSelection(final CompoundCommand cc, final Expression expression) {
        new ExpressionSynchronizer(getEditingDomain(), expression, getSelectedExpression()).synchronize(cc);
        refresh();
    }

    @Override
    protected void inputChanged(Object input, final Object oldInput) {
        if (input instanceof IObservableValue) {
            input = ((IObservableValue) input).getValue();
        }
        if (input != null) {
            manageNatureProviderAndAutocompletionProposal(input);
        }

    }

    public void manageNatureProviderAndAutocompletionProposal(final Object input) {
        if (expressionNatureProvider != null) {
            if (context == null) {
                expressionNatureProvider.setContext((EObject) input);
            } else {
                expressionNatureProvider.setContext(context);
            }
        }
        final Expression selectedExpression = getSelectedExpression();
        if (selectedExpression != null && ExpressionConstants.CONDITION_TYPE.equals(selectedExpression.getType())) {
            setProposalsFiltering(false);
            autoCompletion.getContentProposalAdapter().setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);
        } else {
            autoCompletion.getContentProposalAdapter().setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
        }
        autoCompletion.setContext(expressionNatureProvider.getContext());
        final Set<Expression> filteredExpressions = getFilteredExpressions();
        autoCompletion.setProposals(filteredExpressions.toArray(new Expression[filteredExpressions.size()]));

        final ArrayList<String> filteredExpressionType = getFilteredExpressionType();
        autoCompletion.setFilteredExpressionType(filteredExpressionType);
        if (filteredExpressionType.contains(ExpressionConstants.VARIABLE_TYPE)
                && filteredExpressionType.contains(ExpressionConstants.PARAMETER_TYPE)
                && filteredExpressions.isEmpty()) {
            contentAssistText.setProposalEnabled(false);
        }
        else {
            contentAssistText.setProposalEnabled(true);
        }
    }

    protected Expression getSelectedExpression() {
        return (Expression) ((IStructuredSelection) getSelection()).getFirstElement();
    }

    private ArrayList<String> getFilteredExpressionType() {
        final ArrayList<String> filteredExpressions = new ArrayList<String>();
        final Set<ViewerFilter> fitlers = getFilters();
        final EObject input = expressionNatureProvider.getContext();

        final Expression exp = ExpressionFactory.eINSTANCE.createExpression();
        exp.setName("");
        if (filters != null && expressionNatureProvider != null && input != null) {
            for (final ViewerFilter viewerFilter : fitlers) {
                exp.setType(ExpressionConstants.VARIABLE_TYPE);
                if (!viewerFilter.select(this, input, exp)) {
                    filteredExpressions.add(ExpressionConstants.VARIABLE_TYPE);
                }
                exp.setType(ExpressionConstants.PARAMETER_TYPE);
                if (!viewerFilter.select(this, input, exp)) {
                    filteredExpressions.add(ExpressionConstants.PARAMETER_TYPE);
                }
                exp.setType(ExpressionConstants.CONSTANT_TYPE);
                if (!viewerFilter.select(this, input, exp)) {
                    filteredExpressions.add(ExpressionConstants.CONSTANT_TYPE);
                }
                exp.setType(ExpressionConstants.DOCUMENT_TYPE);
                if (!viewerFilter.select(this, input, exp)) {
                    filteredExpressions.add(ExpressionConstants.DOCUMENT_TYPE);
                }
                exp.setType(ExpressionConstants.DOCUMENT_REF_TYPE);
                if (!viewerFilter.select(this, input, exp)) {
                    filteredExpressions.add(ExpressionConstants.DOCUMENT_REF_TYPE);
                }
            }
        }
        return filteredExpressions;
    }

    public void setExpressionNatureProvider(final IExpressionNatureProvider expressionNatureProvider) {
        this.expressionNatureProvider = expressionNatureProvider;
    }

    protected Set<Expression> getFilteredExpressions() {
        final Set<Expression> filteredExpressions = new TreeSet<Expression>(new ExpressionComparator());
        if (expressionNatureProvider != null) {
            if (!(expressionNatureProvider instanceof ExpressionContentProvider)) {
                final ExpressionContentProvider provider = new ExpressionContentProvider();
                if (context == null) {
                    provider.setContext((EObject) getInput());
                } else {
                    provider.setContext(context);
                }
                final Expression[] expressions = provider.getExpressions();
                if (expressions != null) {
                    filteredExpressions.addAll(Arrays.asList(expressions));
                }
            }
            final Expression[] expressions = expressionNatureProvider.getExpressions();
            EObject input = expressionNatureProvider.getContext();
            if (input == null) {
                if (getInput() instanceof EObject) {
                    input = (EObject) getInput();
                }
            }
            if (expressions != null) {
                filteredExpressions.addAll(Arrays.asList(expressions));
            }
            final Set<Expression> toRemove = new HashSet<Expression>();
            if (input != null) {
                for (final Expression exp : filteredExpressions) {
                    for (final ViewerFilter filter : getFilters()) {
                        if (filter != null && !filter.select(this, input, exp)) {
                            toRemove.add(exp);
                        }
                    }
                    final Expression selectedExpression = getSelectedExpression();
                    if (selectedExpression != null
                            && !ExpressionConstants.CONDITION_TYPE.equals(selectedExpression.getType())) {
                        if (selectedExpression != null && selectedExpression.isReturnTypeFixed()
                                && selectedExpression.getReturnType() != null) {
                            if (!compatibleReturnTypes(selectedExpression, exp)) {
                                toRemove.add(exp);
                            }
                        }
                    }
                }
            }
            filteredExpressions.removeAll(toRemove);
        }
        return filteredExpressions;
    }

    protected boolean compatibleReturnTypes(final Expression currentExpression, final Expression targetExpression) {
        final String currentReturnType = currentExpression.getReturnType();
        final String targetReturnType = targetExpression.getReturnType();
        return new ExpressionReturnTypeFilter().compatibleReturnTypes(currentReturnType, targetReturnType);
    }

    protected Set<ViewerFilter> getFilters() {
        return filters;
    }

    protected void bindExpression() {
        if (expressionBinding != null && externalDataBindingContext != null) {
            externalDataBindingContext.removeBinding(expressionBinding);
            expressionBinding.dispose();
        }

        final IObservableValue nameObservable = getExpressionNameObservable();
        final IObservableValue typeObservable = getExpressionTypeObservable();
        final IObservableValue returnTypeObservable = getExpressionReturnTypeObservable();
        final IObservableValue contentObservable = getExpressionContentObservable();

        nameObservable.addValueChangeListener(this);
        typeObservable.addValueChangeListener(this);
        contentObservable.addValueChangeListener(this);
        returnTypeObservable.addValueChangeListener(this);

        final UpdateValueStrategy targetToModelNameStrategy = new UpdateValueStrategy();
        if (mandatoryFieldName != null) {
            targetToModelNameStrategy.setBeforeSetValidator(new EmptyInputValidator(mandatoryFieldName));
        }
        targetToModelNameStrategy.setConverter(getNameConverter());

        final ISWTObservableValue observeDelayedValue = SWTObservables.observeDelayedValue(500,
                SWTObservables.observeText(textControl, SWT.Modify));
        expressionBinding = internalDataBindingContext.bindValue(observeDelayedValue, nameObservable,
                targetToModelNameStrategy, null);
        bindEditableText(typeObservable);
        if (externalDataBindingContext != null) {
            externalDataBindingContext.addBinding(expressionBinding);
            externalDataBindingContext.addValidationStatusProvider(expressionViewerValidator);
        }
    }

    protected IObservableValue getExpressionNameObservable() {
        IObservableValue nameObservable;
        final EditingDomain editingDomain = getEditingDomain();
        if (editingDomain != null) {
            nameObservable = CustomEMFEditObservables.observeDetailValue(Realm.getDefault(), getSelectedExpressionObservable(),
                    ExpressionPackage.Literals.EXPRESSION__NAME);
        } else {
            nameObservable = EMFObservables.observeDetailValue(Realm.getDefault(), getSelectedExpressionObservable(),
                    ExpressionPackage.Literals.EXPRESSION__NAME);
        }
        return nameObservable;
    }

    protected IObservableValue getExpressionTypeObservable() {
        IObservableValue nameObservable;
        final EditingDomain editingDomain = getEditingDomain();
        if (editingDomain != null) {
            nameObservable = CustomEMFEditObservables.observeDetailValue(Realm.getDefault(), getSelectedExpressionObservable(),
                    ExpressionPackage.Literals.EXPRESSION__TYPE);
        } else {
            nameObservable = EMFObservables.observeDetailValue(Realm.getDefault(), getSelectedExpressionObservable(),
                    ExpressionPackage.Literals.EXPRESSION__TYPE);
        }
        return nameObservable;
    }

    protected IObservableValue getExpressionReturnTypeObservable() {
        IObservableValue returnTypeObservable;
        final EditingDomain editingDomain = getEditingDomain();
        if (editingDomain != null) {
            returnTypeObservable = CustomEMFEditObservables.observeDetailValue(Realm.getDefault(), getSelectedExpressionObservable(),
                    ExpressionPackage.Literals.EXPRESSION__RETURN_TYPE);
        } else {
            returnTypeObservable = EMFObservables.observeDetailValue(Realm.getDefault(), getSelectedExpressionObservable(),
                    ExpressionPackage.Literals.EXPRESSION__RETURN_TYPE);
        }
        return returnTypeObservable;
    }

    protected IObservableValue getExpressionContentObservable() {
        IObservableValue returnContentObservable;
        final EditingDomain editingDomain = getEditingDomain();
        if (editingDomain != null) {
            returnContentObservable = CustomEMFEditObservables.observeDetailValue(Realm.getDefault(), getSelectedExpressionObservable(),
                    ExpressionPackage.Literals.EXPRESSION__CONTENT);
        } else {
            returnContentObservable = EMFObservables.observeDetailValue(Realm.getDefault(), getSelectedExpressionObservable(),
                    ExpressionPackage.Literals.EXPRESSION__CONTENT);
        }
        return returnContentObservable;
    }

    private IObservableValue getSelectedExpressionObservable() {
        return ViewersObservables.observeSingleSelection(this);
    }

    protected void bindEditableText(final IObservableValue typeObservable) {
        final UpdateValueStrategy modelToTargetTypeStrategy = new UpdateValueStrategy();
        modelToTargetTypeStrategy.setConverter(new Converter(String.class, Boolean.class) {

            @Override
            public Object convert(final Object from) {
                final boolean isScriptType = ExpressionConstants.SCRIPT_TYPE.equals(from.toString());
                final boolean isConnectorType = from.toString().equals(ExpressionConstants.CONNECTOR_TYPE);
                final boolean isXPathType = from.toString().equals(ExpressionConstants.XPATH_TYPE);
                final boolean isJavaType = from.toString().equals(ExpressionConstants.JAVA_TYPE);
                final boolean isQueryType = from.toString().equals(ExpressionConstants.QUERY_TYPE);
                if (isScriptType) {
                    textTooltip.setText(Messages.editScriptExpressionTooltip);
                } else if (isXPathType) {
                    textTooltip.setText(Messages.editXpathExpressionTooltip);
                } else if (isJavaType) {
                    textTooltip.setText(Messages.editJavaExpressionTooltip);
                } else if (isConnectorType) {
                    textTooltip.setText(Messages.editConnectorExpressionTooltip);
                } else if (isQueryType) {
                    textTooltip.setText(Messages.editQueryExpressionTooltip);
                } else {
                    textTooltip.setText(null);
                }
                return !(isScriptType || isConnectorType || isJavaType || isXPathType || isQueryType);
            }

        });

        internalDataBindingContext.bindValue(SWTObservables.observeEditable(textControl), typeObservable,
                new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTargetTypeStrategy);
    }

    protected void updateContent(final String newContent) {
        final Expression selectedExpression = getSelectedExpression();
        if (newContent != null && !newContent.equals(selectedExpression.getContent())) {
            final EditingDomain editingDomain = getEditingDomain();
            if (editingDomain != null) {
                editingDomain.getCommandStack().execute(
                        SetCommand.create(editingDomain, selectedExpression, ExpressionPackage.Literals.EXPRESSION__CONTENT,
                                newContent));
            } else {
                selectedExpression.setContent(newContent);
            }
        }
    }

    protected void updateContentType(final String newContentType) {
        final Expression selectedExpression = getSelectedExpression();
        if (!newContentType.equals(selectedExpression.getType())) {
            final EditingDomain editingDomain = getEditingDomain();
            if (editingDomain != null) {
                editingDomain.getCommandStack().execute(
                        SetCommand.create(editingDomain, selectedExpression, ExpressionPackage.Literals.EXPRESSION__TYPE,
                                newContentType));
            } else {
                selectedExpression.setType(newContentType);
            }
            updateInterpreter(newContentType);
        }
    }

    private void updateInterpreter(final String newContentType) {
        final Expression selectedExpression = getSelectedExpression();
        if (!ExpressionConstants.SCRIPT_TYPE.equals(newContentType)) {
            final EditingDomain editingDomain = getEditingDomain();
            if (editingDomain != null) {
                editingDomain.getCommandStack().execute(
                        SetCommand.create(editingDomain, selectedExpression,
                                ExpressionPackage.Literals.EXPRESSION__INTERPRETER, null));
            } else {
                selectedExpression.setInterpreter(null);
            }
        }
    }

    protected String getContentFromInput(final String input) {
        final Expression selectedExpression = getSelectedExpression();
        final String selectedExpressionType = selectedExpression.getType();
        if (ExpressionConstants.SCRIPT_TYPE.equals(selectedExpressionType)
                || ExpressionConstants.PATTERN_TYPE.equals(selectedExpressionType)
                || ExpressionConstants.XPATH_TYPE.equals(selectedExpressionType)
                || ExpressionConstants.JAVA_TYPE.equals(selectedExpressionType)
                || ExpressionConstants.QUERY_TYPE.equals(selectedExpressionType)) {
            return selectedExpression.getContent(); // NO CONTENT UPDATE WHEN
            // THOSES TYPES
        }

        final Set<String> cache = new HashSet<String>();
        for (final Expression e : getFilteredExpressions()) {
            if (e.getName() != null && e.getName().equals(input)) {
                cache.add(e.getContent());
            }
        }
        if (cache.size() > 1) {
            for (final String content : cache) {
                if (content.equals(selectedExpression.getContent())) {
                    return content;
                }
            }
            return cache.iterator().next();
        } else if (cache.size() == 1) {
            return cache.iterator().next();
        }
        return input;
    }

    protected String getContentTypeFromInput(final String input) {
        final Expression selectedExpression = getSelectedExpression();
        Assert.isNotNull(selectedExpression);
        String expressionType = selectedExpression.getType();
        if (input.equals(selectedExpression.getName()) && CONSTANT_TYPE.equals(expressionType)) {
            return expressionType;
        }
        if (selectedExpression.getType() == null) {
            expressionType = CONSTANT_TYPE;
        }
        if (ExpressionConstants.SCRIPT_TYPE.equals(expressionType)) {
            return ExpressionConstants.SCRIPT_TYPE;
        } else if (ExpressionConstants.CONDITION_TYPE.equals(expressionType)) {
            return ExpressionConstants.CONDITION_TYPE;
        } else if (ExpressionConstants.CONNECTOR_TYPE.equals(expressionType)) {
            return ExpressionConstants.CONNECTOR_TYPE;
        } else if (ExpressionConstants.PATTERN_TYPE.equals(expressionType)) {
            return ExpressionConstants.PATTERN_TYPE;
        } else if (ExpressionConstants.JAVA_TYPE.equals(expressionType)) {
            return ExpressionConstants.JAVA_TYPE;
        } else if (ExpressionConstants.XPATH_TYPE.equals(expressionType)) {
            return ExpressionConstants.XPATH_TYPE;
        } else if (ExpressionConstants.URL_ATTRIBUTE_TYPE.equals(expressionType)) {
            return ExpressionConstants.URL_ATTRIBUTE_TYPE;
        } else if (ExpressionConstants.SEARCH_INDEX_TYPE.equals(expressionType)) {
            return ExpressionConstants.SEARCH_INDEX_TYPE;
        } else if (ExpressionConstants.QUERY_TYPE.equals(expressionType)) {
            return ExpressionConstants.QUERY_TYPE;
        }

        final Set<String> cache = new HashSet<String>();
        for (final Expression e : getFilteredExpressions()) {
            if (e.getName() != null && e.getName().equals(input)) {
                cache.add(e.getType());
            }
        }
        if (cache.size() > 1) {
            for (final String type : cache) {
                if (type.equals(selectedExpression.getType())) {
                    return type;
                }
            }
            return cache.iterator().next();
        } else if (cache.size() == 1) {
            return cache.iterator().next();
        } else {
            expressionType = CONSTANT_TYPE;
        }

        return expressionType;
    }

    protected void internalRefresh() {
        final Control composite = getControl();
        if (!composite.isDisposed()) {
            refreshTypeDecoration();
            refreshMessageDecoration();
        }
    }

    private void refreshTypeDecoration() {
        updateTypeDecorationIcon();
        updateTypeDecorationDescriptionText();
        updateTypeDecorationVisibility();
    }

    private void updateTypeDecorationIcon() {
        final ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
        final Image icon = getLabelProviderImage(labelProvider, getSelectedExpression());
        typeDecoration.setImage(icon);
    }

    private void updateTypeDecorationDescriptionText() {
        final ExpressionTypeLabelProvider expTypeProvider = new ExpressionTypeLabelProvider();
        final String desc = expTypeProvider.getText(getSelectedExpression().getType());
        typeDecoration.setDescriptionText(desc);
    }

    private void updateTypeDecorationVisibility() {
        if (getSelectedExpression().getName() == null || getSelectedExpression().getName().isEmpty()) {
            if (!ExpressionConstants.CONDITION_TYPE.equals(getSelectedExpression().getType())) {
                if (typeDecoration.isVisible()) {
                    typeDecoration.hide();
                }
            }
        } else {
            if (!typeDecoration.isVisible()) {
                typeDecoration.show();
            }
        }

    }

    private void refreshMessageDecoration() {
        final Entry<Integer, String> message = getMessageToDisplay();
        if (message != null) {
            messageDecoration.setDescriptionText(message.getValue());
            if (message.getKey() == IStatus.INFO) {
                // Issue with focus
                messageDecoration.setShowOnlyOnFocus(false);
            } else {
                messageDecoration.setShowOnlyOnFocus(false);
            }

            final Image icon = getImageForMessageKind(message.getKey());
            if (icon != null) {
                messageDecoration.setImage(icon);
            }

            messageDecoration.show();

        } else {
            messageDecoration.hide();
        }
    }

    private Image getImageForMessageKind(final Integer messageKind) {
        switch (messageKind) {
            case IStatus.WARNING:
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
            case IStatus.INFO:
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
            case IStatus.ERROR:
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
            default:
                break;
        }

        return null;
    }

    private Entry<Integer, String> getMessageToDisplay() {
        Entry<Integer, String> errorEntry = null;
        Entry<Integer, String> warningEntry = null;
        Entry<Integer, String> infoEntry = null;
        for (final Entry<Integer, String> entry : messages.entrySet()) {
            if (entry.getKey() == IStatus.ERROR && entry.getValue() != null) {
                errorEntry = entry;
            } else if (entry.getKey() == IStatus.WARNING && entry.getValue() != null) {
                warningEntry = entry;
            } else if (entry.getKey() == IStatus.INFO && entry.getValue() != null) {
                infoEntry = entry;
            }
        }
        if (errorEntry != null) {
            return errorEntry;
        } else if (warningEntry != null) {
            return warningEntry;
        } else if (infoEntry != null) {
            return infoEntry;
        } else {
            return null;
        }
    }

    @Override
    public void setLabelProvider(final IBaseLabelProvider labelProvider) {
        Assert.isTrue(labelProvider instanceof ExpressionLabelProvider);
        super.setLabelProvider(labelProvider);
    }

    public Text getTextControl() {
        return textControl;
    }

    public ToolItem getButtonControl() {
        return editControl;
    }

    public void addFilter(final ViewerFilter viewerFilter) {
        filters.add(viewerFilter);
    }

    public void removeFilter(final ViewerFilter viewerFilter) {
        filters.remove(viewerFilter);
    }

    public String getExample() {
        return example;
    }

    public void setExample(final String example) {
        this.example = example;
        textControl.setMessage(example);
        textControl.redraw();
    }

    public String getMessage(final int messageKind) {
        return messages.get(messageKind);
    }

    public void setMessage(String message, final int messageKind) {
        if (IStatus.OK == messageKind) {
            messages.remove(IStatus.ERROR);
            messages.remove(IStatus.WARNING);
        } else {
            if (messageKind == IStatus.WARNING) {
                messages.remove(IStatus.ERROR);
            }
            if (messageKind == IStatus.INFO) {
                messages.remove(IStatus.ERROR);
                messages.remove(IStatus.WARNING);
            }
            if(message != null && message.isEmpty()){
                message = null;
            }
            messages.put(messageKind, message);
        }
        refresh();
    }

    public void removeMessages(final int messageKind) {
        if (messageKind == IStatus.INFO) {
            messages.remove(IStatus.INFO);
        } else if (messageKind == IStatus.ERROR) {
            messages.remove(IStatus.ERROR);
        } else if (messageKind == IStatus.WARNING) {
            messages.remove(IStatus.WARNING);
        }
        refresh();
    }


    @Override
    protected void handleDispose(final DisposeEvent event) {
        if (expressionBinding != null && externalDataBindingContext != null) {
            externalDataBindingContext.removeBinding(expressionBinding);
            expressionBinding.dispose();
        }
        if (internalDataBindingContext != null) {
            internalDataBindingContext.dispose();
        }
        super.handleDispose(event);
    }

    public void setEditingDomain(final EditingDomain editingDomain) {
        // this.editingDomain = editingDomain;
    }

    public void setMandatoryField(final String fieldName, final DataBindingContext dbc) {
        mandatoryFieldName = fieldName;
        externalDataBindingContext = dbc;
    }

    public void addExpressionEditorChangedListener(final ISelectionChangedListener iSelectionChangedListener) {
        expressionEditorListener.add(iSelectionChangedListener);
    }

    public void setContext(final EObject context) {
        this.context = context;
    }

    public void updateAutocompletionProposals() {
        if (expressionNatureProvider != null) {
            final Set<Expression> filteredExpressions = getFilteredExpressions();
            autoCompletion.setProposals(filteredExpressions.toArray(new Expression[filteredExpressions.size()]));
        }
    }

    public void setProposalsFiltering(final boolean filterProposal) {
        autoCompletion.getContentProposalProvider().setFiltering(filterProposal);
    }

    public ToolItem getEraseControl() {
        return eraseControl;
    }

    protected Converter getNameConverter() {
        final Converter nameConverter = new Converter(String.class, String.class) {

            @Override
            public Object convert(final Object fromObject) {
                final int caretPosition = textControl.getCaretPosition();
                final String input = (String) fromObject;
                updateContentType(getContentTypeFromInput(input));
                updateContent(getContentFromInput(input));
                final boolean hasBeenExecuted = executeOperation(input);
                refresh();
                if (hasBeenExecuted) {
                    textControl.setSelection(caretPosition, caretPosition);
                }
                return fromObject;
            }
        };
        return nameConverter;
    }

    public void addExpressionValidator(final IExpressionValidator comaprisonExpressionValidator) {
        expressionViewerValidator.addValidator(comaprisonExpressionValidator);
    }

    public void addExpressionValidationListener(final IExpressionValidationListener listener) {
        if (!validationListeners.contains(listener)) {
            validationListeners.add(listener);
        }
    }

    public void validate() {
        expressionViewerValidator.setContext(context);
        expressionViewerValidator.setExpression(getSelectedExpression());
        expressionViewerValidator.addValidationsStatusChangedListener(this);
        for(final IExpressionValidationListener l : validationListeners){
            expressionViewerValidator.addValidationsStatusChangedListener(l);
        }
        final Expression selectedExpression = getSelectedExpression();
        if (selectedExpression != null) {
            expressionViewerValidator.validate(selectedExpression.getName());
        }
    }


    public void setExternalDataBindingContext(final DataBindingContext ctx) {
        externalDataBindingContext = ctx;
    }

    @Override
    public void proposalAccepted(final IContentProposal proposal) {
        final int proposalAcceptanceStyle = autoCompletion.getContentProposalAdapter().getProposalAcceptanceStyle();
        if (proposalAcceptanceStyle == ContentProposalAdapter.PROPOSAL_REPLACE) {
            final Expression selectedExpression = getSelectedExpression();
            final CompoundCommand cc = new CompoundCommand("Update Expression (and potential side components)");
            final ExpressionProposal prop = (ExpressionProposal) proposal;
            final Expression copy = EcoreUtil.copy((Expression) prop.getExpression());
            copy.setReturnTypeFixed(selectedExpression.isReturnTypeFixed());
            sideModificationOnProposalAccepted(cc, copy);

            updateSelection(cc, copy);
            fireSelectionChanged(new SelectionChangedEvent(ExpressionViewer.this,
                    new StructuredSelection(selectedExpression)));
            validate();
        }
    }

    protected void sideModificationOnProposalAccepted(final CompoundCommand cc, final Expression copy) {
        final String copyType = copy.getType();
        if (ExpressionConstants.FORM_FIELD_TYPE.equals(copyType)) {
            proposalAcceptedForFormField(copy);
        }
    }

    private void proposalAcceptedForFormField(final Expression copy) {
        EObject parent = context;
        if (parent == null) {
            parent = expressionNatureProvider.getContext();
        }
        if (parent instanceof Widget) {
            final Widget w = (Widget) parent;
            if (w != null && w instanceof TextFormField && copy.getName().equals(WidgetHelper.FIELD_PREFIX + w.getName())) {
                String returnTypeModifier = w.getReturnTypeModifier();
                if (returnTypeModifier != null) {
                    if (w instanceof Duplicable && ((Duplicable) w).isDuplicate()) {
                        returnTypeModifier = List.class.getName();
                    }
                    if (!copy.isReturnTypeFixed()) {
                        copy.setReturnType(returnTypeModifier);
                    }
                }
            }
        }
    }

    @Override
    public void proposalPopupOpened(final BonitaContentProposalAdapter adapter) {
        manageNatureProviderAndAutocompletionProposal(getInput());
    }

    @Override
    public void proposalPopupClosed(final BonitaContentProposalAdapter adapter) {

    }

    public IExpressionNatureProvider getExpressionNatureProvider() {
        return expressionNatureProvider;
    }

    @Override
    public boolean isPageFlowContext() {

        return isPageFlowContext;
    }

    @Override
    public void setIsPageFlowContext(final boolean isPageFlowContext) {
        this.isPageFlowContext = isPageFlowContext;

    }

    public void setRefactorOperationToExecuteWhenUpdatingContent(final AbstractRefactorOperation<?, ?, ?> operation) {
        this.operation = operation;
    }

    private boolean executeOperation(final String newValue) {
        boolean hasBeenExecuted = false;
        if (operation != null) {
            final Object oldValue = getInput();
            if (oldValue instanceof Element && !newValue.equals(((Element) getInput()).getName())
                    || oldValue instanceof SearchIndex && !newValue.equals(((SearchIndex) getInput()).getName().getName())) {
                operation.addItemToRefactor(newValue, getInput());
                final IProgressService service = PlatformUI.getWorkbench().getProgressService();
                try {
                    service.busyCursorWhile(operation);
                    hasBeenExecuted = true;
                } catch (final InvocationTargetException e) {
                    BonitaStudioLog.error(e);
                } catch (final InterruptedException e) {
                    BonitaStudioLog.error(e);
                }
            }
        }
        return hasBeenExecuted;
    }

    public void setRemoveOperation(final AbstractRefactorOperation<?, ?, ?> removeOperation) {
        this.removeOperation = removeOperation;
    }

    private boolean executeRemoveOperation(final CompoundCommand cc) {
        boolean isExecuted = false;
        if (removeOperation != null) {
            removeOperation.setCompoundCommand(cc);
            final IProgressService service = PlatformUI.getWorkbench().getProgressService();
            try {
                service.busyCursorWhile(removeOperation);
                isExecuted = true;
            } catch (final InvocationTargetException e) {
                BonitaStudioLog.error(e);
            } catch (final InterruptedException e) {
                BonitaStudioLog.error(e);
            }

        }
        return isExecuted;
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.studio.common.IBonitaVariableContext#isOverViewContext()
     */
    @Override
    public boolean isOverViewContext() {
        return isOverviewContext;
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.studio.common.IBonitaVariableContext#setIsOverviewContext(boolean)
     */
    @Override
    public void setIsOverviewContext(final boolean isOverviewContext) {
        this.isOverviewContext = isOverviewContext;

    }

    public void setAutocomplitionLabelProvider(final LabelProvider labelProvider) {
        getContentAssistText().getAutocompletion().setLabelProvider(labelProvider);

    }

    @Override
    public void validationStatusChanged(final IStatus newStatus) {
        setMessage(newStatus.getMessage(), newStatus.getSeverity());
    }

    @Override
    public void handleValueChange(final ValueChangeEvent event) {
        validate();
    }

}
