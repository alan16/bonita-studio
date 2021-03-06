/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.model.process.builders;

import org.bonitasoft.studio.model.process.Actor;
import org.bonitasoft.studio.model.process.ProcessFactory;
import org.bonitasoft.studio.model.process.Task;

/**
 * @author Romain Bioteau
 *
 */
public class TaskBuilder extends ActivityBuilder<Task, TaskBuilder> {

    public static TaskBuilder createTaskBuilder() {
        return new TaskBuilder();
    }

    public TaskBuilder havingActor(final Actor actor) {
        getBuiltInstance().setActor(actor);
        return this;
    }

    public TaskBuilder overrideActorsOfTheLane() {
        getBuiltInstance().setOverrideActorsOfTheLane(true);
        return this;
    }

    public TaskBuilder reuseActorOfTheLane() {
        getBuiltInstance().setOverrideActorsOfTheLane(false);
        return this;
    }

    public TaskBuilder withPriority(final int priority) {
        getBuiltInstance().setPriority(priority);
        return this;
    }

    @Override
    protected Task newInstance() {
        return ProcessFactory.eINSTANCE.createTask();
    }

}
