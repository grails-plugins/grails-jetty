/* Copyright 2012 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.jetty

import grails.util.BuildSettings
import grails.util.BuildSettingsHolder
import grails.web.container.EmbeddableServer

import org.eclipse.jetty.plus.webapp.EnvConfiguration
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ssl.SslSocketConnector
import org.eclipse.jetty.webapp.Configuration
import org.eclipse.jetty.webapp.TagLibConfiguration
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.webapp.WebInfConfiguration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.util.Assert
import org.springframework.util.FileCopyUtils

/**
 * An implementation of the EmbeddableServer interface for Jetty.
 *
 * @see EmbeddableServer
 *
 * @author Graeme Rocher
 * @since 1.2
 */
class JettyServer implements EmbeddableServer {

	BuildSettings buildSettings
	WebAppContext context
	Server grailsServer

	protected String keystore
	protected File keystoreFile
	protected String keyPassword

	// These are set from the outside in _GrailsRun
	ConfigObject grailsConfig
	def eventListener

	/**
	 * Creates a new JettyServer for the given war and context path
	 */
	JettyServer(String warPath, String contextPath) {
		initialize()
		context = new WebAppContext(war: warPath, contextPath: contextPath)
	}

	/**
	 * Constructs a Jetty server instance for the given arguments. Used for inline, non-war deployment.
	 *
	 * @basedir The web application root
	 * @webXml The web.xml definition
	 * @contextPath The context path to deploy to
	 * @classLoader The class loader to use
	 */
	JettyServer(String basedir, String webXml, String contextPath, ClassLoader classLoader) {
		initialize()
		context = createStandardContext(basedir, webXml, contextPath, classLoader)
	}

	/**
	 * Initializes the JettyServer class
	 */
	protected void initialize() {
		buildSettings = BuildSettingsHolder.getSettings()

		keystore = "$buildSettings.grailsWorkDir/ssl/keystore"
		keystoreFile = new File(keystore)
		keyPassword = "123456"

		System.setProperty('org.eclipse.jetty.xml.XmlParser.NotValidating', 'true')
	}

	/**
	 * @see EmbeddableServer#start()
	 */
	void start() { start DEFAULT_PORT }

	/**
	 * @see EmbeddableServer#start(int)
	 */
	void start(int port) {
		assertState()
		start DEFAULT_HOST, port
	}

	/**
	 * @see EmbeddableServer#start(String, int)
	 */
	void start (String host, int port) {
		startServer configureHttpServer(context, port, host)
	}

	/**
	 * @see EmbeddableServer#startSecure()
	 */
	void startSecure() { startSecure DEFAULT_SECURE_PORT }

	/**
	 * @see EmbeddableServer#startSecure(int)
	 */
	void startSecure(int httpsPort) {
		assertState()
		startSecure DEFAULT_HOST, DEFAULT_PORT, httpsPort
	}

	void startSecure(String host, int httpPort, int httpsPort) {
		startServer configureHttpsServer(context, httpPort, httpsPort, host)
	}

	/**
	 * @see EmbeddableServer#stop()
	 */
	void stop() {
		assertState()
		grailsServer.stop()
	}

	/**
	 * @see EmbeddableServer#restart()
	 */
	void restart() {
		assertState()
		stop()
		start()
	}

	/**
	 * Starts the given Grails server
	 */
	protected startServer(Server grailsServer) {
		eventListener?.event("ConfigureJetty", [grailsServer])
		grailsServer.start()
	}

	/**
	 * Creates a standard WebAppContext from the given arguments
	 */
	protected WebAppContext createStandardContext(String webappRoot, String webXml,
	                                              String contextPath, ClassLoader classLoader) {
		// Jetty requires a "defaults descriptor" on the filesystem. So we copy
		// it from Grails to the project work directory (if it's not already there)
		def webDefaults = new File("${buildSettings.projectWorkDir}/webdefault.xml")
		if (!webDefaults.exists()) {
			FileCopyUtils.copy(
					grailsResource("conf/webdefault.xml").inputStream,
					new FileOutputStream(webDefaults.path))
		}

		def webContext = new WebAppContext(webappRoot, contextPath)
		def configurations = [
			WebInfConfiguration,
			Configuration,
			JettyWebXmlConfiguration,
			TagLibConfiguration
		]*.newInstance()
		def jndiConfig = new EnvConfiguration()
		if (grailsConfig.grails.development.jetty.env) {
			def res = new FileSystemResource(grailsConfig.grails.development.jetty.env.toString())
			if (res) {
				jndiConfig.setJettyEnvXml(res.URL)
			}
		}
		configurations.add(1, jndiConfig)
		webContext.configurations = configurations
		webContext.setDefaultsDescriptor(webDefaults.path)
		webContext.setClassLoader(classLoader)
		webContext.setDescriptor(webXml)
		return webContext
	}

	/**
	 * Configures a new Jetty Server instance for the given WebAppContext
	 */
	protected Server configureHttpServer(WebAppContext context, int serverPort = DEFAULT_PORT,
	                                     String serverHost = DEFAULT_HOST) {
		Server server = new Server()
		grailsServer = server
		def connectors = [new SelectChannelConnector()]
		connectors[0].setPort(serverPort)
		if (serverHost) {
			connectors[0].setHost(serverHost)
		}
		server.setConnectors((Connector[]) connectors)
		server.setHandler(context)
		return server
	}

	/**
	 * Configures a secure HTTPS server
	 */
	protected configureHttpsServer(WebAppContext context, int httpPort = DEFAULT_PORT,
	                               int httpsPort = DEFAULT_SECURE_PORT, String serverHost = DEFAULT_HOST ) {
		def server = configureHttpServer(context, httpPort, serverHost)
		if (!(keystoreFile.exists())) {
			createSSLCertificate()
		}
		def secureListener = new SslSocketConnector()
		secureListener.setPort(httpsPort)
		if (serverHost) {
			secureListener.setHost(serverHost)
		}
		secureListener.setMaxIdleTime(50000)
		secureListener.setPassword(keyPassword)
		secureListener.setKeyPassword(keyPassword)
		secureListener.setKeystore(keystore)
		secureListener.setNeedClientAuth(false)
		secureListener.setWantClientAuth(true)
		def connectors = server.getConnectors().toList()
		connectors.add(secureListener)
		server.setConnectors(connectors.toArray(new Connector[0]))
		return server
	}

	/**
	 * Creates the necessary SSL certificate for running in HTTPS mode
	 */
	protected void createSSLCertificate() {
		println 'Creating SSL Certificate...'
		if (!keystoreFile.parentFile.exists() && !keystoreFile.parentFile.mkdir()) {
			throw new RuntimeException("Unable to create keystore folder: $keystoreFile.parentFile.canonicalPath")
		}

		String[] keytoolArgs = [
			"-genkey",
			"-alias",
			"localhost",
			"-dname",
			"CN=localhost,OU=Test,O=Test,C=US",
			"-keyalg",
			"RSA",
			"-validity",
			"365",
			"-storepass",
			"key",
			"-keystore",
			"${keystore}",
			"-storepass",
			"${keyPassword}",
			"-keypass",
			"${keyPassword}"
		]

		getKeyToolClass().main(keytoolArgs)

		println 'Created SSL Certificate.'
	}

	protected Class<?> getKeyToolClass() {
		try {
			Class.forName 'sun.security.tools.KeyTool'
		}
		catch (ClassNotFoundException e) {
			// no try/catch for this one, if neither is found let it fail
			Class.forName 'com.ibm.crypto.tools.KeyTool'
		}
	}

	protected assertState() {
		Assert.state(context != null, "The WebAppContext has not been initialized!")
	}

	protected grailsResource(String path) {
		if (buildSettings.grailsHome) {
			return new FileSystemResource("$buildSettings.grailsHome/$path")
		}

		new ClassPathResource(path)
	}
}
