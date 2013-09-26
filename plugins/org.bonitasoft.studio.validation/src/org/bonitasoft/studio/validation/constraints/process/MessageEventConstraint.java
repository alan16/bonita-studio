/**
 * Copyright (C) 2010 BonitaSoft S.A.
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
package org.bonitasoft.studio.validation.constraints.process;

import java.util.List;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.diagram.custom.repository.DiagramRepositoryStore;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.process.AbstractCatchMessageEvent;
import org.bonitasoft.studio.model.process.AbstractProcess;
import org.bonitasoft.studio.model.process.Message;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.bonitasoft.studio.model.process.diagram.providers.ProcessMarkerNavigationProvider;
import org.bonitasoft.studio.validation.constraints.AbstractLiveValidationMarkerConstraint;
import org.bonitasoft.studio.validation.i18n.Messages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.validation.IValidationContext;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;

/**
 * @author Romain Bioteau
 *
 */
public class MessageEventConstraint extends AbstractLiveValidationMarkerConstraint {

	/* (non-Javadoc)
	 * @see org.eclipse.emf.validation.AbstractModelConstraint#validate(org.eclipse.emf.validation.IValidationContext)
	 */
	@Override
	protected IStatus performLiveValidation(IValidationContext ctx) {
		final EStructuralFeature featureTriggered = ctx.getFeature();
		if(featureTriggered.equals(ProcessPackage.Literals.MESSAGE__TARGET_PROCESS_EXPRESSION)){
			final Expression targetProcess = (Expression) ctx.getFeatureNewValue();
			if(targetProcess == null || targetProcess.getContent() == null || targetProcess.getContent().isEmpty()){
				return ctx.createFailureStatus(Messages.targetProcessNotSet);
			}
		}
		return ctx.createSuccessStatus();
	}

	@Override
	protected String getMarkerType(DiagramEditor editor) {
		return ProcessMarkerNavigationProvider.MARKER_TYPE;
	}

	@Override
	protected String getConstraintId() {
		return "org.bonitasoft.studio.validation.messageEventConstraint";
	}

	@Override
	protected IStatus performBatchValidation(IValidationContext ctx) {
		final Message event = (Message) ctx.getTarget();
		if(event.getTargetProcessExpression() == null || event.getTargetProcessExpression().getContent() == null || event.getTargetProcessExpression().getContent().isEmpty()){
			return ctx.createFailureStatus(Messages.targetProcessNotSet);
		}else{
			if(ExpressionConstants.CONSTANT_TYPE.equals(event.getTargetProcessExpression().getType())){
				final DiagramRepositoryStore store = (DiagramRepositoryStore) RepositoryManager.getInstance().getRepositoryStore(DiagramRepositoryStore.class);
				List<AbstractProcess> processes = store.findProcesses(event.getTargetProcessExpression().getContent());
				if(processes.isEmpty()){
					return ctx.createFailureStatus(Messages.bind(Messages.processDoesNotExist, event.getTargetProcessExpression().getContent(), event.getName()));
				}
				Expression targetElem = event.getTargetElementExpression();
				if(targetElem != null && targetElem.getContent() != null && !targetElem.getContent().isEmpty() && targetElem.getType().equals(ExpressionConstants.CONSTANT_TYPE)){
					String targetElemName = targetElem.getContent() ;
					for(AbstractProcess p : processes){
						List<AbstractCatchMessageEvent> events = ModelHelper.getAllItemsOfType(p, ProcessPackage.Literals.ABSTRACT_CATCH_MESSAGE_EVENT);
						for(AbstractCatchMessageEvent ev : events){
							if(targetElemName.equals(ev.getName())){
								return ctx.createSuccessStatus();
							}
						}
					}
					return ctx.createFailureStatus(Messages.bind(Messages.targetCatchMessageNotExists,targetElemName,event.getTargetProcessExpression().getContent()));  
				}
			}
		}
		return ctx.createSuccessStatus();
	}

}