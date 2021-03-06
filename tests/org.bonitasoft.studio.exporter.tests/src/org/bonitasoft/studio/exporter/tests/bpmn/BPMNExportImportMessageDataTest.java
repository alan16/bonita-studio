/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.exporter.tests.bpmn;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Map;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.exporter.bpmn.transfo.BonitaToBPMN;
import org.bonitasoft.studio.exporter.extension.BonitaModelExporterImpl;
import org.bonitasoft.studio.exporter.extension.IBonitaModelExporter;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.TableExpression;
import org.bonitasoft.studio.model.process.CatchMessageEvent;
import org.bonitasoft.studio.model.process.Element;
import org.bonitasoft.studio.model.process.Lane;
import org.bonitasoft.studio.model.process.MainProcess;
import org.bonitasoft.studio.model.process.Message;
import org.bonitasoft.studio.model.process.Pool;
import org.bonitasoft.studio.model.process.ThrowMessageEvent;
import org.bonitasoft.studio.model.process.diagram.edit.parts.MainProcessEditPart;
import org.bonitasoft.studio.test.swtbot.util.SWTBotTestUtil;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.gef.finder.SWTBotGefTestCase;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.spec.bpmn.di.util.DiResourceFactoryImpl;
import org.omg.spec.bpmn.model.DocumentRoot;
/**
 * @author Aurélien
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class BPMNExportImportMessageDataTest extends SWTBotGefTestCase {
	
	private static  MainProcess mainProcessAfterReimport;
	private static boolean isInitalized = false;
	private static ThrowMessageEvent throwMessageEvent;
	private static CatchMessageEvent catchMessageEvent;
	
	@Before
	public void init() throws IOException{
		if(!isInitalized){
			prepareTest();
		}
		isInitalized = true;
	}
	
	@Test
	public void testDataDefined(){
		Message message = throwMessageEvent.getEvents().get(0);
		TableExpression messageContent = message.getMessageContent();
		EList<Expression> messageDataLine = messageContent
				.getExpressions()
				.get(0)
				.getExpressions();
		assertEquals("messageContentId1", messageDataLine.get(0));
		assertNotNull(messageDataLine.get(1));
		
	}
	
	@Test
	public void testDataMappedCorrectlyOnThrow(){
		Message message = throwMessageEvent.getEvents().get(0);
		TableExpression messageContent = message.getMessageContent();
		EList<Expression> messageDataLine = messageContent
				.getExpressions()
				.get(0)
				.getExpressions();
		Expression messageValue = messageDataLine.get(1);
		assertEquals("data1", messageValue.getName());
		assertEquals(ExpressionConstants.VARIABLE_TYPE, messageValue.getType());
	}
	
	@Test
	public void testDataMappedCorrectlyOnCatch(){
		String eventId = catchMessageEvent.getEvent();
		Message message = throwMessageEvent.getEvents().get(0);
		assertEquals("Event name of catch not well defined",message.getName(), eventId);
	}
	
	protected void prepareTest() throws IOException {
		SWTBotTestUtil.importProcessWIthPathFromClass(bot, "MessageDataTestValue-1.0.bos", "Bonita 6.x", "MessageDataTestValue", BPMNExportImportMessageDataTest.class, false);
		SWTBotGefEditor editor1 = bot.gefEditor(bot.activeEditor().getTitle());
		SWTBotGefEditPart step1Part = editor1.getEditPart("Step1").parent();	
		MainProcessEditPart mped = (MainProcessEditPart) step1Part.part().getRoot().getChildren().get(0);
		IBonitaModelExporter exporter = new BonitaModelExporterImpl(mped) ;
		File bpmnFileExported = File.createTempFile("testMessageDataTestValue", ".bpmn");
		final boolean transformed = new BonitaToBPMN().transform(exporter, bpmnFileExported, new NullProgressMonitor());
		assertTrue("Error during export", transformed);


		ResourceSet resourceSet1 = new ResourceSetImpl();
		final Map<String, Object> extensionToFactoryMap = resourceSet1.getResourceFactoryRegistry().getExtensionToFactoryMap();
		final DiResourceFactoryImpl diResourceFactoryImpl = new DiResourceFactoryImpl();
		extensionToFactoryMap.put("bpmn", diResourceFactoryImpl);
		Resource resource2 = resourceSet1.createResource(URI.createFileURI(bpmnFileExported.getAbsolutePath()));		
		resource2.load(Collections.emptyMap());

		final DocumentRoot model2 = (DocumentRoot) resource2.getContents().get(0);
		
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				try {
					mainProcessAfterReimport = BPMNTestUtil.importBPMNFile(model2);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}				
			}
		});
		
		for(Element element : ((Lane)((Pool)mainProcessAfterReimport.getElements().get(0)).getElements().get(0)).getElements()){
			if(element instanceof ThrowMessageEvent){
				throwMessageEvent = (ThrowMessageEvent) element;
				break;
			}
		}
		
		for(Element element : ((Pool)mainProcessAfterReimport.getElements().get(1)).getElements()){
			if(element instanceof CatchMessageEvent){
				catchMessageEvent = (CatchMessageEvent) element;
				break;
			}
		}
	}
}
