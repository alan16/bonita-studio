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
package org.bonitasoft.studio.actors.ui.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCriterion;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.studio.actors.repository.OrganizationRepositoryStore;
import org.bonitasoft.studio.common.ProjectUtil;
import org.bonitasoft.studio.common.jface.BonitaErrorDialog;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.Repository;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.common.repository.model.IRepositoryStore;
import org.bonitasoft.studio.engine.BOSEngineManager;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

/**
 * @author Romain Bioteau
 *
 */
public class InstallOrganizationHandler extends AbstractHandler {

    private static final String PROFILE_ID = "profileId";
    private static final String USER_ID = "userId";
    private static final String USER_PROFILE_NAME = "User";

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if(event != null){
            String id = event.getParameter("artifact") ;
            IRepositoryStore organizationStore = RepositoryManager.getInstance().getRepositoryStore(OrganizationRepositoryStore.class) ;
            IRepositoryFileStore file = organizationStore.getChild(id) ;
            if(file != null){
                APISession session = null;
                try{
                    session = BOSEngineManager.getInstance().loginDefaultTenant(Repository.NULL_PROGRESS_MONITOR) ;
                    IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session) ;
                    identityAPI.deleteOrganization() ;
                    File tmpFile = new File(ProjectUtil.getBonitaStudioWorkFolder(),"tmpOrganizationFile.xml") ;
                    tmpFile.delete() ;
                    file.export(tmpFile.getAbsolutePath()) ;
                    String content = getFileContent(tmpFile) ;
                    tmpFile.delete() ;
                    identityAPI.importOrganization(content) ;
                }catch(final Exception e){
                    Display.getDefault().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            new BonitaErrorDialog(Display.getDefault().getActiveShell(), "Error", "An error occured during synchronization", e).open() ;
                        }

                    }) ;
                    throw new ExecutionException("", e) ;
                }finally{
                    if(session != null){
                        BOSEngineManager.getInstance().logoutDefaultTenant(session) ;
                    }
                }

                session = null;
                try{
                    session = BOSEngineManager.getInstance().loginDefaultTenant(Repository.NULL_PROGRESS_MONITOR) ;
                    IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session) ;
                    CommandAPI commandApi =  TenantAPIAccessor.getCommandAPI(session) ;
                    applyAllProfileToUsers(identityAPI,commandApi) ;
                }catch (final Exception e) {
                    Display.getDefault().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            new BonitaErrorDialog(Display.getDefault().getActiveShell(), "Error", "An error occured during synchronization", e).open() ;
                        }

                    }) ;
                    throw new ExecutionException("", e) ;
                }finally{
                    if(session != null){
                        BOSEngineManager.getInstance().logoutDefaultTenant(session) ;
                    }
                }

            }else{
                throw new ExecutionException("Organization : "+ id +" not found !") ;
            }
        }
        return null;
    }

    protected void applyAllProfileToUsers(IdentityAPI identityAPI, CommandAPI commandApi) throws Exception {
        final Map<String, Serializable> searchParameters = new HashMap<String, Serializable>();
        searchParameters.put("fromIndex", 0);
        searchParameters.put("numberOfProfiles", Integer.MAX_VALUE);
        searchParameters.put("field", "name");
        searchParameters.put("order", "DESC");

        final List<Long> profiles = new ArrayList<Long>() ;
        final List<Map<String, Serializable>> searchedProfiles = (List<Map<String, Serializable>>) commandApi.execute("searchProfile", searchParameters);
        for(Map<String, Serializable> profile : searchedProfiles){
            if(USER_PROFILE_NAME.equals(profile.get("name"))){
                long profileId =  (Long) profile.get("id") ;
                profiles.add(profileId) ;
            }
        }

        List<User> users = identityAPI.getUsers(0, Integer.MAX_VALUE, UserCriterion.USER_NAME_ASC) ;
        for(User u : users){
            long id =  u.getId() ;
            for(Long profile : profiles){
                final Map<String, Serializable> addProfileMemberParams = new HashMap<String, Serializable>();
                addProfileMemberParams.put(PROFILE_ID, profile);
                addProfileMemberParams.put(USER_ID, id);
                commandApi.execute("addProfileMember", addProfileMemberParams);
            }
        }
    }

    private String getFileContent(File file){
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = ""; //$NON-NLS-1$
            while((line = reader.readLine()) != null)
            {
                sb.append(line);
                sb.append(SWT.CR);
            }
            reader.close();

            return sb.toString() ;
        }
        catch (IOException ioe)
        {
            BonitaStudioLog.error(ioe);
        }
        return null;
    }

}
