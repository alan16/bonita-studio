/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.actors.operation;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCriterion;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.studio.actors.ActorsPlugin;
import org.bonitasoft.studio.actors.model.organization.DocumentRoot;
import org.bonitasoft.studio.actors.model.organization.Organization;
import org.bonitasoft.studio.actors.model.organization.OrganizationFactory;
import org.bonitasoft.studio.actors.model.organization.PasswordType;
import org.bonitasoft.studio.actors.model.organization.util.OrganizationXMLProcessor;
import org.bonitasoft.studio.common.BonitaConstants;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.Repository;
import org.bonitasoft.studio.engine.BOSEngineManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.eclipse.emf.ecore.xmi.util.XMLProcessor;
import org.eclipse.jface.operation.IRunnableWithProgress;


/**
 * @author Romain Bioteau
 *
 */
public class PublishOrganizationOperation implements IRunnableWithProgress{

    private Organization organization;
    private APISession session;
    private boolean flushSession;

    public PublishOrganizationOperation(Organization organization){
        this.organization =  organization;
    }

    public void setSession(APISession session){
        this.session = session;
    }

    public void setOrganization(Organization organization){

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        Assert.isNotNull(organization);
        flushSession = false;
        BonitaStudioLog.info("Loading organization "+organization.getName()+" in portal...",ActorsPlugin.PLUGIN_ID) ;
        try {
            if(session == null){
                session = BOSEngineManager.getInstance().loginDefaultTenant(Repository.NULL_PROGRESS_MONITOR) ;
                flushSession = true;
            }

            IdentityAPI identityAPI = BOSEngineManager.getInstance().getIdentityAPI(session);
            ProcessAPI processApi = BOSEngineManager.getInstance().getProcessAPI(session);
            SearchResult<ProcessDeploymentInfo> result = processApi.searchProcessDeploymentInfos(new SearchOptionsBuilder(0,Integer.MAX_VALUE).done());
            for(ProcessDeploymentInfo info : result.getResult()){
                processApi.deleteProcessInstances(info.getProcessId(), 0, Integer.MAX_VALUE);
                processApi.deleteArchivedProcessInstances(info.getProcessId(), 0, Integer.MAX_VALUE);
            }
            identityAPI.deleteOrganization() ;
            String content = toString(organization);
            identityAPI.importOrganization(content) ;
            ProfileAPI profileAPI =  BOSEngineManager.getInstance().getProfileAPI(session) ;
            applyAllProfileToUsers(identityAPI,profileAPI) ;
        }catch(Exception e){
            throw new InvocationTargetException(e);
        }finally{
            if(flushSession && session != null){
                BOSEngineManager.getInstance().logoutDefaultTenant(session);
                session = null;
            }
        }
    }

    private String toString(Organization organization) throws IOException {
        DocumentRoot root = OrganizationFactory.eINSTANCE.createDocumentRoot() ;
        Organization exportedCopy = EcoreUtil.copy(organization)  ;
        exportedCopy.setName(null) ;
        exportedCopy.setDescription(null) ;
        addStudioTechnicalUser(exportedCopy);
        root.setOrganization(exportedCopy) ;
        final XMLProcessor processor = new OrganizationXMLProcessor() ;
        final Resource resource = new XMLResourceImpl() ;
        resource.getContents().add(root) ;
        final Map<String, Object> options = new HashMap<String, Object>();
        options.put(XMLResource.OPTION_ENCODING, "UTF-8");
        options.put(XMLResource.OPTION_XML_VERSION, "1.0");
        return processor.saveToString(resource, options);
    }

    /**
     * @param exportedCopy
     */
    private void addStudioTechnicalUser(Organization exportedCopy) {
        org.bonitasoft.studio.actors.model.organization.User user = OrganizationFactory.eINSTANCE.createUser();
        PasswordType passwordType = OrganizationFactory.eINSTANCE.createPasswordType();
        passwordType.setValue("bpm");
        passwordType.setEncrypted(false);
        user.setUserName(BonitaConstants.STUDIO_TECHNICAL_USER_NAME);
        user.setFirstName(BonitaConstants.STUDIO_TECHNICAL_USER_FIRST_NAME);
        user.setJobTitle(BonitaConstants.STUDIO_TECHNICAL_USER_JOB_TITLE);
        user.setPassword(passwordType);
        exportedCopy.getUsers().getUser().add(user);
    }

    protected void applyAllProfileToUsers(IdentityAPI identityAPI, ProfileAPI profileAPI) throws Exception {
        final List<Long> profiles = new ArrayList<Long>() ;
        final SearchOptions options = new SearchOptionsBuilder(0, Integer.MAX_VALUE).sort("name", Order.DESC).done();
        final SearchResult<Profile> searchedProfiles = profileAPI.searchProfiles(options);
        for(Profile profile : searchedProfiles.getResult()){
            long profileId = profile.getId();
            profiles.add(profileId) ;
        }

        List<User> users = identityAPI.getUsers(0, Integer.MAX_VALUE, UserCriterion.USER_NAME_ASC) ;
        for(User u : users){
            long id =  u.getId() ;
            for(Long profile : profiles){
                profileAPI.createProfileMember(profile, id, -1L, -1L);
            }
        }
    }


}
