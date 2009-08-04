class JettyGrailsPlugin {
    // the plugin version
    def version = "1.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.2 > *"
	// scope
	def scope = ['dev']
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def author = "Graeme Rocher"
    def authorEmail = "graeme.rocher@springsource.com"
    def title = "Jetty Plugin"
    def description = '''\\
Makes Jetty 6.1.14 the development time container for Grails
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/jetty"

}
