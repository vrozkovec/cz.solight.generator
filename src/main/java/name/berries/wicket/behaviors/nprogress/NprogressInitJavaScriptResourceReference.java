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
package name.berries.wicket.behaviors.nprogress;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import de.agilecoders.wicket.jquery.util.Generics2;

/**
 * @author vit
 */
public final class NprogressInitJavaScriptResourceReference extends JavaScriptResourceReference
{
	private static final NprogressInitJavaScriptResourceReference INSTANCE = new NprogressInitJavaScriptResourceReference();

	/**
	 * @return the singleton INSTANCE
	 */
	public static NprogressInitJavaScriptResourceReference get()
	{
		return INSTANCE;
	}

	NprogressInitJavaScriptResourceReference()
	{
		super(NprogressBehavior.class, "nprogress-init.js");
	}

	@Override
	public List<HeaderItem> getDependencies()
	{
		final List<HeaderItem> dependencies = Generics2.newArrayList(super.getDependencies());
		dependencies.add(JavaScriptHeaderItem.forReference(NprogressJavaScriptResourceReference.get()));
		dependencies.add(CssHeaderItem.forReference(NprogressCssResourceReference.get()));
		return dependencies;
	}
}