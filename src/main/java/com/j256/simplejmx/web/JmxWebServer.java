package com.j256.simplejmx.web;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;

/**
 * Simple web-server which exposes JMX beans via HTTP. To use this class you need to provide a Jetty version in your
 * dependency list or classpath.
 * 
 * <p>
 * <b>NOTE:</b> This class tries to support both Jetty version 8 and 9. If the
 * {@code org.eclipse.jetty.server.nio.SelectChannelConnector} class is available it will assume you are using version 8
 * otherwise version 9.
 * </p>
 * 
 * @author graywatson
 */
public class JmxWebServer implements Closeable {

	private InetAddress serverAddress;
	private int serverPort;
	private Server server;
	private final JettyConnectorFactory jettyConnectorFactory = getConnectorFactory();

	public JmxWebServer() {
		// for spring
	}

	public JmxWebServer(InetAddress inetAddress, int serverPort) {
		this.serverAddress = inetAddress;
		this.serverPort = serverPort;
	}

	public JmxWebServer(int serverPort) {
		this.serverPort = serverPort;
	}

	/**
	 * Start the internal Jetty web server and configure the {@link JmxWebHandler} to handle the requests.
	 */
	public void start() throws Exception {
		server = new Server();
		Connector connector = jettyConnectorFactory.buildConnector(server, serverAddress, serverPort);
		server.addConnector(connector);
		server.setHandler(new JmxWebHandler());
		server.start();
	}

	/**
	 * Stop the internal Jetty web server and associated classes.
	 */
	public void stop() throws Exception {
		if (server != null) {
			server.setGracefulShutdown(100);
			server.stop();
			server = null;
		}
	}

	@Override
	public void close() throws IOException {
		try {
			stop();
		} catch (IOException ioe) {
			throw ioe;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * Optional address that the Jetty web server will be running on.
	 */
	public void setServerAddress(InetAddress serverAddress) {
		this.serverAddress = serverAddress;
	}

	/**
	 * Required port that the Jetty web server will be running on.
	 */
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	/**
	 * Try to figure out and return a connector factory compatible with either Jetty version 8 or 9.
	 */
	private JettyConnectorFactory getConnectorFactory() {
		try {
			Class.forName("org.eclipse.jetty.server.nio.SelectChannelConnector");
			return new Jetty8ConnectorFactory();
		} catch (Exception e) {
			return new Jetty9ConnectorFactory();
		}
	}
}
