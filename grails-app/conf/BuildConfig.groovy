import org.apache.ivy.core.module.descriptor.DefaultExcludeRule
import org.apache.ivy.core.module.id.ArtifactId
import org.apache.ivy.core.module.id.ModuleId
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.apache.ivy.plugins.matcher.ExactPatternMatcher

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
		String jettyVersion = '7.6.0.v20120127'
		runtime "org.eclipse.jetty.aggregate:jetty-all:$jettyVersion",{
			transitive = false
		}

		// needed for JSP compilation
		runtime 'org.eclipse.jdt.core.compiler:ecj:3.6.2',{
			transitive = false
		}
	}
}
