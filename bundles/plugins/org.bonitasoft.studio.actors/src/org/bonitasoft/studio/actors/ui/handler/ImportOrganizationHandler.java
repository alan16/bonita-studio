/**
 * Copyright (C) 2012-2014 BonitaSoft S.A.
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
package org.bonitasoft.studio.actors.ui.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CancellationException;

import org.bonitasoft.studio.actors.i18n.Messages;
import org.bonitasoft.studio.actors.repository.OrganizationFileStore;
import org.bonitasoft.studio.actors.repository.OrganizationRepositoryStore;
import org.bonitasoft.studio.actors.ui.wizard.page.OrganizationValidator;
import org.bonitasoft.studio.common.jface.BonitaErrorDialog;
import org.bonitasoft.studio.common.jface.FileActionDialog;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

/**
 * @author Romain Bioteau
 *
 */
public class ImportOrganizationHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final OrganizationRepositoryStore organizationStore = RepositoryManager.getInstance().getRepositoryStore(OrganizationRepositoryStore.class) ;
		final FileDialog fd = new FileDialog(Display.getDefault().getActiveShell(),SWT.OPEN) ;
		fd.setFilterExtensions(new String[]{"*.xml;*.zip"}) ;
		final String filePath = fd.open() ;
		if(filePath != null){
			final IProgressService service = PlatformUI.getWorkbench().getProgressService() ;
			try {
				service.run(false, false, new IRunnableWithProgress() {

					@Override
					public void run(final IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
						monitor.beginTask(Messages.importingOrganization, IProgressMonitor.UNKNOWN) ;
						FileInputStream fis = null ;
						try {
							fis = new FileInputStream(filePath);
							final String id =	new File(filePath).getName() ;
							FileActionDialog.setThrowExceptionOnCancel(true);
							final OrganizationFileStore file = organizationStore.importInputStream(id, fis) ;

							if(file != null && file.isCorrectlySyntaxed()){
                                final IStatus status = new OrganizationValidator().validate(file.getContent());
                                if (!status.isOK()) {
                                    MessageDialog.openWarning(Display.getDefault().getActiveShell(), Messages.importOrganizationWithWarningTitle,
                                            Messages.bind(Messages.importOrganizationWithWarningMessage,status.getMessage()));
                                } else {
                                    MessageDialog.openInformation(Display.getDefault().getActiveShell(), Messages.importOrganizationSuccessfullTitle,
                                            Messages.importOrganizationSuccessfullMessage);
                                }

							} else {
								fis.close();
								if( file != null){
									file.delete();
								}
								MessageDialog.openError(Display.getDefault().getActiveShell(),  Messages.importOrganizationFailedTitle, Messages.importOrganizationFailedMessage);
							}
						} catch(final CancellationException ce) {
							String message = Messages.importOrganizationCancelledMessage;
							if(ce.getMessage() != null){
								message = Messages.importOrganizationCancelledMessage + ":\n"+ce.getMessage();
							}
							MessageDialog.openWarning(Display.getDefault().getActiveShell(), Messages.importOrganizationCancelledTitle, message);
						} catch (final Exception e) {
							BonitaStudioLog.error(e) ;
							final OrganizationFileStore file = organizationStore.getChild(new File(filePath).getName().replace(".xml", "."+OrganizationRepositoryStore.ORGANIZATION_EXT));
							if( file != null){
								file.delete();
							}
							new BonitaErrorDialog(Display.getDefault().getActiveShell(), Messages.importOrganizationFailedTitle, Messages.importOrganizationFailedMessage,e).open();
						}finally{
							FileActionDialog.setThrowExceptionOnCancel(false);
							if(fis != null){
								try {
									fis.close() ;
								} catch (final IOException e) {

								}
							}
						}

					}
				});
			} catch (final Exception e) {
				BonitaStudioLog.error(e) ;
			} finally {
				FileActionDialog.setThrowExceptionOnCancel(false);
			}

		}

		return null;
	}

}
