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
package name.berries.wicket.util;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Args;

/**
 * @author rozkovec
 */
public class AjaxUtil
{
	/**
	 * Checks if target is present in current request cycle.
	 *
	 * @return <code>true</code> if present
	 */
	public static boolean isTargetPresent()
	{
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle == null)
			return false;

		Optional<AjaxRequestTarget> maybeTarget = requestCycle.find(AjaxRequestTarget.class);
		return maybeTarget.isPresent();
	}

	/**
	 * Finds target in current request cycle.
	 *
	 * @return ajax request target
	 */
	public static AjaxRequestTarget findTarget()
	{

		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle == null)
			return null;

		Optional<AjaxRequestTarget> maybeTarget = requestCycle.find(AjaxRequestTarget.class);

		if (maybeTarget.isPresent())
			return maybeTarget.get();

		return null;
	}

	/**
	 * Runs given action only if {@link AjaxRequestTarget} has been found in current request.
	 *
	 * @param action
	 * @return <code>true</code> if target was found, <code>false</code> otherwise.
	 */
	public static boolean ifTargetExists(Consumer<AjaxRequestTarget> action)
	{
		AjaxRequestTarget target = findTarget();
		if (target != null)
		{
			action.accept(target);
			return true;
		}
		return false;
	}

	/**
	 * Adds these components to the target if there is some target present.
	 *
	 * @param components
	 *            can be <code>null</code> - in that case, NOOP if no target present
	 * @return ajax request target
	 */
	public static AjaxRequestTarget maybeAddToTarget(Component... components)
	{
		AjaxRequestTarget target = findTarget();
		if (target != null && components != null && components.length > 0)
		{
			for (Component component : components)
			{
				if (component != null)
					target.add(component);
			}
		}

		return target;
	}

	/**
	 * Triggers event via jQuery on given component
	 *
	 * @param target
	 *
	 * @param component
	 * @param event
	 *
	 * @return script
	 */
	public static CharSequence triggerEvent(AjaxRequestTarget target, Component component, String event)
	{
		String js = String.format("$('#%s').trigger('%s')", component.getMarkupId(), event);
		target.appendJavaScript(js);
		return js;
	}

	/**
	 * Copies from one instance to the other
	 *
	 * @param target
	 * @param from
	 * @param to
	 * @return script in case it would be used some other way
	 */
	public static CharSequence copyValuesFromTo(AjaxRequestTarget target, Component from, Component to)
	{

		Args.notNull(from, "from");
		Args.notNull(to, "to");

		String js = String.format("$('#%s input').each(function(index){$('#%s input').eq(index).val($(this).val());});",
			from.getMarkupId(), to.getMarkupId());
		if (target != null)
			target.appendJavaScript(js);

		return js;
	}

	/**
	 * Copies from one instance to the other
	 *
	 * @param target
	 * @param value
	 * @param component
	 * @return script in case it would be used some other way
	 */
	public static CharSequence setValueTo(AjaxRequestTarget target, Object value, Component component)
	{
		Args.notNull(component, "component");

		String js = String.format("$('#%s').val('%s');", component.getMarkupId(), value);
		if (target != null)
			target.appendJavaScript(js);

		return js;
	}

	/**
	 * Copies from one instance to the other
	 *
	 * @param target
	 * @param mappings
	 *            map of pairs, key used as a source component, value as destination one
	 *
	 */
	public static void copyValuesFromTo(AjaxRequestTarget target, MultiValuedMap<Component, Component> mappings)
	{

		Args.notNull(mappings, "mappings");
		Set<Component> keys = mappings.keySet();
		if (target != null)
			for (Component source : keys)
			{
				Collection<Component> components = mappings.get(source);
				components.forEach((destination) -> {
					String js = String.format("$('input#%s').val($('input#%s').val());", destination.getMarkupId(),
						source.getMarkupId());
					target.appendJavaScript(js);
				});
			}
	}

}
