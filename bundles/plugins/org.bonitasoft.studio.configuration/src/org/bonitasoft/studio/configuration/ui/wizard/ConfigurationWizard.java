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
package org.bonitasoft.studio.configuration.ui.wizard;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.studio.common.ModelVersion;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.extension.BonitaStudioExtensionRegistryManager;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.common.repository.model.IRepositoryStore;
import org.bonitasoft.studio.configuration.ConfigurationSynchronizer;
import org.bonitasoft.studio.configuration.extension.IProcessConfigurationWizardPage;
import org.bonitasoft.studio.configuration.i18n.Messages;
import org.bonitasoft.studio.configuration.preferences.ConfigurationPreferenceConstants;
import org.bonitasoft.studio.configuration.ui.wizard.page.ApplicationDependenciesConfigurationWizardPage;
import org.bonitasoft.studio.configuration.ui.wizard.page.ProcessDependenciesConfigurationWizardPage;
import org.bonitasoft.studio.configuration.ui.wizard.page.RunConfigurationWizardPage;
import org.bonitasoft.studio.diagram.custom.repository.ProcessConfigurationRepositoryStore;
import org.bonitasoft.studio.model.configuration.Configuration;
import org.bonitasoft.studio.model.configuration.ConfigurationFactory;
import org.bonitasoft.studio.model.configuration.util.ConfigurationAdapterFactory;
import org.bonitasoft.studio.model.configuration.util.ConfigurationResourceFactoryImpl;
import org.bonitasoft.studio.model.process.AbstractProcess;
import org.bonitasoft.studio.model.process.MainProcess;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.bonitasoft.studio.model.process.util.ProcessAdapterFactory;
import org.bonitasoft.studio.pics.Pics;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

/**
 * @author Romain Bioteau
 *
 */
public class ConfigurationWizard extends Wizard {

    private static final String CONFIGURATION_WIZARD_PAGE_ID = "org.bonitasoft.studio.configuration.wizardPage";
    private static final String PRIORITY_ATTRIBUTE = "priority";
    private static final String CLASS_ATTRIBUTE = "class";

    private MainProcess diagram;
    private AbstractProcess process;
    private Configuration configurationWorkingCopy;
    private String configurationName;
    private ComposedAdapterFactory adapterFactory;
    private final IRepositoryStore processConfStore;

    public ConfigurationWizard() {
        super() ;
        setDefaultPageImageDescriptor(Pics.getWizban()) ;
        processConfStore = RepositoryManager.getInstance().getCurrentRepository().getRepositoryStore(ProcessConfigurationRepositoryStore.class) ;
    }

    @Override
    public boolean needsPreviousAndNextButtons() {
        return false;
    }

    public ConfigurationWizard(MainProcess diagram,AbstractProcess selectedProcess,String configurationName) {
        this() ;
        Assert.isNotNull(selectedProcess);
        Assert.isNotNull(configurationName);
        Assert.isNotNull(diagram);
        this.diagram = diagram ;
        this.configurationName = configurationName ;
        setProcess(selectedProcess) ;
        String processName = selectedProcess.getName() +" ("+selectedProcess.getVersion()+")";
        setWindowTitle(Messages.bind(Messages.configurationTitle,configurationName,processName)) ;
    }

    @Override
    public void addPages() {
        IConfigurationElement[] elems = BonitaStudioExtensionRegistryManager.getInstance().getConfigurationElements(CONFIGURATION_WIZARD_PAGE_ID);
        List<IConfigurationElement> elements = sortByPriority(elems) ;
        for(IConfigurationElement e :elements){
            try {
                IProcessConfigurationWizardPage page =  (IProcessConfigurationWizardPage) e.createExecutableExtension(CLASS_ATTRIBUTE) ;
                addPage(page) ;
            } catch (Exception e1){
                BonitaStudioLog.error(e1) ;
            }
        }
        addPage(new ProcessDependenciesConfigurationWizardPage()) ;
        addPage(new ApplicationDependenciesConfigurationWizardPage()) ;
        addPage(new RunConfigurationWizardPage()) ;
    }



    private List<IConfigurationElement> sortByPriority(IConfigurationElement[] elems) {
        List<IConfigurationElement> sortedConfigElems = new ArrayList<IConfigurationElement>() ;
        for(IConfigurationElement elem : elems){
            sortedConfigElems.add(elem) ;
        }

        Collections.sort(sortedConfigElems, new Comparator<IConfigurationElement>() {

            @Override
            public int compare(IConfigurationElement e1, IConfigurationElement e2) {
                int	p1 = 0;
                int p2 = 0 ;
                try{
                    p1 = Integer.parseInt(e1.getAttribute(PRIORITY_ATTRIBUTE));
                }catch (NumberFormatException e) {
                    p1 = 0 ;
                }
                try{
                    p2 = Integer.parseInt(e2.getAttribute(PRIORITY_ATTRIBUTE));
                }catch (NumberFormatException e) {
                    p2 = 0 ;
                }
                return  p1 -p2; //Lowest Priority first
            }

        }) ;

        return sortedConfigElems ;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        applyChanges() ;
        return true;
    }

    public void applyChanges(){
        Configuration configuration = getConfigurationCopy() ;
        if(process != null && configuration != null){
            String id = ModelHelper.getEObjectID(process) ;
            if(configurationName.equals(ConfigurationPreferenceConstants.LOCAL_CONFIGURAITON)){
                String fileName = id+".conf";
                IRepositoryFileStore file = processConfStore.getChild(fileName) ;
                if(file == null){
                    file = processConfStore.createRepositoryFileStore(fileName) ;
                }
                try {
                    file.save(configuration) ;
                } catch (Exception e) {
                    BonitaStudioLog.error(e) ;
                }
            }else{
                EditingDomain editingDomain = TransactionUtil.getEditingDomain(process) ;
                boolean dispose = false ;
                if(editingDomain == null){
                    dispose = true ;
                    editingDomain =  initializeEditingDomain() ;
                }
                CompoundCommand cc = new CompoundCommand() ;
                for(Configuration conf : process.getConfigurations()){
                    if(conf.getName().equals(configuration.getName())){
                        cc.append(RemoveCommand.create(editingDomain, process, ProcessPackage.Literals.ABSTRACT_PROCESS__CONFIGURATIONS, conf)) ;
                        break ;
                    }
                }
                cc.append(AddCommand.create(editingDomain, process, ProcessPackage.Literals.ABSTRACT_PROCESS__CONFIGURATIONS, EcoreUtil.copy(configuration))) ;
                editingDomain.getCommandStack().execute(cc) ;
                if(dispose){
                    adapterFactory.dispose() ;
                    try {
                        process.eResource().save(Collections.EMPTY_MAP) ;
                    } catch (IOException e) {
                        BonitaStudioLog.error(e) ;
                    }
                }
            }
        }
    }

    protected AdapterFactoryEditingDomain initializeEditingDomain() {
        // Create an adapter factory that yields item providers.
        adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
        adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
        adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
        adapterFactory.addAdapterFactory(new ConfigurationAdapterFactory()) ;
        adapterFactory.addAdapterFactory(new ProcessAdapterFactory()) ;

        // command stack that will notify this editor as commands are executed
        BasicCommandStack commandStack = new BasicCommandStack();

        // Create the editing domain with our adapterFactory and command stack.
        AdapterFactoryEditingDomain editingDomain = new AdapterFactoryEditingDomain(adapterFactory,commandStack, new HashMap<Resource, Boolean>());
        editingDomain.getResourceSet().getResourceFactoryRegistry().getExtensionToFactoryMap().put("conf", new ConfigurationResourceFactoryImpl()) ;
        return editingDomain ;
    }

    public AbstractProcess getProcess() {
        return process;
    }

    public void setProcess(final AbstractProcess process) {
        this.process = process ;
        final Configuration configuration = getConfigurationFromProcess(process,configurationName) ;
        if(configuration != null){
        	IProgressService service = PlatformUI.getWorkbench().getProgressService();
        	try {
				service.run(true, false, new IRunnableWithProgress() {
					
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException,
							InterruptedException {
						new ConfigurationSynchronizer(process, configuration).synchronize(monitor) ;
					}
				});
			} catch (InvocationTargetException e) {
				BonitaStudioLog.error(e);
			} catch (InterruptedException e) {
				BonitaStudioLog.error(e);
			}
        	
            configurationWorkingCopy = EcoreUtil.copy(configuration) ;
        }
    }

    public MainProcess getDiagram() {
        return diagram;
    }

    public void updatePages() {
        for(IWizardPage page : getPages()){
            if(page instanceof IProcessConfigurationWizardPage){
                ((IProcessConfigurationWizardPage) page).updatePage(process,configurationWorkingCopy) ;
            }
        }
    }

    @Override
    public IWizardPage getStartingPage() {
        if (getPageCount() == 0) {
            return null;
        }
        for(IWizardPage page : getPages()){
            if(page instanceof IProcessConfigurationWizardPage){
                if(configurationWorkingCopy != null && ((IProcessConfigurationWizardPage) page).isConfigurationPageValid(configurationWorkingCopy) != null){
                    return page ;
                }
            }
        }
        return super.getStartingPage() ;
    }

    private Configuration getConfigurationFromProcess(AbstractProcess process, String confName) {
        String id = ModelHelper.getEObjectID(getProcess()) ;
        Configuration configuration = null ;
        if(ConfigurationPreferenceConstants.LOCAL_CONFIGURAITON.equals(confName)){
            final IRepositoryFileStore file = processConfStore.getChild(id+".conf") ;
            if(file != null){
                configuration = (Configuration) file.getContent() ;
            }else{
                configuration = ConfigurationFactory.eINSTANCE.createConfiguration();
                configuration.setName(ConfigurationPreferenceConstants.LOCAL_CONFIGURAITON) ;
                configuration.setVersion(ModelVersion.CURRENT_VERSION);
            }
        }else if(process != null){
            for(Configuration conf : process.getConfigurations()){
                if(conf.getName().equals(confName)){
                    configuration = conf ;
                    break ;
                }
            }
            if(configuration == null){
                configuration = ConfigurationFactory.eINSTANCE.createConfiguration() ;
                configuration.setName(confName) ;
                configuration.setVersion(ModelVersion.CURRENT_VERSION);
            }
        }
        return configuration ;
    }

    public Configuration getConfigurationCopy() {
        return configurationWorkingCopy;
    }


}