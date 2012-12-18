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

package org.bonitasoft.studio.diagram.custom.editPolicies;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.studio.common.NamingUtils;
import org.bonitasoft.studio.common.Pair;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.gmf.tools.GMFTools;
import org.bonitasoft.studio.diagram.custom.Activator;
import org.bonitasoft.studio.diagram.custom.BonitaNodesElementTypeResolver;
import org.bonitasoft.studio.diagram.custom.Messages;
import org.bonitasoft.studio.diagram.custom.figures.DropDownMenuEventFigure;
import org.bonitasoft.studio.diagram.custom.figures.DropDownMenuFigure;
import org.bonitasoft.studio.diagram.custom.figures.SlideMenuBarFigure;
import org.bonitasoft.studio.model.process.ANDGateway;
import org.bonitasoft.studio.model.process.Activity;
import org.bonitasoft.studio.model.process.CallActivity;
import org.bonitasoft.studio.model.process.CatchLinkEvent;
import org.bonitasoft.studio.model.process.Connection;
import org.bonitasoft.studio.model.process.EndErrorEvent;
import org.bonitasoft.studio.model.process.EndEvent;
import org.bonitasoft.studio.model.process.EndMessageEvent;
import org.bonitasoft.studio.model.process.EndSignalEvent;
import org.bonitasoft.studio.model.process.EndTerminatedEvent;
import org.bonitasoft.studio.model.process.Event;
import org.bonitasoft.studio.model.process.InclusiveGateway;
import org.bonitasoft.studio.model.process.IntermediateCatchMessageEvent;
import org.bonitasoft.studio.model.process.IntermediateCatchSignalEvent;
import org.bonitasoft.studio.model.process.IntermediateThrowMessageEvent;
import org.bonitasoft.studio.model.process.IntermediateThrowSignalEvent;
import org.bonitasoft.studio.model.process.ProcessFactory;
import org.bonitasoft.studio.model.process.ReceiveTask;
import org.bonitasoft.studio.model.process.ScriptTask;
import org.bonitasoft.studio.model.process.SendTask;
import org.bonitasoft.studio.model.process.ServiceTask;
import org.bonitasoft.studio.model.process.StartErrorEvent;
import org.bonitasoft.studio.model.process.StartEvent;
import org.bonitasoft.studio.model.process.StartMessageEvent;
import org.bonitasoft.studio.model.process.StartSignalEvent;
import org.bonitasoft.studio.model.process.StartTimerEvent;
import org.bonitasoft.studio.model.process.Task;
import org.bonitasoft.studio.model.process.ThrowLinkEvent;
import org.bonitasoft.studio.model.process.XORGateway;
import org.bonitasoft.studio.model.process.diagram.edit.parts.MainProcessEditPart;
import org.bonitasoft.studio.model.process.diagram.providers.ProcessElementTypes;
import org.bonitasoft.studio.pics.Pics;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.ZoomListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gmf.runtime.common.ui.services.icon.IconService;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Listener;


/**
 * @author Romain Bioteau
 * @author Aurelien Pupier improve resource management (free handle of image)
 */
public class ActivitySwitchEditPolicy extends AbstractSingleSelectionEditPolicy implements ZoomListener,LayerConstants {


    public static final String SWITCH_TYPE_ROLE = "switchActivityType"; //$NON-NLS-1$
    private static final IFigure EMPTY_FIGURE = new RectangleFigure() ;
    static {
        EMPTY_FIGURE.setSize(20, 20);
        EMPTY_FIGURE.setVisible(false);
    }


    private final List<IFigure> figures;
    private IFigure layer;
    private SlideMenuBarFigure toolBarFigure ;
    private ImageFigure toolImage ;
    private FreeformLayer composite;

    public ActivitySwitchEditPolicy(){
        super();
        figures = new ArrayList<IFigure>();

    }

    private DropDownMenuFigure dropMenu;
    private IFigure referenceFigure;
    private Image iconImage;
    private ImageFigure moreToolImage;
    private DropDownMenuEventFigure dropEventMenu;
    private ZoomManager zoomManager;



    private Pair<IFigure, MouseListener> createClickableItem(final Point location, final IGraphicalEditPart host, final IElementType type) {
        ImageFigure image  ;
        /*Activity*/
        AdapterFactory factory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
        if(type.equals(ProcessElementTypes.Task_2004) || type.equals(ProcessElementTypes.Task_3005)){
            image = new ImageFigure(Pics.getImage("decoration/","task")); //$NON-NLS-1$ //$NON-NLS-2$
        }else if(type.equals(ProcessElementTypes.CallActivity_2036) || type.equals(ProcessElementTypes.CallActivity_3063)){
            image = new ImageFigure(new AdapterFactoryLabelProvider(factory).getImage(ProcessFactory.eINSTANCE.createCallActivity()));
        }else if(type.equals(ProcessElementTypes.ReceiveTask_2025)){
            image = new ImageFigure(Pics.getImage("decoration/","enveloppe-vide"));
        }else if(type.equals(ProcessElementTypes.SendTask_2026)){
            image = new ImageFigure(Pics.getImage("decoration/","enveloppe-pleine"));
        }else if(type.equals(ProcessElementTypes.ServiceTask_2027)){
            image = new ImageFigure(Pics.getImage("decoration/","serviceTask"));
        }else if(type.equals(ProcessElementTypes.ScriptTask_2028)){
            image = new ImageFigure(Pics.getImage("decoration/","scriptTask"));
        }else{
            image = new ImageFigure(IconService.getInstance().getIcon(type));
        }
        image.setSize(16, 16);
        image.setToolTip(new Label(NamingUtils.getPaletteText(false, type.getEClass())));



        MouseListener listener = new MouseListener() {

            public void mouseReleased(MouseEvent me) {
            }

            public void mousePressed(MouseEvent me) {
                ShapeNodeEditPart node = (ShapeNodeEditPart) host.getAdapter(ShapeNodeEditPart.class);
                if(node != null){
                    GraphicalEditPart targetEditPart = GMFTools.convert(type.getEClass(),node, new BonitaNodesElementTypeResolver(),GMFTools.PROCESS_DIAGRAM);
                    EditPart p = targetEditPart.getParent() ;
                    while(!(p instanceof MainProcessEditPart)){
                        p = p.getParent() ;
                    }
                    p.getViewer().select(targetEditPart);
                    p.getViewer().flush() ;
                    //consume the event in order that the editpart keep the focus
                    me.consume();
                }
            }

            public void mouseDoubleClicked(MouseEvent me) {

            }
        };
        figures.add(image);

        return new Pair<IFigure, MouseListener>(image, listener);
    }

    private IFigure createClickableFigure(final Point location, final IGraphicalEditPart host, final IElementType type) {
        Pair<IFigure, MouseListener> item = createClickableItem(location, host, type);
        item.getFirst().addMouseListener(item.getSecond());
        return item.getFirst();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void hideFeedback() {
        List<IFigure> figToDelete = new ArrayList<IFigure>();
        if(layer != null){
            List<Object> children = layer.getChildren();
            for (Object fig : children){
                if(figures.contains(fig)){
                    if(fig instanceof IFigure) {
                        figToDelete.add((IFigure) fig);
                    }
                }
            }
            figures.clear();
            for(IFigure fig : figToDelete){
                layer.remove(fig);
            }
        }
        if(iconImage != null && !iconImage.isDisposed()){
            iconImage.dispose();
        }

    }


    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.SelectionEditPolicy#showSelection()
     */
    @Override
    protected void showFeedback() {
        if(zoomManager.getZoom() > GMFTools.MINIMAL_ZOOM_DISPLAY){
            IGraphicalEditPart host2 = getHost();
            IGraphicalEditPart host = host2;
            if(host == null) {
                return ;
            }
            while(host.getAdapter(ShapeNodeEditPart.class) == null){
                host = (IGraphicalEditPart) host.getParent() ;
            }


            layer = getLayer(LayerConstants.HANDLE_LAYER);

            referenceFigure = SelectionFeedbackEditPolicy.getFeedbackFigure(host2);
            if (referenceFigure == null) {
                referenceFigure = getHostFigure();
            }
            Rectangle bounds = referenceFigure.getBounds().getCopy();

            //Get the absolute coordinate
            referenceFigure.translateToAbsolute(bounds);
            IFigure parentFigure = referenceFigure.getParent();
            while( parentFigure != null  ) {
                if(parentFigure instanceof Viewport) {
                    Viewport viewport = (Viewport)parentFigure;
                    bounds.translate(
                            viewport.getHorizontalRangeModel().getValue(),
                            viewport.getVerticalRangeModel().getValue());
                    parentFigure = parentFigure.getParent();
                }
                else {
                    parentFigure = parentFigure.getParent();
                }
            }


            composite = new FreeformLayer();
            composite.setSize(20, 20);


            composite.setLocation(new Point(bounds.getLeft().x + 10, bounds.getBottomRight().y));

            toolBarFigure = new SlideMenuBarFigure(composite);


            iconImage = Pics.getImage("Icon_tools.png", Activator.getDefault()) ;
            toolImage = new ImageFigure(iconImage);
            dropMenu = new DropDownMenuFigure(toolImage,composite,layer, Messages.switchTool);
            toolBarFigure.addToMenu(dropMenu);
            dropMenu.addToggleVisibilityListener(new Listener() {

                public void handleEvent(org.eclipse.swt.widgets.Event event) {
                    if(!dropMenu.isCollapsed()){
                        if(	getHost().getEditPolicy(BoundaryEventToolEditPolicy.BOUNDARY_TOOL_ROLE) != null){
                            if(((BoundaryEventToolEditPolicy) getHost().getEditPolicy(BoundaryEventToolEditPolicy.BOUNDARY_TOOL_ROLE)).getDropMenu() != null ){
                                ((BoundaryEventToolEditPolicy) getHost().getEditPolicy(BoundaryEventToolEditPolicy.BOUNDARY_TOOL_ROLE)).getDropMenu().collapse() ;
                            }
                        }
                    }
                }
            }) ;

            List<Pair<IFigure, MouseListener>> clickableItems = new ArrayList<Pair<IFigure, MouseListener>>();

            if(host2.getAdapter(Task.class) != null ){
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ServiceTask_2027));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.CallActivity_2036));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ReceiveTask_2025));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.SendTask_2026));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ScriptTask_2028));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.Activity_2006));
            }else if(host2.getAdapter(CallActivity.class) != null ){
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ServiceTask_2027));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.Task_2004));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ReceiveTask_2025));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.SendTask_2026));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ScriptTask_2028));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.Activity_2006 ));
            } else if(host2.getAdapter(ServiceTask.class) != null ){
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.CallActivity_2036));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.Task_2004));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ReceiveTask_2025));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.SendTask_2026));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ScriptTask_2028));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.Activity_2006 ));
            } else if(host2.getAdapter(ScriptTask.class) != null ){
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ServiceTask_2027));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.CallActivity_2036));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.Task_2004));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ReceiveTask_2025));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.SendTask_2026));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.Activity_2006 ));
            } else if(host2.getAdapter(ANDGateway.class) != null ){
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.XORGateway_2008 ));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.InclusiveGateway_2030));
            } else if(host2.getAdapter(XORGateway.class) != null){
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ANDGateway_2009 ));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.InclusiveGateway_2030));
            }else if(host2.getAdapter(InclusiveGateway.class) != null){
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.XORGateway_2008 ));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ANDGateway_2009 ));
                //		}else if(getHost().getAdapter(IntermediateCatchTimerEvent.class) != null){
                //			figure1 = createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.StartTimerEvent_2016 );
            }else if(host2.getAdapter(StartSignalEvent.class) != null){
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.StartMessageEvent_2010) );
                if(!isInSubProcessEventPool()){
                    clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.StartEvent_2002));
                }
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.StartTimerEvent_2016));
                if(isInSubProcessEventPool()){
                    clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.StartErrorEvent_2033));
                }
            }else if(host2.getAdapter(StartTimerEvent.class) != null){
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.StartMessageEvent_2010) );
                if(!isInSubProcessEventPool()){
                    clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.StartEvent_2002));
                }
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.StartSignalEvent_2022));
                if(isInSubProcessEventPool()){
                    clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.StartErrorEvent_2033));
                }
            }else if(host2.getAdapter(StartErrorEvent.class) != null
                    /*&& isInSubProcessEventPool()*/){//it must be in an EventSubProcessPool, notice no switch to Start allowed
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.StartMessageEvent_2010) );
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.StartSignalEvent_2022));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.StartTimerEvent_2016));
            }else if(host2.getAdapter(SendTask.class) != null){
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.Task_2004));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ServiceTask_2027));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.CallActivity_2036 ));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ReceiveTask_2025));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ScriptTask_2028));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.Activity_2006));
            }else if(host2.getAdapter(ReceiveTask.class) != null){
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.Task_2004));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ServiceTask_2027));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.CallActivity_2036 ));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.SendTask_2026));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ScriptTask_2028));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.Activity_2006));
            } else if(host2.getAdapter(Task.class) == null
                    && host2.getAdapter(CallActivity.class) == null
                    && host2.getAdapter(SendTask.class) == null
                    && host2.getAdapter(ReceiveTask.class) == null
                    && host2.getAdapter(Activity.class) != null){//automatic task
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ServiceTask_2027));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.Task_2004));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.CallActivity_2036 ));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ReceiveTask_2025));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.SendTask_2026));
                clickableItems.add(createClickableItem(new Point(0, 0), host2, ProcessElementTypes.ScriptTask_2028));
            }else if(host2.getAdapter(IntermediateCatchMessageEvent.class) != null){
                //clickableItems.add(createClickableItem(new Point(0, 0),getHost(), ProcessElementTypes.StartMessageEvent_2010));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.IntermediateThrowMessageEvent_2014));
                //	clickableItems.add(createClickableItem(new Point(0, 0),getHost(), ProcessElementTypes.EndMessageEvent_2011));
            }else if(host2.getAdapter(IntermediateThrowMessageEvent.class) != null){
                //	clickableItems.add(createClickableItem(new Point(0, 0),getHost(), ProcessElementTypes.StartMessageEvent_2010));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.IntermediateCatchMessageEvent_2013));
                //	clickableItems.add(createClickableItem(new Point(0, 0),getHost(), ProcessElementTypes.EndMessageEvent_2011));
            }else if(host2.getAdapter(EndMessageEvent.class) != null){
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndEvent_2003));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndErrorEvent_2029));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndSignalEvent_2023));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndTerminatedEvent_2035));
                //			clickableItems.add(createClickableItem(new Point(0, 0),getHost(), ProcessElementTypes.));
                //			clickableItems.add(createClickableItem(new Point(0, 0),getHost(), ProcessElementTypes.IntermediateCatchMessageEvent_2013));
            }else if(host2.getAdapter(CatchLinkEvent.class) != null){
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.ThrowLinkEvent_2019));
            }else if(host2.getAdapter(ThrowLinkEvent.class) != null){
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.CatchLinkEvent_2018));
            }else if(host2.getAdapter(IntermediateThrowSignalEvent.class) != null){
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.IntermediateCatchSignalEvent_2021));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndSignalEvent_2023));
            }else if(host2.getAdapter(IntermediateCatchSignalEvent.class) != null){
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.IntermediateThrowSignalEvent_2020));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndSignalEvent_2023));
            }else if(host2.getAdapter(EndSignalEvent.class) != null){
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndMessageEvent_2011));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndEvent_2003));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndErrorEvent_2029));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndTerminatedEvent_2035));
            }else if(host2.getAdapter(EndEvent.class) != null){
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndMessageEvent_2011));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndSignalEvent_2023));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndErrorEvent_2029));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndTerminatedEvent_2035));
            }else if(host2.getAdapter(StartEvent.class) != null){
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.StartMessageEvent_2010));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.StartTimerEvent_2016));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.StartSignalEvent_2022));
                if(isInSubProcessEventPool()){
                    clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.StartErrorEvent_2033));
                }
            }else if(host2.getAdapter(StartMessageEvent.class) != null){
                if(!isInSubProcessEventPool()){
                    clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.StartEvent_2002));
                }
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.StartTimerEvent_2016));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.StartSignalEvent_2022));
                if(isInSubProcessEventPool()){
                    clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.StartErrorEvent_2033));
                }
            }else if(host2.getAdapter(EndErrorEvent.class) != null){
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndMessageEvent_2011));;
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndSignalEvent_2023));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndEvent_2003));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndTerminatedEvent_2035));
            } else if(host2.getAdapter(EndTerminatedEvent.class) != null){
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndMessageEvent_2011));;
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndSignalEvent_2023));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndEvent_2003));
                clickableItems.add(createClickableItem(new Point(0, 0),host2, ProcessElementTypes.EndErrorEvent_2029));
            }

            for(Pair<IFigure, MouseListener> item : clickableItems){
                dropMenu.addToMenu(item.getFirst(),item.getSecond());
            }

            if(host instanceof IGraphicalEditPart){
                EObject model = host2.resolveSemanticElement();
                if(model instanceof Event
                        && !(model instanceof ReceiveTask)
                        && !(model instanceof SendTask)){
                    moreToolImage = new ImageFigure(Pics.getImage("red_down.gif")); //$NON-NLS-1$
                    dropEventMenu = new DropDownMenuEventFigure(moreToolImage,composite,layer);
                    dropEventMenu.addEventsFigure(addNoneEventFigures());
                    dropEventMenu.addEventsFigure(addMessageEventFigures());
                    dropEventMenu.addEventsFigure(addTimerEventFigures());
                    dropEventMenu.addEventsFigure(addSignalEventFigures());
                    dropEventMenu.addEventsFigure(addLinkEventFigures());
                    dropEventMenu.addEventsFigure(addErrorEventFigures());
                    dropEventMenu.addEventsFigure(addTerminatedEventFigures());
                    dropEventMenu.createSubMenuFigure();
                    dropEventMenu.paintElements();
                    figures.add(dropEventMenu) ;
                    dropMenu.addToMenu(dropEventMenu,null);
                }
            }


            dropMenu.createSubMenuFigure();
            //		dropMenu.paintElements();

            figures.add(referenceFigure);
            figures.add(composite);
            layer.add(composite);

            composite.addMouseMotionListener(new MouseMotionListener() {

                public void mouseMoved(MouseEvent me) {

                }

                public void mouseHover(MouseEvent arg0) {


                }

                public void mouseExited(MouseEvent me) {

                }

                public void mouseEntered(MouseEvent me) {
                    referenceFigure.translateToAbsolute(me.getLocation());
                    IFigure parentFigure = referenceFigure.getParent();
                    while( parentFigure != null  ) {
                        if(parentFigure instanceof Viewport) {
                            Viewport viewport = (Viewport)parentFigure;
                            me.getLocation().translate(
                                    viewport.getHorizontalRangeModel().getValue(),
                                    viewport.getVerticalRangeModel().getValue());
                            parentFigure = parentFigure.getParent();
                        }
                        else {
                            parentFigure = parentFigure.getParent();
                        }
                    }
                }

                public void mouseDragged(MouseEvent arg0) {

                }
            });
        }
    }



    public DropDownMenuFigure getDropMenu() {
        return dropMenu ;
    }

    private List<IFigure> addNoneEventFigures() {
        List<IFigure> result = new ArrayList<IFigure>();
        IGraphicalEditPart host = getHost();
        if(!isInSubProcessEventPool() && !(isEndEvent(host.resolveSemanticElement()) || hasIncomingConnection())){
            result.add(createClickableFigure(new Point(0, 0),getHost(), ProcessElementTypes.StartEvent_2002));
        } else {
            result.add(EMPTY_FIGURE);
        }
        result.add(EMPTY_FIGURE);
        result.add(EMPTY_FIGURE);
        if (!(isStartEvent(host.resolveSemanticElement()) || hasOutgoingConnection())) {
        	result.add(createClickableFigure(new Point(0, 0),getHost(), ProcessElementTypes.EndEvent_2003));
        } else {
        	result.add(EMPTY_FIGURE);
        }
        return result;
    }

    private List<IFigure> addLinkEventFigures() {
        List<IFigure> result = new ArrayList<IFigure>();
        result.add(EMPTY_FIGURE);
        result.add(createClickableFigure(new Point(0, 0),getHost(), ProcessElementTypes.CatchLinkEvent_2018));
        result.add(createClickableFigure(new Point(0, 0),getHost(), ProcessElementTypes.ThrowLinkEvent_2019));
        result.add(EMPTY_FIGURE);

        return result;
    }

    private List<IFigure> addSignalEventFigures() {
        List<IFigure> result = new ArrayList<IFigure>();
        IGraphicalEditPart host = getHost();
        if (!(isEndEvent(host.resolveSemanticElement()) || hasIncomingConnection())){
        	result.add(createClickableFigure(new Point(0, 0),getHost(), ProcessElementTypes.StartSignalEvent_2022));
        } else {
        	result.add(EMPTY_FIGURE);
        }
        result.add(createClickableFigure(new Point(0, 0),getHost(), ProcessElementTypes.IntermediateCatchSignalEvent_2021));
        result.add(createClickableFigure(new Point(0, 0),getHost(), ProcessElementTypes.IntermediateThrowSignalEvent_2020));
        if (!(isStartEvent(host.resolveSemanticElement()) || hasOutgoingConnection())) {
        	result.add(createClickableFigure(new Point(0, 0),getHost(), ProcessElementTypes.EndSignalEvent_2023));
        } else {
        	result.add(EMPTY_FIGURE);
        }
        return result;
    }

    private List<IFigure> addTimerEventFigures() {
        List<IFigure> result = new ArrayList<IFigure>();
        IGraphicalEditPart host = getHost();
        if (!(isEndEvent(host.resolveSemanticElement()) || hasIncomingConnection())){
        	result.add(createClickableFigure(new Point(0, 0),getHost(), ProcessElementTypes.StartTimerEvent_2016));
        } else {
        	result.add(EMPTY_FIGURE);
        }
        result.add(createClickableFigure(new Point(0, 0),getHost(), ProcessElementTypes.IntermediateCatchTimerEvent_2017));
        result.add(EMPTY_FIGURE);
        result.add(EMPTY_FIGURE);
        return result;
    }


    private List<IFigure> addMessageEventFigures() {
        List<IFigure> result = new ArrayList<IFigure>();
        IGraphicalEditPart host = getHost();
        if (!(isEndEvent(host.resolveSemanticElement()) || hasIncomingConnection())){
        	result.add(createClickableFigure(new Point(0, 0),getHost(), ProcessElementTypes.StartMessageEvent_2010));
        } else {
        	result.add(EMPTY_FIGURE);
        }
        result.add(createClickableFigure(new Point(0, 0),getHost(), ProcessElementTypes.IntermediateCatchMessageEvent_2013));
        result.add(createClickableFigure(new Point(0, 0),getHost(), ProcessElementTypes.IntermediateThrowMessageEvent_2014));
        if (!(isStartEvent(host.resolveSemanticElement()) || hasOutgoingConnection())) {
        	result.add(createClickableFigure(new Point(0, 0),getHost(), ProcessElementTypes.EndMessageEvent_2011));
        } else {
        	result.add(EMPTY_FIGURE);
        }
        return result;
    }

    private List<IFigure> addErrorEventFigures() {
        List<IFigure> result = new ArrayList<IFigure>();
        IGraphicalEditPart host = getHost();
        if(isInSubProcessEventPool() &&  !(isEndEvent(host.resolveSemanticElement()) || hasIncomingConnection())){
            result.add(createClickableFigure(new Point(0, 0),host, ProcessElementTypes.StartErrorEvent_2033));
        } else {
            result.add(EMPTY_FIGURE);
        }
        result.add(EMPTY_FIGURE);
        result.add(EMPTY_FIGURE);
        if (!(isStartEvent(host.resolveSemanticElement()) || hasOutgoingConnection())) {
        	result.add(createClickableFigure(new Point(0, 0),host, ProcessElementTypes.EndErrorEvent_2029));
        } else {
        	result.add(EMPTY_FIGURE);
        }
        return result;
    }

    private List<IFigure> addTerminatedEventFigures() {
        List<IFigure> result = new ArrayList<IFigure>();
        IGraphicalEditPart host = getHost();
        	result.add(EMPTY_FIGURE);
        	result.add(EMPTY_FIGURE);
        	result.add(EMPTY_FIGURE);
         if (!(isStartEvent(host.resolveSemanticElement()) || hasOutgoingConnection())) {
        	result.add(createClickableFigure(new Point(0, 0),host, ProcessElementTypes.EndTerminatedEvent_2035));
        } else {
        	result.add(EMPTY_FIGURE);
        }
        return result;
    }

    private boolean isInSubProcessEventPool() {
        EditPart host = getHost();
        return host instanceof IGraphicalEditPart
                && ModelHelper.isInEvenementialSubProcessPool(((IGraphicalEditPart)host).resolveSemanticElement());
    }
    
    
    private boolean isStartEvent(EObject element){
    	if ((element instanceof StartEvent) ||
    		(element instanceof StartMessageEvent) ||
    		(element instanceof StartTimerEvent) ||
    		(element instanceof StartSignalEvent) ||
    		(element instanceof StartErrorEvent) ){
    		return true;
    	}
    	return false;
    }
    
    private boolean isEndEvent(EObject element){
    	if ((element instanceof EndEvent) ||
        		(element instanceof EndMessageEvent) ||
        		(element instanceof EndTerminatedEvent) ||
        		(element instanceof EndSignalEvent) ||
        		(element instanceof EndErrorEvent) ){
        		return true;
        	}
        	return false;
    }
    	
    private boolean hasIncomingConnection(){
    	EditPart host = getHost();
    	EObject element = ((IGraphicalEditPart)host).resolveSemanticElement();
    	
    	List<Connection> connections = ModelHelper.getParentProcess(element).getConnections();
    	for (Connection connection : connections){
    		EObject target = connection.getTarget();
    		if (target.equals(element)){
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean hasOutgoingConnection(){
    	EditPart host = getHost();
    	EObject element = ((IGraphicalEditPart)host).resolveSemanticElement();
    	
    	List<Connection> connections = ModelHelper.getParentProcess(element).getConnections();
    	for (Connection connection : connections){
    		EObject source = connection.getSource();
    		if (source.equals(element)){
    			return true;
    		}
    	}
    	return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.SelectionEditPolicy#deactivate()
     */
    @Override
    public void deactivate() {
        super.deactivate();
        zoomManager.removeZoomListener(this);
        if(iconImage != null && !iconImage.isDisposed()){
            iconImage.dispose();
        }
        referenceFigure = null;
        toolImage = null;
        moreToolImage = null ;
        toolBarFigure = null;
    }

    @Override
    public void activate() {
        super.activate();
        zoomManager = ((DiagramRootEditPart) getHost().getRoot()).getZoomManager();
        zoomManager.addZoomListener(this) ;
    }

    public SlideMenuBarFigure getToolbarFigure() {
        return toolBarFigure;
    }

    public void zoomChanged(double zoom) {
        hideSelection();
    }


}
