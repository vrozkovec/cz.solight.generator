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

import java.util.List;

import org.apache.wicket.ajax.WicketAjaxJQueryResourceReference;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import de.agilecoders.wicket.jquery.util.Generics2;

/**
 * @author vit
 */
public final class BootstrapNotifyJavaScriptResourceReference extends JavaScriptResourceReference
{
	private static final BootstrapNotifyJavaScriptResourceReference INSTANCE = new BootstrapNotifyJavaScriptResourceReference();

	/**
	 * @return the singleton INSTANCE
	 */
	public static BootstrapNotifyJavaScriptResourceReference get()
	{
		return INSTANCE;
	}

	BootstrapNotifyJavaScriptResourceReference()
	{
		super(BootstrapNotifyAjaxPanel.class, "bootstrap-notify.min.js");
	}

	@Override
	public List<HeaderItem> getDependencies()
	{
		final List<HeaderItem> dependencies = Generics2.newArrayList(super.getDependencies());
		dependencies.add(JavaScriptHeaderItem.forReference(WicketAjaxJQueryResourceReference.get()));
		dependencies.add(CssHeaderItem.forReference(BootstrapNotifyCssResourceReference.get()));
		return dependencies;
	}
}