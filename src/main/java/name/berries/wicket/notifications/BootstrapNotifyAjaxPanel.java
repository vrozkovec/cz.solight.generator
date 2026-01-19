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
package name.berries.wicket.notifications;

import org.apache.wicket.Component;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.basic.Label;

import name.berries.wicket.components.RawLabel;

import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Alert.Type;

/**
 * @author vit
 */
public class BootstrapNotifyAjaxPanel extends AjaxNotificationPanel
{
	/**
	 * Template for notify script.
	 */
	public static final String NOTIFY_SCRIPT_TEMPLATE = "$.notify({ icon: '%s', message: '%s' },{ showProgressbar: false, newest_on_top: true, type: '%s', delay: %s, z_index: 10000 });";

	/**
	 * Construct.
	 *
	 * @param id
	 */
	public BootstrapNotifyAjaxPanel(String id)
	{
		super(id);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 * @param fence
	 * @param filter
	 */
	public BootstrapNotifyAjaxPanel(String id, Component fence, IFeedbackMessageFilter filter)
	{
		super(id, fence, filter);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 * @param fence
	 */
	public BootstrapNotifyAjaxPanel(String id, Component fence)
	{
		super(id, fence);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 * @param filter
	 */
	public BootstrapNotifyAjaxPanel(String id, IFeedbackMessageFilter filter)
	{
		super(id, filter);
	}

	@Override
	protected Component newMessageDisplayComponent(String markupId, FeedbackMessage message)
	{
		Type type = Type.from(message.getLevelAsString());
		String icon = "fa fa-exclamation-circle";
		switch (type)
		{
			case Info :
				icon = "fa fa-info-circle";
				break;

			case Success :
				icon = "fa fa-ok";
				break;

			case Danger :
				icon = "fa fa-exclamation-triangle";
				break;
		}

		String script = generateNotifyScript(JavaScriptUtils.escapeQuotes(message.getMessage().toString()).toString(), type, icon);

		final Label label = new RawLabel(markupId, "")
		{
			@Override
			public void renderHead(IHeaderResponse response)
			{
				super.renderHead(response);
				response.render(OnLoadHeaderItem.forScript(script));
			}
		};
		label.setEscapeModelStrings(false);
		return label;
	}

	protected String generateNotifyScript(String message, Type type, String icon)
	{
		return generateDefaultNotifyScript(message, type, icon, getDelayInMs());
	}


	/**
	 * Generates default notify script. Uses {@link #NOTIFY_SCRIPT_TEMPLATE} as a template.
	 *
	 * @param message
	 * @param type
	 * @param icon
	 * @param delayInMs
	 * @return javascript
	 */
	public static String generateDefaultNotifyScript(String message, Type type, String icon, int delayInMs)
	{
		return generateNotifyScript(NOTIFY_SCRIPT_TEMPLATE, message, type, icon, delayInMs);
	}

	/**
	 * Generates notify script.
	 *
	 * @param template
	 *            see {@link #NOTIFY_SCRIPT_TEMPLATE}
	 * @param message
	 * @param type
	 * @param icon
	 * @param delayInMs
	 * @return javascript
	 */
	public static String generateNotifyScript(String template, String message, Type type, String icon, int delayInMs)
	{
		return String.format(template, icon, message, type.name().toLowerCase(), delayInMs);
	}

	/**
	 * Returns the delay of when notification should disappear.
	 *
	 * @return delay in ms
	 */
	protected int getDelayInMs()
	{
		return 5000;
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new BootstrapNotifyJavaScriptResourceReference()));
	}
}