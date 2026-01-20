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
package name.berries.app.guice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * @author vit
 */
public class GuiceStaticHolder
{
	private static final Logger log = LoggerFactory.getLogger(GuiceStaticHolder.class);

	private static Injector INJECTOR;

	/**
	 * Gets injector.
	 *
	 * @return injector
	 */
	public static Injector getInjector()
	{
		return INJECTOR;
	}

	/**
	 * Gets instance.
	 *
	 * @param type
	 * @param <T>
	 * @return instance of T
	 */
	public static <T> T getInstance(Class<T> type)
	{
		return getInjector().getInstance(type);
	}

	/**
	 * @param injector
	 */
	public static void setInjector(Injector injector)
	{
		if (INJECTOR == null)
			INJECTOR = injector;
		else
			throw new RuntimeException(
				"Injector already set, refusing to set new one. Call unset() first if you really need to do it.");
	}

	/**
	 * Injects injectable members of the object. Does nothing when static injector is not
	 * initialized.
	 *
	 * @param object
	 */
	public static void injectMembers(Object object)
	{
		if (INJECTOR != null)
			INJECTOR.injectMembers(object);
		else
			log.error("Injector not set.");

	}

	/**
	 * Unsets the injector.
	 */
	public static void unset()
	{
		INJECTOR = null;
	}

}
