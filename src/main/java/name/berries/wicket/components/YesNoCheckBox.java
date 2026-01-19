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

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons.Type;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.checkbox.bootstrapcheckbox.BootstrapCheckBoxPicker;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.checkbox.bootstrapcheckbox.BootstrapCheckBoxPickerConfig;

/**
 * @author vit
 */
public class YesNoCheckBox extends BootstrapCheckBoxPicker
{
	/**
	 * Construct.
	 *
	 * @param id
	 */
	public YesNoCheckBox(String id)
	{
		super(id);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 * @param model
	 */
	public YesNoCheckBox(String id, IModel<Boolean> model)
	{
		super(id, model);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 * @param config
	 */
	public YesNoCheckBox(String id, BootstrapCheckBoxPickerConfig config)
	{
		super(id, config);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 * @param model
	 * @param config
	 */
	public YesNoCheckBox(String id, IModel<Boolean> model, BootstrapCheckBoxPickerConfig config)
	{
		super(id, model, config);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		BootstrapCheckBoxPickerConfig config = getConfig();
		config.withReverse(true);
		config.withOnCls(Type.Outline_Secondary);
		config.withOffCls(Type.Outline_Secondary);
	}


	@Override
	protected IModel<String> getOnLabel()
	{
		return new ResourceModel("common.yes");
	}

	@Override
	protected IModel<String> getOffLabel()
	{
		return new ResourceModel("common.no");
	}
}