/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package name.berries.wicket.util.css;

import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

/**
 * @author vit
 */
public class CssClassRemover extends AttributeModifier
{

	/**
	 * Construct.
	 *
	 * @param cssClass
	 */
	public CssClassRemover(String cssClass)
	{
		this(new Model<String>(cssClass));
	}

	/**
	 * Construct.
	 *
	 * @param removeModel
	 */
	public CssClassRemover(IModel<?> removeModel)
	{
		super("class", removeModel);
	}

	@Override
	protected String newValue(String currentValue, String valueToRemove)
	{
		if (currentValue == null)
			return "";

		Set<String> classes = Sets.newHashSet(Splitter.on(" ").split(currentValue));
		classes.remove(valueToRemove);
		return Joiner.on(" ").join(classes);
	}

}
