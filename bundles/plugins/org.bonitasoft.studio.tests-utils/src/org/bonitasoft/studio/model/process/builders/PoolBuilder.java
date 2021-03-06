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

import org.bonitasoft.studio.model.parameter.builders.ParameterBuilder;
import org.bonitasoft.studio.model.process.Pool;
import org.bonitasoft.studio.model.process.ProcessFactory;

/**
 * @author Romain Bioteau
 *
 */
public class PoolBuilder extends ElementBuilder<Pool, PoolBuilder> {

    public static PoolBuilder create() {
        return new PoolBuilder();
    }

    public PoolBuilder havingElements(final ElementBuilder<?, ?>... elements) {
        if (elements != null) {
            for (final ElementBuilder<?, ?> elementBuilder : elements) {
                getBuiltInstance().getElements().add(elementBuilder.build());
            }
        }
        return getThis();
    }

    public PoolBuilder havingActors(final ActorBuilder... actors) {
        if (actors != null) {
            for (final ActorBuilder actor : actors) {
                getBuiltInstance().getActors().add(actor.build());
            }
        }
        return getThis();
    }

    public PoolBuilder havingDocuments(final DocumentBuilder... documents) {
        if (documents != null) {
            for (final DocumentBuilder document : documents) {
                getBuiltInstance().getDocuments().add(document.build());
            }
        }
        return getThis();
    }

    public PoolBuilder havingParameters(final ParameterBuilder... parameters) {
        if (parameters != null) {
            for (final ParameterBuilder parameter : parameters) {
                getBuiltInstance().getParameters().add(parameter.build());
            }
        }
        return getThis();
    }

    public PoolBuilder havingData(final DataBuilder<?, ?>... data) {
        if (data != null) {
            for (final DataBuilder<?, ?> dataBuilder : data) {
                getBuiltInstance().getData().add(dataBuilder.build());
            }
        }
        return getThis();
    }

    @Override
    protected Pool newInstance() {
        return ProcessFactory.eINSTANCE.createPool();
    }



}
