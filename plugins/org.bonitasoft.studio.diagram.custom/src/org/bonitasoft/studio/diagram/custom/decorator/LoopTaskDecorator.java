/**
 * Copyright (C) 2009 BonitaSoft S.A.
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
package org.bonitasoft.studio.diagram.custom.decorator;

import org.bonitasoft.studio.common.diagram.tools.FiguresHelper;
import org.bonitasoft.studio.common.figures.DecoratorSVGFigure;
import org.bonitasoft.studio.model.process.Activity;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.bonitasoft.studio.model.process.CallActivity;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.diagram.core.listener.DiagramEventBroker;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget.Direction;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.Size;

/**
 * @author Aurelien Pupier
 *
 */
public class LoopTaskDecorator extends AbstractBonitaDecorator {
	
	
	public LoopTaskDecorator(IDecoratorTarget decoratorTarget, ActivityDecoratorProvider activityDecoratorProvider) {
		super(decoratorTarget);
	}

	@Override
	protected void activateDiagramEventBroker(EObject node,
			TransactionalEditingDomain domain, EObject model) {
		DiagramEventBroker.getInstance(domain).addNotificationListener(node,ProcessPackage.eINSTANCE.getContainer_Elements(),notificationListener); 
		DiagramEventBroker.getInstance(domain).addNotificationListener(node,ProcessPackage.eINSTANCE.getActivity_IsLoop(),notificationListener); 
	}

	@Override
	protected void deactivateDiagramEventBroker(IGraphicalEditPart gep) {
		DiagramEventBroker.getInstance(gep.getEditingDomain()).removeNotificationListener(gep.resolveSemanticElement(),ProcessPackage.eINSTANCE.getContainer_Elements(), notificationListener);
		DiagramEventBroker.getInstance(gep.getEditingDomain()).removeNotificationListener(gep.resolveSemanticElement(),ProcessPackage.eINSTANCE.getActivity_IsLoop(), notificationListener);
	}

	@Override
	protected DecoratorSVGFigure getImageDecorator() {
		return FiguresHelper.getDecoratorFigure(FiguresHelper.LOOP_DECORATOR) ;
	}
	

	@Override
	protected Direction getDirection() {
		return IDecoratorTarget.Direction.SOUTH;
	}

	@Override
	protected boolean isAppearing(EObject activity) {
		return activity instanceof Activity &&  ((Activity)activity).getIsLoop() && !(activity instanceof CallActivity);
	}
	
	@Override
	protected int getDelta(Node view) {
		int delta = -1 ;
		if(view != null){
			int height = ((Size)view.getLayoutConstraint()).getHeight() ; 
			if(height != 0){
				delta = - (height / 25) ; 
				if(delta > -1 ){
					delta = -1 ;
				}
			}else{
				delta = - 1 ; 
			}

		}
		return delta;
	}
	
}
