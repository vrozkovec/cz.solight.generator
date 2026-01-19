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

import java.time.Duration;

import org.apache.wicket.Component;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;

import name.berries.wicket.util.AjaxUtil;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationAlert;
import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

/**
 * @author rozkovec
 */
public class AjaxNotificationPanel extends NotificationPanel
{

	/**
	 * Construct.
	 *
	 * @param id
	 */
	public AjaxNotificationPanel(String id)
	{
		super(id);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 * @param fence
	 */
	public AjaxNotificationPanel(String id, Component fence)
	{
		super(id, fence);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 * @param filter
	 */
	public AjaxNotificationPanel(String id, IFeedbackMessageFilter filter)
	{
		super(id, filter);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 * @param fence
	 * @param filter
	 */
	public AjaxNotificationPanel(String id, Component fence, IFeedbackMessageFilter filter)
	{
		super(id, fence, filter);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		setOutputMarkupPlaceholderTag(true);
		setEscapeModelStrings(false);
		hideAfter(Duration.ofSeconds(12));
	}

	@Override
	protected Component newMessageDisplayComponent(String markupId, FeedbackMessage message)
	{
		NotificationAlert alert = new HtmlNotificationAlert(markupId, message, Duration.ofSeconds(12));
		alert.setCloseButtonVisible(isCloseButtonVisible());
		return alert;
	}

	/**
	 * @param target
	 * @param c
	 */
	public static void notifyAll(IPartialPageRequestHandler target, Component c)
	{
		c.getPage().visitChildren(AjaxNotificationPanel.class, (panel, retVal) -> target.add(panel));
	}

	/**
	 */
	public static void notifyAllIfTargetExists()
	{
		AjaxUtil.ifTargetExists(t -> t.getPage().visitChildren(AjaxNotificationPanel.class, (panel, retVal) -> t.add(panel)));
	}

}
