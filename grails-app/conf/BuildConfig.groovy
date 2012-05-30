grails.project.work.dir = 'target'
grails.project.docs.output.dir = 'docs/manual' // for gh-pages branch
grails.project.source.level = 1.6

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
	}

	plugins {
		build(':release:2.0.2', ':rest-client-builder:1.0.2') {
			export = false
		}
	}

	dependencies {
		String jettyVersion = '8.1.3.v20120416'

		compile("org.eclipse.jetty:jetty-continuation:$jettyVersion") {
			excludes 'javax.servlet'
		}

		compile("org.eclipse.jetty:jetty-http:$jettyVersion") {
			excludes 'junit', 'javax.servlet'
		}

		compile("org.eclipse.jetty:jetty-io:$jettyVersion") {
			excludes 'jetty-test-helper'
		}

		compile("org.eclipse.jetty:jetty-jmx:$jettyVersion") {
			excludes 'junit'
		}

		compile("org.eclipse.jetty:jetty-jndi:$jettyVersion") {
			excludes 'javax.mail.glassfish', 'junit'
		}

		compile("org.eclipse.jetty:jetty-jsp:$jettyVersion") {
			excludes 'com.sun.el', 'javax.el', 'javax.servlet.jsp',
			         'javax.servlet.jsp.jstl', 'org.apache.jasper.glassfish',
			         'org.apache.taglibs.standard.glassfish', 'org.eclipse.jdt.core'
		}

		compile("org.eclipse.jetty:jetty-plus:$jettyVersion") {
			excludes 'derby', 'javax.servlet', 'javax.transaction', 'jetty-webapp', 'junit'
		}

		compile("org.eclipse.jetty:jetty-security:$jettyVersion") {
			excludes 'junit', 'javax.servlet'
		}

		compile("org.eclipse.jetty:jetty-server:$jettyVersion") {
			excludes 'junit', 'javax.servlet', 'jetty-test-helper', 'mockito-core'
		}

		compile("org.eclipse.jetty:jetty-servlet:$jettyVersion") {
			excludes 'jetty-test-helper'
		}

		compile("org.eclipse.jetty:jetty-util:$jettyVersion") {
			excludes 'javax.servlet', 'jetty-test-helper', 'junit', 'slf4j-api', 'slf4j-jdk14'
		}

		compile "org.eclipse.jetty:jetty-webapp:$jettyVersion"

		compile("org.eclipse.jetty:jetty-websocket:$jettyVersion") {
			excludes 'javax.servlet', 'jetty-test-helper'
		}

		compile("org.eclipse.jetty:jetty-xml:$jettyVersion") {
			excludes 'junit'
		}

		// needed for JSP compilation
		runtime 'org.eclipse.jdt.core.compiler:ecj:3.6.2'
	}
}
