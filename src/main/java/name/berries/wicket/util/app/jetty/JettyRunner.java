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
package name.berries.wicket.util.app.jetty;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.security.ProtectionDomain;

import javax.management.MBeanServer;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.DatabaseAdaptor;
import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.FileSessionDataStore;
import org.eclipse.jetty.server.session.JDBCSessionDataStore;
import org.eclipse.jetty.server.session.JDBCSessionDataStore.SessionTableSchema;
import org.eclipse.jetty.server.session.JDBCSessionDataStoreFactory;
import org.eclipse.jetty.server.session.SessionDataStore;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import name.berries.wicket.util.app.JettyUtil;
import name.berries.wicket.util.app.WicketAppUtil;

import jakarta.servlet.SessionCookieConfig;

/**
 * Helper class for starting embedded jetty server. For datbase structure see jetty-sessions-db.sql
 * file in the package.
 *
 * @author Vit Rozkovec
 */
public abstract class JettyRunner
{
	private int port;
	private String tempDirectoryPath;

	private RuntimeConfigurationType wicketMode = RuntimeConfigurationType.DEVELOPMENT;

	private boolean jettyJDBCSessionDataStoreActive = true;
	private String jettySessionsDb = JettyDbConfig.JETTY_SESSIONS_DB;
	private String jettySessionsUsername = JettyDbConfig.JETTY_SESSIONS_USERNAME;
	private String jettySessionsPassword = JettyDbConfig.JETTY_SESSIONS_PASSWORD;

	/**
	 * Construct.
	 *
	 * @param port
	 *            port that will Jetty listen to connections
	 * @param tempDirectoryPath
	 *            directory that will jetty use for temp files.<br />
	 *            <strong>Beware!</strong> All files in this directory will be deleted when
	 *            application starts.
	 */
	protected JettyRunner()
	{
		super();
		port = WicketAppUtil.getAppPort(8080);
		tempDirectoryPath = JettyUtil.getConfiguredAbsolutePathForJettyTempDir();
	}

	/**
	 * @return main class of the project that starts the server
	 */
	protected abstract Class<?> getMainClass();

	/**
	 * Sets wicketMode.
	 *
	 * @param wicketMode
	 *            wicketMode
	 */
	public void setWicketMode(RuntimeConfigurationType wicketMode)
	{
		this.wicketMode = wicketMode;
	}

	/**
	 * Start the server
	 */
	public final void start()
	{
		if (RuntimeConfigurationType.DEVELOPMENT.equals(wicketMode))
			System.setProperty("wicket.configuration", "development");

		Server server = newServer();
		Handler handler = newHandler(jettyJDBCSessionDataStoreActive);
		handler.setServer(server);
		server.setHandler(handler);

		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
		server.addEventListener(mBeanContainer);
		server.addBean(mBeanContainer);

		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					server.stop();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.exit(100);
				}
			}
		});

		try
		{
			server.start();

			if (Boolean.parseBoolean(System.getenv("RUNNING_IN_ECLIPSE")))
			{
				System.out.println("You're using Eclipse; click in this console and	"
					+ "press ENTER to call System.exit() and run the shutdown routine.");
				try
				{
					System.in.read();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				System.exit(0);
			}

			server.join();
		}
		catch (InterruptedException e)
		{

			e.printStackTrace();
			Thread.currentThread().interrupt();
			System.exit(100);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(100);
		}

	}

	/**
	 * Factory method for getting the serer
	 *
	 * @return initialized server instance
	 */
	protected Server newServer()
	{
		Args.notNull(port, "port");
		Server server = new Server();

		HttpConfiguration httpConfig = new HttpConfiguration();
		// Handles X-Forwarded-* headers
		httpConfig.addCustomizer(new ForwardedRequestCustomizer());
		ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
		connector.setPort(port);
		server.addConnector(connector);

		return server;
	}

	protected Handler newHandler(boolean jettyJDBCSessionDataStoreActive)
	{
		Args.notNull(tempDirectoryPath, "tempDirectoryPath");
		Args.notNull(getMainClass(), "mainClass");

		Class<?> mainClass = getMainClass();
		ProtectionDomain domain = mainClass.getProtectionDomain();
		URL location = domain.getCodeSource().getLocation();

		WebAppContext webapp = new WebAppContext()
		// {
		// @Override
		// protected SessionHandler newSessionHandler()
		// {
		// return new LoggingSessionHandler();
		// }
		// }
		;
		webapp.setContextPath("/");
		webapp.setWar(location.toExternalForm());

		configureSessionHandler(webapp.getSessionHandler());

		configureWebAppContext(webapp);


		// (Optional) Set the directory the war will extract to.
		// If not set, java.io.tmpdir will be used, which can cause problems
		// if the temp directory gets cleaned periodically.
		// Your build scripts should remove this directory between deployments
		webapp.setTempDirectory(new File(tempDirectoryPath));

		addResourceFolderIfExists(webapp, "static", "/static/*", false);
		addResourceFolderIfExists(webapp, "webapp", "/*", true);

		if (jettyJDBCSessionDataStoreActive)
		{
			jdbcSessionStore(webapp.getSessionHandler());
		}

		return webapp;
	}

	protected SessionHandler configureSessionHandler(SessionHandler sessionHandler)
	{
		sessionHandler.setHttpOnly(true);
		sessionHandler.setSameSite(HttpCookie.SameSite.STRICT);
		sessionHandler.setSecureRequestOnly(true);
		sessionHandler.setUsingCookies(true);

		SessionCookieConfig cookieConfig = sessionHandler.getSessionCookieConfig();
		cookieConfig.setSecure(true);
		cookieConfig.setHttpOnly(true);

		sessionHandler.setSessionCache(sessionCache(sessionHandler));

		return sessionHandler;
	}

	protected void configureWebAppContext(WebAppContext webapp)
	{
	}

	protected DefaultSessionCache sessionCache(SessionHandler sessionHandler)
	{
		DefaultSessionCache sessionCache = new DefaultSessionCache(sessionHandler);

		if (jettyJDBCSessionDataStoreActive)
			sessionCache.setSessionDataStore(jdbcSessionStore(sessionHandler));
		else
			sessionCache.setSessionDataStore(fileSessionDataStore());

		sessionCache.setRemoveUnloadableSessions(true);
		sessionCache.setEvictionPolicy(60 * 60 * 2);// 2 hours
		sessionCache.setSaveOnInactiveEviction(true);
		return sessionCache;
	}

	protected SessionDataStore jdbcSessionStore(SessionHandler sessionHandler)
	{
		String connectionUrl = "";
		connectionUrl += "jdbc:mariadb://localhost/";
		connectionUrl += getJettySessionsDb();
		connectionUrl += "?user=" + getJettySessionsUsername();
		connectionUrl += "&password=" + getJettySessionsPassword();
		connectionUrl += "&timezone=Europe/Prague";

		// Configure the DatabaseAdaptor
		DatabaseAdaptor adaptor = new DatabaseAdaptor();
		adaptor.setDriverInfo("org.mariadb.jdbc.Driver", connectionUrl);

		// Create a JDBCSessionDataStoreFactory
		JDBCSessionDataStoreFactory jdbcDataStoreFactory = new JDBCSessionDataStoreFactory();
		jdbcDataStoreFactory.setDatabaseAdaptor(adaptor);

		// Configure the SessionTableSchema
		if (!Strings.isEmpty(getSessionSchemaTableName()))
		{
			SessionTableSchema schema = new JDBCSessionDataStore.SessionTableSchema();
			schema.setTableName(getSessionSchemaTableName());
			jdbcDataStoreFactory.setSessionTableSchema(schema);
		}

		return jdbcDataStoreFactory.getSessionDataStore(sessionHandler);
	}

	private SessionDataStore fileSessionDataStore()
	{
		String suffix = getMainClass().getCanonicalName().replace(".", "_");
		FileSessionDataStore fileSessionDataStore = new FileSessionDataStore();
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		File storeDir = new File(baseDir, "wicket-session-store-" + suffix);
		storeDir.mkdir();
		fileSessionDataStore.setStoreDir(storeDir);
		return fileSessionDataStore;
	}

	private void addResourceFolderIfExists(WebAppContext webapp, String path, String mapping, boolean required)
	{
		Class<? extends JettyRunner> clazz = this.getClass();
		ClassLoader classLoader = clazz.getClassLoader();
		URL resource = classLoader.getResource(path);
		if (resource == null)
		{
			if (required)
				throw new RuntimeException("Cannot start the app without required resource: " + path);
			else
				return;
		}
		String staticPath = resource.toExternalForm();
		ServletHolder resourceServlet = new ServletHolder(DefaultServlet.class);
		resourceServlet.setInitParameter("dirAllowed", "false");
		resourceServlet.setInitParameter("resourceBase", staticPath);
		resourceServlet.setInitParameter("pathInfoOnly", "true");

		if (WicketAppUtil.localMode())
		{
			resourceServlet.setInitParameter("cacheControl", "no-cache");
			resourceServlet.setInitParameter("useFileMappedBuffer", "false");
			resourceServlet.setInitParameter("etags", "true");
		}

		webapp.addServlet(resourceServlet, mapping);
	}

	/**
	 * Gets port.
	 *
	 * @return port
	 */

	public int getPort()
	{
		return port;
	}

	/**
	 * Sets port.
	 *
	 * @param port
	 *            port
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	/**
	 * Gets tempDirectoryPath.
	 *
	 * @return tempDirectoryPath
	 */
	public String getTempDirectoryPath()
	{
		return tempDirectoryPath;
	}

	/**
	 * Sets tempDirectoryPath.
	 *
	 * @param tempDirectoryPath
	 *            tempDirectoryPath
	 */
	public void setTempDirectoryPath(String tempDirectoryPath)
	{
		this.tempDirectoryPath = tempDirectoryPath;
	}

	/**
	 * Gets jettyJDBCSessionDataStoreActive.
	 *
	 * @return jettyJDBCSessionDataStoreActive
	 */
	public boolean isJettyJDBCSessionDataStoreActive()
	{
		return jettyJDBCSessionDataStoreActive;
	}

	/**
	 * Sets jettyJDBCSessionDataStoreActive.
	 *
	 * @param jettyJDBCSessionDataStoreActive
	 *            jettyJDBCSessionDataStoreActive
	 */
	public void setJettyJDBCSessionDataStoreActive(boolean jettyJDBCSessionDataStoreActive)
	{
		this.jettyJDBCSessionDataStoreActive = jettyJDBCSessionDataStoreActive;
	}

	/**
	 * Gets jettySessionsDb.
	 *
	 * @return jettySessionsDb
	 */
	public String getJettySessionsDb()
	{
		return jettySessionsDb;
	}

	/**
	 * Sets jettySessionsDb.
	 *
	 * @param jettySessionsDb
	 *            jettySessionsDb
	 */
	public void setJettySessionsDb(String jettySessionsDb)
	{
		this.jettySessionsDb = jettySessionsDb;
	}

	/**
	 * Gets jettySessionsUsername.
	 *
	 * @return jettySessionsUsername
	 */
	public String getJettySessionsUsername()
	{
		return jettySessionsUsername;
	}

	/**
	 * Sets jettySessionsUsername.
	 *
	 * @param jettySessionsUsername
	 *            jettySessionsUsername
	 */
	public void setJettySessionsUsername(String jettySessionsUsername)
	{
		this.jettySessionsUsername = jettySessionsUsername;
	}

	/**
	 * Gets jettySessionsPassword.
	 *
	 * @return jettySessionsPassword
	 */
	public String getJettySessionsPassword()
	{
		return jettySessionsPassword;
	}

	/**
	 * Sets jettySessionsPassword.
	 *
	 * @param jettySessionsPassword
	 *            jettySessionsPassword
	 */
	public void setJettySessionsPassword(String jettySessionsPassword)
	{
		this.jettySessionsPassword = jettySessionsPassword;
	}

	/**
	 * Gets wicketMode.
	 *
	 * @return wicketMode
	 */
	public RuntimeConfigurationType getWicketMode()
	{
		return wicketMode;
	}

	/**
	 * Allow overriding session schema table name.
	 *
	 * @return table name
	 */
	public String getSessionSchemaTableName()
	{
		return null;
	}
}
