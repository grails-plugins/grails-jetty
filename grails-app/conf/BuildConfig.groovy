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

	dependencies {

		String jettyVersion = '8.1.13.v20130916'

        runtime ("org.eclipse.jetty.aggregate:jetty-all:$jettyVersion") {
            exclude "javax.servlet"
        }

		// needed for JSP compilation
		runtime 'org.eclipse.jdt.core.compiler:ecj:3.7.2'
	}

	plugins {
		build(':release:2.2.0', ':rest-client-builder:1.0.3') {
			export = false
		}
	}
}
