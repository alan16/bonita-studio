/**
 * Copyright (C) 2012-2014 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.data.ui.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bonitasoft.studio.common.DataTypeLabels;
import org.bonitasoft.studio.common.DatasourceConstants;
import org.bonitasoft.studio.common.IBonitaVariableContext;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.data.DataPlugin;
import org.bonitasoft.studio.data.i18n.Messages;
import org.bonitasoft.studio.data.operation.RefactorDataOperation;
import org.bonitasoft.studio.model.process.AbstractProcess;
import org.bonitasoft.studio.model.process.Activity;
import org.bonitasoft.studio.model.process.Data;
import org.bonitasoft.studio.model.process.ProcessFactory;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.bonitasoft.studio.pics.Pics;
import org.bonitasoft.studio.refactoring.core.RefactoringOperationType;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.xtext.ui.XtextProjectHelper;

/**
 * @author Romain Bioteau
 * 
 */
public class DataWizard extends Wizard implements IBonitaVariableContext {

    private final EObject container;

    private final Data dataWorkingCopy;

    private boolean editMode = false;

    private Data originalData;

    private Set<EStructuralFeature> featureToCheckForUniqueID;

    private EStructuralFeature dataContainmentFeature;

    private boolean showAutogenerateForm;

    private DataWizardPage page;

    private String fixedReturnType;

    private TransactionalEditingDomain editingDomain;

    private boolean isPageFlowContext = false;

    private boolean isOverviewContext = false;

    public DataWizard(TransactionalEditingDomain editingDomain, EObject container, EStructuralFeature dataContainmentFeature,
            Set<EStructuralFeature> featureToCheckForUniqueID,
            boolean showAutogenerateForm) {
        initDataWizard(dataContainmentFeature, showAutogenerateForm);
        this.editingDomain = editingDomain;
        this.container = container;
        dataWorkingCopy = ProcessFactory.eINSTANCE.createData();
        dataWorkingCopy.setDataType(ModelHelper.getDataTypeForID(container, DataTypeLabels.stringDataType));
        editMode = false;
        this.featureToCheckForUniqueID = new HashSet<EStructuralFeature>();
        this.featureToCheckForUniqueID.add(dataContainmentFeature);
        setWindowTitle(Messages.newVariable);
    }

    public DataWizard(TransactionalEditingDomain editingDomain, EObject container, EStructuralFeature dataContainmentFeature,
            Set<EStructuralFeature> featureToCheckForUniqueID,
            boolean showAutogenerateForm, String fixedReturnType) {
        initDataWizard(dataContainmentFeature, showAutogenerateForm);
        this.editingDomain = editingDomain;
        this.container = container;
        dataWorkingCopy = ProcessFactory.eINSTANCE.createData();
        dataWorkingCopy.setDataType(ModelHelper.getDataTypeForID(container, DataTypeLabels.stringDataType));
        editMode = false;
        this.featureToCheckForUniqueID = new HashSet<EStructuralFeature>();
        this.featureToCheckForUniqueID.add(dataContainmentFeature);
        this.fixedReturnType = fixedReturnType;
        setWindowTitle(Messages.newVariable);
    }

    public DataWizard(TransactionalEditingDomain editingDomain, Data data, EStructuralFeature dataContainmentFeature,
            Set<EStructuralFeature> featureToCheckForUniqueID, boolean showAutogenerateForm) {
        initDataWizard(dataContainmentFeature, showAutogenerateForm);
        Assert.isNotNull(data);
        this.editingDomain = editingDomain;
        setNeedsProgressMonitor(true);
        container = data.eContainer();
        originalData = data;
        dataWorkingCopy = EcoreUtil.copy(data);
        editMode = true;
        this.featureToCheckForUniqueID = featureToCheckForUniqueID;
        setWindowTitle(Messages.editVariable);
    }

    private void initDataWizard(EStructuralFeature dataContainmentFeature, boolean showAutogenerateForm) {
        setDefaultPageImageDescriptor(Pics.getWizban());
        this.dataContainmentFeature = dataContainmentFeature;// the default add data on this feature
        this.showAutogenerateForm = showAutogenerateForm;
    }

    @Override
    public void addPages() {
        page = getWizardPage();
        addPage(page);
    }

    protected DataWizardPage getWizardPage() {
        DataWizardPage page = null;
        if (!dataContainmentFeature.equals(ProcessPackage.Literals.DATA_AWARE__DATA)) {
            page = new DataWizardPage(dataWorkingCopy, container, false, false, false, showAutogenerateForm, featureToCheckForUniqueID, fixedReturnType);
            page.setIsPageFlowContext(isPageFlowContext);
            page.setIsOverviewContext(isOverviewContext);
            if (editMode) {
                page.setTitle(Messages.editVariableTitle);
                page.setDescription(Messages.editVariableDescription);
            }
            return page;
        } else {
            boolean isOnActivity = container instanceof Activity;
            page = new DataWizardPage(dataWorkingCopy, container, true, true, isOnActivity, showAutogenerateForm, featureToCheckForUniqueID, fixedReturnType);
            page.setIsPageFlowContext(isPageFlowContext);
            page.setIsOverviewContext(isOverviewContext);
        }
        if (editMode) {
            page.setTitle(Messages.editVariableTitle);
            page.setDescription(Messages.editVariableDescription);
        }
        return page;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        Data workingCopy = getWorkingCopy();
        setDatasourceId(workingCopy, dataContainmentFeature);
        if (editMode) {
            final RefactorDataOperation op = createRefactorOperation(editingDomain, workingCopy);
            final boolean switchingDataEClass = !originalData.eClass().equals(workingCopy.eClass());
            op.setUpdateDataReferences(switchingDataEClass);
            if (op.canExecute()) {
                try {
                    getContainer().run(true, false, op);
                } catch (InvocationTargetException e) {
                    BonitaStudioLog.error(e);
                    return false;
                } catch (InterruptedException e) {
                    BonitaStudioLog.error(e);
                    return false;
                }
                if (op.isCancelled()) {
                    return false;
                }
            }
        } else {
            editingDomain.getCommandStack().execute(AddCommand.create(editingDomain, container, dataContainmentFeature, workingCopy));
        }
        refreshXtextReferences();

        return true;
    }

    protected void refreshXtextReferences() {
        try {
            RepositoryManager.getInstance().getCurrentRepository().getProject()
                    .build(IncrementalProjectBuilder.FULL_BUILD, XtextProjectHelper.BUILDER_ID, Collections.<String, String> emptyMap(), null);
        } catch (CoreException e) {
            BonitaStudioLog.error(e, DataPlugin.PLUGIN_ID);
        }
    }

    protected RefactorDataOperation createRefactorOperation(final TransactionalEditingDomain editingDomain, Data workingCopy) {
        AbstractProcess process = ModelHelper.getParentProcess(container);
        final RefactorDataOperation op = new RefactorDataOperation(RefactoringOperationType.UPDATE);
        op.setEditingDomain(editingDomain);
        op.setContainer(process);
        op.addItemToRefactor(workingCopy, originalData);
        op.setDirectDataContainer(container);
        op.setDataContainmentFeature(dataContainmentFeature);
        op.setAskConfirmation(true);
        return op;
    }

    public Data getWorkingCopy() {
        return page.getWorkingCopy();
    }

    private void setDatasourceId(Data workingCopy, EStructuralFeature feature) {
        if (feature.equals(ProcessPackage.Literals.PAGE_FLOW__TRANSIENT_DATA)
                || feature.equals(ProcessPackage.Literals.RECAP_FLOW__RECAP_TRANSIENT_DATA)
                || feature.equals(ProcessPackage.Literals.VIEW_PAGE_FLOW__VIEW_TRANSIENT_DATA)) {
            workingCopy.setDatasourceId(DatasourceConstants.PAGEFLOW_DATASOURCE);
        } else if (workingCopy.isTransient()) {
            workingCopy.setDatasourceId(DatasourceConstants.IN_MEMORY_DATASOURCE);
        } else {
            workingCopy.setDatasourceId(DatasourceConstants.BOS_DATASOURCE);
        }
    }

    public Data getOriginalData() {
        return originalData;
    }

    @Override
    public boolean isPageFlowContext() {
        return isPageFlowContext;
    }

    @Override
    public void setIsPageFlowContext(boolean isPageFlowContext) {
        this.isPageFlowContext = isPageFlowContext;

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
    public void setIsOverviewContext(boolean isOverviewContext) {
        this.isOverviewContext = isOverviewContext;

    }

}
