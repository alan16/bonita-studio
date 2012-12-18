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
package org.bonitasoft.studio.importer.test.bonita4;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.bonitasoft.studio.importer.bonita4.XPDLToProc;
import org.bonitasoft.studio.model.process.ANDGateway;
import org.bonitasoft.studio.model.process.Activity;
import org.bonitasoft.studio.model.process.Actor;
import org.bonitasoft.studio.model.process.CallActivity;
import org.bonitasoft.studio.model.process.DataType;
import org.bonitasoft.studio.model.process.Element;
import org.bonitasoft.studio.model.process.EnumType;
import org.bonitasoft.studio.model.process.FlowElement;
import org.bonitasoft.studio.model.process.MainProcess;
import org.bonitasoft.studio.model.process.Pool;
import org.bonitasoft.studio.model.process.SequenceFlow;
import org.bonitasoft.studio.model.process.Task;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Mickael Istria
 *
 */
public class TestImportXPDL extends TestCase {

    public org.eclipse.emf.common.util.URI toEMFURI(File file) throws MalformedURLException {
        org.eclipse.emf.common.util.URI res = URI.createFileURI(file.getAbsolutePath());
        return res;
    }

    @Test
    public void testCarpool() throws Exception {
        File destFile = new File("dest" + System.currentTimeMillis() + ".proc");
        destFile.createNewFile();
        destFile.deleteOnExit();
        URL xpdlResource = FileLocator.toFileURL(getClass().getResource("carpool.xpdl"));
        XPDLToProc xpdlToProc = new XPDLToProc();
        destFile = xpdlToProc.createDiagram(xpdlResource,  new NullProgressMonitor());

        ResourceSet resourceSet = new ResourceSetImpl();
        Resource resource = resourceSet.getResource(toEMFURI(destFile), true);
        MainProcess mainProcess = (MainProcess)resource.getContents().get(0);
        Pool process = (Pool)mainProcess.getElements().get(0);

        int steps = 0;
        int transitions = 0;
        List<Task> tasks = new ArrayList<Task>();
        int connectors = 0;

        for (Element item : process.getElements()) {
            if (item instanceof FlowElement) {
                steps++;
            }
            if (item instanceof Task) {
                tasks.add((Task)item);
            }
            if (item instanceof Activity) {
                connectors += ((Activity)item).getConnectors().size();
            }
        }
        for (Element item : process.getConnections()) {
            if (item instanceof SequenceFlow) {
                transitions++;
            }
        }

        assertEquals("Not the same number of steps as expected", 11, steps);
        assertEquals("Not the same number of tasks as expected", 3, tasks.size());
        assertEquals("Not the same number of transitions as expected", 14, transitions);
        assertEquals("Not the same number of data as expected", 4, process.getData().size());

        assertEquals("Not the same number of group as expected", 1, mainProcess.getActors().size());
        assertEquals("Not the same number of connectors as expected", 7, connectors);

        Actor group = mainProcess.getActors().get(0);
        for (Task task : tasks) {
            assertEquals("Task has no group", group, task.getActor());
        }

    }

    @Test
    public void testApprovalWorkflow() throws Exception {
        File destFile = new File("dest" + System.currentTimeMillis() + ".proc");
        destFile.createNewFile();
        destFile.deleteOnExit();
        URL xpdlResource = FileLocator.toFileURL(getClass().getResource("ApprovalWorkflow.xpdl"));
        XPDLToProc xpdlToProc = new XPDLToProc();
        destFile = xpdlToProc.createDiagram(xpdlResource, new NullProgressMonitor());

        ResourceSet resourceSet = new ResourceSetImpl();
        Resource resource = resourceSet.getResource(toEMFURI(destFile), true);
        MainProcess mainProcess = (MainProcess)resource.getContents().get(0);
        Pool pool = (Pool) mainProcess.getElements().get(0);

        int steps = 0;
        int transitions = 0;
        List<Task> tasks = new ArrayList<Task>();
        int connectors = 0;
        int deadlines = 0;
        Activity approval = null;
        for (Element item : pool.getElements()) {
            if (item instanceof FlowElement) {
                steps++;
            }
            if (item instanceof Task) {
                tasks.add((Task)item);
            }
            if (item instanceof Activity) {
                connectors += ((Activity)item).getConnectors().size();
            }
            if (item.getName().equals("Approval")) {
                approval = (Activity)item;
            }
        }
        for (Element item : pool.getConnections()) {
            if (item instanceof SequenceFlow) {
                transitions++;
            }
        }

        int enums = 0;
        for (DataType dataType : mainProcess.getDatatypes()) {
            if (dataType instanceof EnumType) {
                enums++;
            }
        }
        assertEquals("Not the same number of Enum as expected", 1, enums);

        assertEquals("Not the same number of steps as expected", 6, steps);
        assertEquals("Not the same number of tasks as expected", 2, tasks.size());
        assertEquals("Not the same number of transitions as expected", 6, transitions);
        assertEquals("Not the same number of global data as expected", 1, pool.getData().size());
        assertEquals("Not the same number of data as expected for task 'Approval'", 1, approval.getData().size());

        assertEquals("Not the same number of group as expected", 2, mainProcess.getActors().size());
        assertEquals("Not the same number of connectors as expected", 2, connectors);
        assertEquals("Not the same number of deadlines as expected", 0, deadlines);
        for (Task task : tasks) {
            assertNotNull("Task has no group",  task.getActor());
        }
    }

    @Test
    public void testWebSale() throws Exception {
        File destFile = new File("dest" + System.currentTimeMillis() + ".proc");
        destFile.createNewFile();
        destFile.deleteOnExit();
        URL xpdlResource = FileLocator.toFileURL(getClass().getResource("WebSale.xpdl"));
        XPDLToProc xpdlToProc = new XPDLToProc();
        destFile = xpdlToProc.createDiagram(xpdlResource,  new NullProgressMonitor());

        ResourceSet resourceSet = new ResourceSetImpl();
        Resource resource = resourceSet.getResource(toEMFURI(destFile), true);
        MainProcess mainProcess = (MainProcess)resource.getContents().get(0);
        Pool process = (Pool)mainProcess.getElements().get(0);

        int steps = 0;
        int transitions = 0;
        List<Task> tasks = new ArrayList<Task>();
        int connectors = 0;
        int deadlines = 0;
        Activity approval = null;
        for (Element item : process.getElements()) {
            if (item instanceof FlowElement) {
                steps++;
            }
            if (item instanceof Task) {
                tasks.add((Task)item);
            }
            if (item instanceof Activity) {
                connectors += ((Activity)item).getConnectors().size();
            }
            if (item.getName().equals("SalesReview")) {
                approval = (Activity)item;
            }
        }
        for (Element item : process.getConnections()) {
            if (item instanceof SequenceFlow) {
                transitions++;
            }
        }

        int enums = 0;
        for (DataType dataType : mainProcess.getDatatypes()) {
            if (dataType instanceof EnumType) {
                enums++;
            }
        }
        assertEquals("Not the same number of Enum as expected", 2, enums);

        assertEquals("Not the same number of steps as expected", 9, steps);
        assertEquals("Not the same number of tasks as expected", 4, tasks.size());
        assertEquals("Not the same number of transitions as expected", 11, transitions);
        assertEquals("Not the same number of global data as expected", 4, process.getData().size());
        assertEquals("Not the same number of data as expected for task 'Approval'", 1, approval.getData().size());

        assertEquals("Not the same number of group as expected", 2, mainProcess.getActors().size());
        assertEquals("Not the same number of connectors as expected", 3, connectors);
        assertEquals("Not the same number of deadlines as expected", 0, deadlines);

        for (Task task : tasks) {
            assertNotNull("Task has no group",task.getActor());
        }

    }

    @Test
    public void testSubFlow() throws Exception {
        File destFile = new File("dest" + System.currentTimeMillis() + ".proc");
        destFile.createNewFile();
        destFile.deleteOnExit();
        URL xpdlResource = FileLocator.toFileURL(getClass().getResource("subflow_proed.xpdl"));
        XPDLToProc xpdlToProc = new XPDLToProc();
        destFile = xpdlToProc.createDiagram(xpdlResource, new NullProgressMonitor());

        ResourceSet resourceSet = new ResourceSetImpl();
        Resource resource = resourceSet.getResource(toEMFURI(destFile), true);
        MainProcess mainProcess = (MainProcess)resource.getContents().get(0);
        Pool process = (Pool)mainProcess.getElements().get(0);

        CallActivity subprocess = null;
        List<ANDGateway> gateways = new ArrayList<ANDGateway>();
        for (Element item : process.getElements()) {
            if (item instanceof CallActivity) {
                subprocess = (CallActivity)item;
            }
            if (item instanceof ANDGateway) {
                gateways.add((ANDGateway)item);
            }
        }
        assertEquals("Wrong subflow name", "subflow", subprocess.getCalledActivityName().getContent());
        assertEquals("Wrong number of gateways", 2, gateways.size());
    }

    @Ignore
    public void testMultiInstantiation() throws Exception {
        File destFile = new File("dest" + System.currentTimeMillis() + ".proc");
        destFile.createNewFile();
        destFile.deleteOnExit();
        URL xpdlResource = FileLocator.toFileURL(getClass().getResource("multiInstantiation.xpdl"));
        XPDLToProc xpdlToProc = new XPDLToProc();
        destFile = xpdlToProc.createDiagram(xpdlResource, new NullProgressMonitor());

        ResourceSet resourceSet = new ResourceSetImpl();
        Resource resource = resourceSet.getResource(toEMFURI(destFile), true);
        MainProcess mainProcess = (MainProcess)resource.getContents().get(0);
        Pool process = (Pool)mainProcess.getElements().get(0);

        Activity activity = null;
        for (Element item : process.getElements()) {
            if (item instanceof Activity) {
                activity = (Activity)item;
            }
        }
        assertNotNull("Missing a multiinstantiation", activity.getMultiInstantiation());
    }
}
