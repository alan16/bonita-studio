/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.diagram.custom.repository;

import java.util.Set;

import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.model.configuration.ConfigurationFactory;
import org.bonitasoft.studio.model.process.AbstractProcess;
import org.bonitasoft.studio.model.process.Pool;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.bonitasoft.studio.preferences.BonitaPreferenceConstants;
import org.bonitasoft.studio.preferences.BonitaStudioPreferencesPlugin;
import org.bonitasoft.studio.repository.themes.ApplicationLookNFeelFileStore;
import org.bonitasoft.studio.repository.themes.LookNFeelRepositoryStore;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener;

/**
 * 
 * @author Romain Bioteau
 *
 */
public class PoolNotificationListerner implements NotificationListener {

    private final ProcessConfigurationRepositoryStore processConfStore;
    private final ApplicationResourceRepositoryStore resourceStore;
    private final LookNFeelRepositoryStore lookNFeelStore;
    private final DiagramRepositoryStore diagramStore;

    public PoolNotificationListerner(){
        processConfStore = (ProcessConfigurationRepositoryStore) RepositoryManager.getInstance().getRepositoryStore(ProcessConfigurationRepositoryStore.class) ;
        diagramStore = (DiagramRepositoryStore) RepositoryManager.getInstance().getRepositoryStore(DiagramRepositoryStore.class) ;
        resourceStore = (ApplicationResourceRepositoryStore) RepositoryManager.getInstance().getRepositoryStore(ApplicationResourceRepositoryStore.class) ;
        lookNFeelStore = (LookNFeelRepositoryStore)RepositoryManager.getInstance().getRepositoryStore(LookNFeelRepositoryStore.class) ;
    }

    public void notifyChanged(Notification notification) {
        // Listen for changes to features.
        switch (notification.getFeatureID(AbstractProcess.class)) {
            case ProcessPackage.ABSTRACT_PROCESS__ELEMENTS:
                if (notification.getNewValue() instanceof Pool) { //Pool added
                    Pool pool = (Pool) notification.getNewValue();
                    TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(pool) ;


                    // check that process is not empty
                    String processUUID = ModelHelper.getEObjectID(pool) ;
                    IRepositoryFileStore confFile = processConfStore.createRepositoryFileStore(processUUID+".conf") ;
                    confFile.save(ConfigurationFactory.eINSTANCE.createConfiguration()) ;

                    ApplicationResourceFileStore artifact = (ApplicationResourceFileStore) resourceStore.getChild(processUUID) ;
                    if (artifact == null) {
                        String themeId = BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore().getString(BonitaPreferenceConstants.DEFAULT_APPLICATION_THEME) ;
                        ApplicationLookNFeelFileStore file = (ApplicationLookNFeelFileStore) lookNFeelStore.getChild(themeId) ;
                        CompoundCommand templateCommand = WebTemplatesUtil.createAddTemplateCommand(editingDomain, pool, file);
                        // add an empty application folder
                        editingDomain.getCommandStack().execute(templateCommand);
                        org.eclipse.emf.common.command.Command createDefaultResourceFolders = WebTemplatesUtil.createDefaultResourceFolders(editingDomain, pool);
                        if (createDefaultResourceFolders != null) {
                            editingDomain.getCommandStack().execute(createDefaultResourceFolders);
                        }
                    }
                } else if(notification.getNewValue() == null && notification.getOldValue() instanceof Pool){//Pool removed
                    Set<String> poolIds = diagramStore.getAllProcessIds() ;
                    for(IRepositoryFileStore file : processConfStore.getChildren()){
                        String id = file.getName() ;
                        id = id.substring(0, id.lastIndexOf(".")) ;
                        if(!poolIds.contains(id)){
                            file.delete() ;
                        }
                    }

                    for(IRepositoryFileStore file : resourceStore.getChildren()){
                        String id = file.getName() ;
                        if(!poolIds.contains(id)){
                            file.delete() ;
                        }
                    }

                }
                break;
        }
    }

}