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

package org.bonitasoft.studio.connectors.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bonitasoft.studio.common.Pair;
import org.bonitasoft.studio.common.jface.BonitaStudioFontRegistry;
import org.bonitasoft.studio.engine.i18n.Messages;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;

/**
 * @author Romain Bioteau
 *
 */
public class TestConnectorResultDialog extends Dialog {

    private static final String VALUES_FIELD_NAME = "values"; //$NON-NLS-1$
    private static final String CAPTIONS_FILED_NAME = "captions"; //$NON-NLS-1$
    private static final String RESULTS_TITLE = Messages.resultTitleLabel ;
    private static final String BACK_LABEL = Messages.backLabel ;

    private Map<String, Object> testResultAsMap ;
    private Set<String> testResultAsSet;
    private Composite mainComposite ;
    private ListViewer listViewer;
    private Throwable resultExecption;
    private TableViewer tableViewer;

    @SuppressWarnings("unchecked")
    public TestConnectorResultDialog(Shell parentShell,Object testResult) {
        super(parentShell);
        setShellStyle(SWT.MAX | SWT.CLOSE | SWT.APPLICATION_MODAL | SWT.RESIZE);
        if(testResult instanceof Map<?, ?>){
            testResultAsMap = (Map<String, Object>) testResult ;
        }else if(testResult instanceof Set<?>){
            testResultAsSet = (Set<String>) testResult ;
        }else if(testResult instanceof Throwable){
            resultExecption = (Throwable)testResult ;
        }
    }
    @SuppressWarnings("unchecked")
    @Override
    protected Control createDialogArea(Composite parent) {
        mainComposite = new Composite(parent,SWT.NONE);
        mainComposite.setLayoutData(GridDataFactory.fillDefaults().hint(400, 400).grab(true, true).create());
        mainComposite.setLayout(new GridLayout(2, false));

        if(testResultAsMap != null && !testResultAsMap.isEmpty()){
            Iterator<Entry<String, Object>> it = testResultAsMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Object> entry = it.next();

                boolean isRowSet = false ;
                Field captionsField = null ;
                Field valuesField  = null ;
                List<String> captions = null ;
                List<String> values = null ;
                try {
                    if(entry.getValue() != null && entry.getValue().getClass().getDeclaredField(CAPTIONS_FILED_NAME) != null && entry.getValue().getClass().getDeclaredField(VALUES_FIELD_NAME) != null){
                        isRowSet = true ;
                        captionsField =  entry.getValue().getClass().getDeclaredField(CAPTIONS_FILED_NAME);
                        captions = new ArrayList<String>();
                        captionsField.setAccessible(true);
                        captions = (List<String>) captionsField.get(entry.getValue() );

                        values = new ArrayList<String>();
                        valuesField =  entry.getValue().getClass().getDeclaredField(VALUES_FIELD_NAME);
                        valuesField.setAccessible(true);
                        values = (List<String>) valuesField.get(entry.getValue() );
                    }else{
                        isRowSet = false ;
                    }

                } catch (Exception e) {
                    isRowSet = false ;
                }

                //                if(isRowSet){
                //                    createTableViewer(mainComposite,captions,values);
                //                }else{
                new Label(mainComposite, SWT.NONE).setText(entry.getKey());
                Object value = entry.getValue();
                if(value == null || value instanceof String || value instanceof Long || value instanceof Integer){//TODO check other types
                    Text text = new Text(mainComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL) ;
                    text.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
                    if(entry.getValue() != null){
                        text.setText(entry.getValue().toString());
                    }else{
                        text.setText("NULL"); //$NON-NLS-1$
                    }
                    text.setEditable(false);
                    text.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
                } else if(value instanceof Document){
                    TreeViewer viewer = new TreeViewer(mainComposite, SWT.BORDER);
                    viewer.getTree().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

                    viewer.setContentProvider(new XmlDocumentContentProvider());
                    viewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 300).create());
                    viewer.setLabelProvider(new XmlLabelProvider());
                    viewer.setInput(value);
                    viewer.getTree().setFont(BonitaStudioFontRegistry.getCommentsFont());
                    viewer.expandAll();
                } else{
                    TreeViewer viewer = new TreeViewer(mainComposite, SWT.BORDER);
                    viewer.getTree().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

                    viewer.setContentProvider(new PojoBrowserContentProvider());
                    viewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 300).create());
                    viewer.setLabelProvider(new JavaUILabelProvider() {

                        @Override
                        public Image getImage(Object element) {
                            if( element instanceof Pair<?,?>) {
                                return getImage(((Pair<?,?>) element).getFirst());
                            }else if(element == null){
                                return null;
                            }
                            return super.getImage(element);
                        }
                        @Override
                        public String getText(Object item) {
                            if (item instanceof String) {
                                return (String)item;
                            } if( item instanceof Pair<?,?>) {

                                Object first = ((Pair<?,?>) item).getFirst();
                                if(first instanceof IType){
                                    return getText(first) + " : " + ((Pair<?,?>) item).getSecond().toString();
                                }else{
                                    return getText(first);
                                }
                            }else if (item instanceof IMember) {
                                return super.getText(item);
                            } else if(item != null){
                                return item.toString();
                            } else{
                                return null;
                            }
                        }
                    });
                    new Label(mainComposite, SWT.NONE);
                    Label warningLabel = new Label(mainComposite, SWT.NONE);
                    GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false).applyTo(warningLabel);
                    warningLabel.setText(Messages.testConnectorPOJOWarning);
                    viewer.setInput(value);

                }


            }
        }else if(testResultAsSet != null){
            listViewer = new ListViewer(mainComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
            GridData gd  = GridDataFactory.fillDefaults().grab(true, true).create() ;
            gd.horizontalSpan = 2 ;
            listViewer.getList().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
            listViewer.setContentProvider(new IStructuredContentProvider() {

                @Override
                public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                }

                @Override
                public void dispose() {
                }


                @Override
                public Object[] getElements(Object inputElement) {
                    return ((Set)inputElement).toArray();
                }
            });
            listViewer.setLabelProvider(new LabelProvider());
            listViewer.setInput(testResultAsSet);



        }else if(resultExecption != null){
            Label foundExceptionLabel = new Label(mainComposite, SWT.NONE);
            foundExceptionLabel.setText(Messages.exceptionFound);
            GridData gd = new GridData();
            gd.horizontalSpan = 2 ;
            foundExceptionLabel.setLayoutData(gd);
            Text text = new Text(mainComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL) ;
            text.setLayoutData(new GridData(GridData.FILL_BOTH));


            String exception = ""; //$NON-NLS-1$
            Throwable e = resultExecption;
            while (e != null) {
                exception = exception.concat(e.toString()+"\n"); //$NON-NLS-1$
                e = e.getCause() ;
            }

            text.setText(exception);
            text.setEditable(false);
            text.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        }else{
            Label successMessage = new Label(mainComposite, SWT.NONE);
            successMessage.setText(Messages.successMessage);
            GridData gd = new GridData();
            gd.horizontalSpan = 2 ;
            successMessage.setLayoutData(gd);
        }

        return parent;
    }

    // This will create the columns for the table
    private void createColumns(TableViewer viewer, List<String> captions) {


        for (int i = 0; i < captions.size(); i++) {
            TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
            column.getColumn().setText(captions.get(i));
            column.getColumn().setWidth(100);
            column.getColumn().setResizable(true);
            column.getColumn().setMoveable(true);
        }
        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
        getButton(IDialogConstants.CANCEL_ID).setText(BACK_LABEL);
    }



    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(RESULTS_TITLE);
    }

    @SuppressWarnings("unchecked")
    public void setResult(Object result) {
        if(result instanceof Map){
            testResultAsMap = (Map<String, Object>) result ;
        }else if(result instanceof Set){
            testResultAsSet = (Set<String>) result ;
        }
    }
}
