/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.studio.swtbot.framework.expression;

import org.bonitasoft.studio.expression.editor.i18n.Messages;
import org.bonitasoft.studio.swtbot.framework.BotDialog;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;

/**
 * Expression editor dialog.
 *
 * @author Joachim Segala
 */
public class BotExpressionEditorDialog extends BotDialog {

    public BotExpressionEditorDialog(final SWTGefBot bot) {
        super(bot, Messages.editExpression);
    }

    public BotScriptExpressionEditor selectScriptTab() {
        bot.tableWithLabel(Messages.expressionTypeLabel).select("Script");
        bot.waitUntilWidgetAppears(Conditions.waitForWidget(WidgetMatcherFactory.widgetOfType(StyledText.class)));
        return new BotScriptExpressionEditor(bot, this);
    }

    public BotVariableExpressionEditor selectVariableTab() {
        bot.tableWithLabel(Messages.expressionTypeLabel).select("Variable");
        bot.waitUntilWidgetAppears(Conditions.waitForWidget(WidgetMatcherFactory.widgetOfType(Table.class)));
        return new BotVariableExpressionEditor(bot, this);
    }

    public BotConstantExpressionEditor selectConstantType() {
        bot.tableWithLabel(Messages.expressionTypeLabel).select("Constant");
        bot.waitUntilWidgetAppears(Conditions.waitForWidget(WidgetMatcherFactory.widgetOfType(Text.class)));
        return new BotConstantExpressionEditor(bot, this);
    }

    public BotFormFieldExpressionEditor selectFormFieldType() {
        bot.tableWithLabel(Messages.expressionTypeLabel).select("Form field");
        bot.waitUntilWidgetAppears(Conditions.waitForWidget(WidgetMatcherFactory.widgetOfType(Table.class)));
        return new BotFormFieldExpressionEditor(bot, this);
    }

}
