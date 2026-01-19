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
package cz.solight.generator.xmltopdf.wicket.app;

import org.apache.wicket.RuntimeConfigurationType;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.webapp.WebAppContext;

import name.berries.wicket.util.app.WicketAppUtil;
import name.berries.wicket.util.app.jetty.JettyRunner;

/**
 * Server runner
 */
public class Server extends JettyRunner
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Server start = new Server();

		if (WicketAppUtil.localMode())
		{
			start.setWicketMode(RuntimeConfigurationType.DEVELOPMENT);
		}

		start.start();
	}

	@Override
	public String getSessionSchemaTableName()
	{
		return "org_kusalainstitute_surveys";
	}

	@Override
	protected SessionHandler configureSessionHandler(SessionHandler sessionHandler)
	{
		sessionHandler.setHttpOnly(true);
		sessionHandler.setSameSite(HttpCookie.SameSite.LAX);
		sessionHandler.setSecureRequestOnly(true);
		sessionHandler.setUsingCookies(true);
		return sessionHandler;
	}

	/**
	 * Configures the web application context
	 */
	@Override
	protected void configureWebAppContext(WebAppContext webapp)
	{
		webapp.setMaxFormContentSize(1024 * 1024 * 5);
	}

	/**
	 * Returns the main class for the server
	 */
	@Override
	protected Class<?> getMainClass()
	{
		return Server.class;
	}
}