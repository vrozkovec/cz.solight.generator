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
package name.berries.wicket.components;

import java.io.Serializable;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

/**
 * Label, ktery je automaticky neviditelny v pripade, ze jeho model neobsahuje zadna data.
 *
 * @author rozkovec
 */
public class InvisibleLabel extends Label
{

	/**
	 * Construct.
	 *
	 * @param id
	 */
	public InvisibleLabel(String id)
	{
		super(id);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 * @param label
	 */
	public InvisibleLabel(String id, String label)
	{
		super(id, label);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 * @param model
	 */
	public InvisibleLabel(String id, IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 * @param label
	 */
	public InvisibleLabel(String id, Serializable label)
	{
		super(id, label);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		add(new InvisibleBehavior());
	}
}
