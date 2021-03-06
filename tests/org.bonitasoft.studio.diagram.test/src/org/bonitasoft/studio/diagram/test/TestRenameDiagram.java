/**
 * Copyright (C) 2014 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.diagram.test;

import org.assertj.core.api.Assertions;
import org.bonitasoft.studio.common.jface.FileActionDialog;
import org.bonitasoft.studio.model.process.MainProcess;
import org.bonitasoft.studio.preferences.BonitaPreferenceConstants;
import org.bonitasoft.studio.preferences.BonitaStudioPreferencesPlugin;
import org.bonitasoft.studio.swtbot.framework.application.BotApplicationWorkbenchWindow;
import org.bonitasoft.studio.swtbot.framework.diagram.BotProcessDiagramPerspective;
import org.bonitasoft.studio.test.swtbot.util.SWTBotTestUtil;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.gef.finder.SWTBotGefTestCase;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TestRenameDiagram extends SWTBotGefTestCase {


    // Before and After
    private static boolean disablePopup;
    private static boolean askRenameOnFirstSave;

    @BeforeClass
    public static void setUpBeforeClass() {
        disablePopup = FileActionDialog.getDisablePopup();
        askRenameOnFirstSave = BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore().getBoolean(BonitaPreferenceConstants.ASK_RENAME_ON_FIRST_SAVE);
        BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore().setValue(BonitaPreferenceConstants.ASK_RENAME_ON_FIRST_SAVE, false);
        FileActionDialog.setDisablePopup(true);
    }


    @AfterClass
    public static void tearDownAfterClass() {
        FileActionDialog.setDisablePopup(disablePopup);
        BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore().setValue(BonitaPreferenceConstants.ASK_RENAME_ON_FIRST_SAVE, askRenameOnFirstSave);
    }

    @Override
    @After
    public void tearDown(){
        BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore().setValue(BonitaPreferenceConstants.ASK_RENAME_ON_FIRST_SAVE, false);
        bot.saveAllEditors();
    }


    @Test
    public void testFirstSaveRenaming(){
        BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore().setValue(BonitaPreferenceConstants.ASK_RENAME_ON_FIRST_SAVE, true);
        SWTBotTestUtil.createNewDiagram(bot);
        SWTBotEditor botEditor = bot.activeEditor();
        SWTBotGefEditor gmfEditor = bot.gefEditor(botEditor.getTitle());
        MainProcess diagram = (MainProcess) ((IGraphicalEditPart) gmfEditor.mainEditPart().part()).resolveSemanticElement();
        String originalName = diagram.getName();
        bot.menu("Diagram").menu("Save").click();
        bot.waitUntil(Conditions.shellIsActive(org.bonitasoft.studio.common.Messages.openNameAndVersionDialogTitle));
        assertTrue("OK should be enabled",bot.button(IDialogConstants.OK_LABEL).isEnabled());

        final String newName = originalName +" renamed"+System.currentTimeMillis();
        bot.textWithLabel(org.bonitasoft.studio.common.Messages.name, 0).setText(newName);

        bot.button(IDialogConstants.OK_LABEL).click();
        final String editorTitle = newName + " (1.0)";
        bot.waitUntil(new ICondition() {

            public boolean test() throws Exception {
                return editorTitle.equals(bot.activeEditor().getTitle());
            }

            public void init(final SWTBot bot) {
            }

            public String getFailureMessage() {
                return "The editor title (" + bot.activeEditor().getTitle() + ") doesn't match the new name of the diagram " + editorTitle + "\n" +
                        "Please attach Studio log from .metadata/.logs folder on [BS-9265]";
            }
        });
        assertFalse("Editor is dirty", bot.activeEditor().isDirty());

        //Disable dialog
        SWTBotTestUtil.createNewDiagram(bot);
        botEditor = bot.activeEditor();
        gmfEditor = bot.gefEditor(botEditor.getTitle());
        diagram = (MainProcess) ((IGraphicalEditPart) gmfEditor.mainEditPart().part()).resolveSemanticElement();
        originalName = diagram.getName();
        bot.menu("Diagram").menu("Save").click();
        bot.waitUntil(Conditions.shellIsActive(org.bonitasoft.studio.common.Messages.openNameAndVersionDialogTitle));
        assertTrue("OK should be enabled",bot.button(IDialogConstants.OK_LABEL).isEnabled());

        bot.checkBox(org.bonitasoft.studio.application.i18n.Messages.doNotDisplayForOtherDiagrams).select();
        bot.button(IDialogConstants.OK_LABEL).click();
        assertEquals(originalName +" (1.0)", bot.activeEditor().getTitle());
        assertFalse("Editor is dirty", bot.activeEditor().isDirty());

        SWTBotTestUtil.createNewDiagram(bot);
        bot.menu("Diagram").menu("Save").click();
        bot.waitWhile(Conditions.shellIsActive("Progress Information"));
        assertFalse(bot.activeShell().getText().equals(org.bonitasoft.studio.common.Messages.openNameAndVersionDialogTitle));
        assertFalse("Editor is dirty", bot.activeEditor().isDirty());
    }

    @Test
    public void testRenameMenu(){
        SWTBotTestUtil.createNewDiagram(bot);

        bot.menu("Diagram").menu("Save").click();

        final SWTBotEditor botEditor = bot.activeEditor();
        final SWTBotGefEditor gmfEditor = bot.gefEditor(botEditor.getTitle());
        final MainProcess diagram = (MainProcess) ((IGraphicalEditPart) gmfEditor.mainEditPart().part()).resolveSemanticElement();
        final String originalName = diagram.getName();
        bot.menu("Diagram").menu("Rename...").click();
        bot.waitUntil(Conditions.shellIsActive(org.bonitasoft.studio.common.Messages.openNameAndVersionDialogTitle));

        assertTrue("OK should be enabled",bot.button(IDialogConstants.OK_LABEL).isEnabled());

        final String newName = originalName +" renamed"+System.currentTimeMillis();
        bot.textWithLabel(org.bonitasoft.studio.common.Messages.name, 0).setText(newName);

        bot.button(IDialogConstants.OK_LABEL).click();
        assertEquals(newName +" (1.0)", bot.activeEditor().getTitle());
        assertFalse("Editor is dirty", bot.activeEditor().isDirty());
    }


    @Test
    public void testRenameDiagramOnce() throws Exception {

        final boolean tmpDisablePopup = BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore()
                .getDefaultBoolean(BonitaPreferenceConstants.ASK_RENAME_ON_FIRST_SAVE);
        BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore().setValue(BonitaPreferenceConstants.ASK_RENAME_ON_FIRST_SAVE, true);

        SWTBotTestUtil.createNewDiagram(bot);
        SWTBotTestUtil.changeDiagramName(bot, "NewDiagramName");

        // TimeOUt if a the pop up has been reopened (see BS-9819)
        bot.waitWhile(Conditions.shellIsActive(org.bonitasoft.studio.common.Messages.openNameAndVersionDialogTitle));

        BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore().setValue(BonitaPreferenceConstants.ASK_RENAME_ON_FIRST_SAVE, tmpDisablePopup);
    }

    @Test
    public void testFormDiagramReopenedAfterRenaming() {
        bot.closeAllEditors();
        final BotApplicationWorkbenchWindow botApplicationWorkbenchWindow = new BotApplicationWorkbenchWindow(bot);
        final BotProcessDiagramPerspective botProcessDiagramPerspective = botApplicationWorkbenchWindow.createNewDiagram();
        botProcessDiagramPerspective.activeProcessDiagramEditor().selectElement("Step1");
        final SWTBotEditor diagramEditor = bot.activeEditor();
        botProcessDiagramPerspective.getDiagramPropertiesPart().selectApplicationTab().selectPageflowTab().addForm().finish();
        diagramEditor.show();
        diagramEditor.setFocus();
        botProcessDiagramPerspective.activeProcessDiagramEditor().selectDiagram();

        botProcessDiagramPerspective.getDiagramPropertiesPart().selectGeneralTab().selectDiagramTab().setName("newName");

        Assertions.assertThat(bot.editors()).hasSize(2);
    }

}
