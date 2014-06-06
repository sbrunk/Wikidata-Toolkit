package org.wikidata.wdtk.dumpfiles.constraint;

/*
 * #%L
 * Wikidata Toolkit Dump File Handling
 * %%
 * Copyright (C) 2014 Wikidata Toolkit Developers
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;

/**
 * 
 * @author Julian Mendez
 * 
 */
public class ConstraintConflictsWith implements Constraint {

	final List<PropertyValues> list = new ArrayList<PropertyValues>();

	public ConstraintConflictsWith(PropertyIdValue constrainedProperty,
			List<PropertyValues> list) {
		Validate.notNull(list, "List cannot be null.");
		Validate.notNull(constrainedProperty, "Property cannot be null.");
		this.constrainedProperty = constrainedProperty;
		this.list.addAll(list);
	}

	final PropertyIdValue constrainedProperty;

	@Override
	public PropertyIdValue getConstrainedProperty() {
		return this.constrainedProperty;
	}

	public List<PropertyValues> getList() {
		return Collections.unmodifiableList(this.list);
	}

	@Override
	public <T> T accept(ConstraintVisitor<T> visitor) {
		Validate.notNull(visitor, "Visitor cannot be null.");
		return visitor.visit(this);
	}

}