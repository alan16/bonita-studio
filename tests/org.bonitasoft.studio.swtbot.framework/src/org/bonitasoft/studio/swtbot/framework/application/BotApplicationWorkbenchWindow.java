/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.studio.swtbot.framework.application;

import org.bonitasoft.studio.common.jface.SWTBotConstants;
import org.bonitasoft.studio.swtbot.framework.application.menu.AbstractBotMenu;
import org.bonitasoft.studio.swtbot.framework.application.menu.BotEditMenu;
import org.bonitasoft.studio.swtbot.framework.diagram.BotProcessDiagramPerspective;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.matchers.WithId;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;

/**
 * Application workbench.
 *
 * @author Joachim Segala
 */
public class BotApplicationWorkbenchWindow extends AbstractBotMenu {

    public BotApplicationWorkbenchWindow(final SWTGefBot bot) {
        super(bot);
    }

    public BotProcessDiagramPerspective createNewDiagram() {
        final long timebeforeCreatenewDiagram = System.currentTimeMillis();
        final int nbEditorsBefore = bot.editors().size();
        bot.waitUntil(Conditions.waitForWidget(WithId.withId(SWTBotConstants.SWTBOT_ID_MAIN_SHELL)), 40000);
        bot.waitUntil(Conditions.shellIsActive(bot.shellWithId(SWTBotConstants.SWTBOT_ID_MAIN_SHELL).getText()), 40000);
        bot.waitUntil(Conditions.widgetIsEnabled(bot.menu("Diagram")), 40000);
        final SWTBotMenu menu = bot.menu("Diagram");
        menu.menu("New").click();
        bot.waitUntil(new ICondition() {

            @Override
            public boolean test() throws Exception {
                return nbEditorsBefore + 1 == bot.editors().size();
            }

            @Override
            public void init(final SWTBot bot) {
            }

            @Override
            public String getFailureMessage() {
                return "Editor for new diagram has not been opened";
            }
        }, 30000, 100);
        System.out.println("Time to create a new diagram: " + String.valueOf(System.currentTimeMillis() - timebeforeCreatenewDiagram));

        return new BotProcessDiagramPerspective(bot);
    }

    public BotOpenDiagramDialog open() {
        bot.toolbarButton("Open").click();
        bot.waitUntil(Conditions.shellIsActive("Open an existing diagram"));
        return new BotOpenDiagramDialog(bot);
    }

    public BotApplicationWorkbenchWindow save() {
        bot.toolbarButton("Save").click();
        bot.waitUntil(new DefaultCondition() {

            @Override
            public boolean test() throws Exception {
                return !BotApplicationWorkbenchWindow.this.bot.activeEditor().isDirty();
            }

            @Override
            public String getFailureMessage() {
                return "The save took too much time";
            }
        });
        return this;
    }

    public BotApplicationWorkbenchWindow close() {
        final int nbEditorsBefore = bot.editors().size();
        bot.waitUntil(Conditions.widgetIsEnabled(bot.menu("Diagram")), 40000);
        final SWTBotMenu menu = bot.menu("Diagram");
        menu.menu("Close").click();
        bot.waitUntil(new ICondition() {

            @Override
            public boolean test() throws Exception {
                return nbEditorsBefore - 1 == bot.editors().size();
            }

            @Override
            public void init(final SWTBot bot) {
            }

            @Override
            public String getFailureMessage() {
                return "Editor for new diagram has not been opened";
            }
        }, 30000, 100);
        return this;
    }

    public BotEditMenu editMenu() {
        openMenu("Edit");
        return new BotEditMenu(bot);
    }

}
