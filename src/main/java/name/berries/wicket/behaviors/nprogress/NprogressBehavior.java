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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

/**
 * @author rozkovec
 */
public class NprogressBehavior extends Behavior
{
	/**
	 * To be used in
	 * {@link AbstractAjaxTimerBehavior#updateAjaxAttributes(org.apache.wicket.ajax.attributes.AjaxRequestAttributes)}:
	 *
	 * <pre>
	 * &#64;Override
	 * protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	 * {
	 * 	super.updateAjaxAttributes(attributes);
	 * 	attributes.getExtraParameters().put(NprogressBehavior.SKIP_NPROGRESS_ATTRIBUTE_KEY, true);
	 * }
	 * </pre>
	 */
	public static final String SKIP_NPROGRESS_ATTRIBUTE_KEY = "skipNProgress";

	/**
	 * Construct.
	 */
	public NprogressBehavior()
	{
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);
		response.render(JavaScriptHeaderItem.forReference(NprogressInitJavaScriptResourceReference.get()));
	}
}
