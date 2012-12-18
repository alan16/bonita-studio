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
package org.bonitasoft.studio.actors.repository;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.bonitasoft.studio.actors.ActorsPlugin;
import org.bonitasoft.studio.actors.i18n.Messages;
import org.bonitasoft.studio.actors.model.organization.Organization;
import org.bonitasoft.studio.actors.model.organization.util.OrganizationAdapterFactory;
import org.bonitasoft.studio.common.jface.FileActionDialog;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.Repository;
import org.bonitasoft.studio.common.repository.store.AbstractEMFRepositoryStore;
import org.bonitasoft.studio.pics.Pics;
import org.eclipse.core.resources.IFile;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.swt.graphics.Image;

/**
 * @author Romain Bioteau
 *
 */
public class OrganizationRepositoryStore extends AbstractEMFRepositoryStore<OrganizationFileStore> {

    private static final String STORE_NAME = "organizations";
    public static final String ORGANIZATION_EXT = "organization";
    private static final Set<String> extensions = new HashSet<String>() ;
    static{
        extensions.add(ORGANIZATION_EXT) ;
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.common.repository.model.IRepositoryStore#createRepositoryFileStore(java.lang.String)
     */
    @Override
    public OrganizationFileStore createRepositoryFileStore(String fileName) {
        return new OrganizationFileStore(fileName, this);
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.common.repository.model.IRepositoryStore#getName()
     */
    @Override
    public String getName() {
        return STORE_NAME;
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.common.repository.model.IRepositoryStore#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return Messages.organizations;
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.common.repository.model.IRepositoryStore#getIcon()
     */
    @Override
    public Image getIcon() {
        return Pics.getImage("organization.png",ActorsPlugin.getDefault());
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.common.repository.model.IRepositoryStore#getCompatibleExtensions()
     */
    @Override
    public Set<String> getCompatibleExtensions() {
        return extensions;
    }

    @Override
    protected OrganizationFileStore doImportInputStream(String fileName,InputStream inputStream) {
        String newFileName = fileName.replace(".xml", "."+ORGANIZATION_EXT) ;
        final IFile file = getResource().getFile(newFileName);
        OrganizationFileStore fileStore = null;
        try{
            if(file.exists()){
                if(FileActionDialog.overwriteQuestion(newFileName)){
                    file.setContents(inputStream, true, false, Repository.NULL_PROGRESS_MONITOR);
                }
            } else {
                file.create(inputStream, true, Repository.NULL_PROGRESS_MONITOR);
            }
            fileStore = createRepositoryFileStore(newFileName);
        }catch(Exception e){
            BonitaStudioLog.error(e) ;
        }
        if(file != null){
            Organization orga = fileStore.getContent() ;
            if(orga.getName() == null || orga.getName().isEmpty()){
                orga.setName(newFileName.substring(0,newFileName.length()-ORGANIZATION_EXT.length() - 1)) ;
                fileStore.save(orga) ;
            }
        }
        return fileStore ;
    }

    @Override
    protected void addAdapterFactory(ComposedAdapterFactory adapterFactory) {
        adapterFactory.addAdapterFactory(new OrganizationAdapterFactory()) ;
    }

}
