/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.filter.capability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.opengis.filter.capability.TemporalOperator;
import org.opengis.filter.capability.TemporalOperators;

/**
 * 
 *
 * @source $URL$
 */
public class TemporalOperatorsImpl implements TemporalOperators {

    Set<TemporalOperator> operators;

    public TemporalOperatorsImpl() {
        this(new ArrayList());
    }

    public TemporalOperatorsImpl(Collection<TemporalOperator> operators) {
        this.operators = new LinkedHashSet<TemporalOperator>();
        this.operators.addAll(operators);
    }

    public Collection<TemporalOperator> getOperators() {
        return operators;
    }

    public TemporalOperator getOperator(String name) {
        for (TemporalOperator op : operators) {
            if (op.getName().equals(name)) {
                return op;
            }
        }
        return null;
    }

}
