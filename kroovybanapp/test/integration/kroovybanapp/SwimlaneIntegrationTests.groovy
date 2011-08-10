package kroovybanapp

import org.kroovyban.User
import org.kroovyban.Authority
import org.kroovyban.UserAuthority
import org.kroovyban.Swimlane

import org.junit.*
import grails.plugins.springsecurity.SpringSecurityService 
import org.codehaus.groovy.grails.commons.GrailsApplication


/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
class SwimlaneIntegrationTests {

    def grailsApplication
	
    @Before
    void setUp() {
        // Setup logic here
    }

    @After
    void tearDown() {
        // Tear down logic here
    }

    @Test
    void testBasicSwimlane() {
		def u = new User ( name:"joe" ).save()
		def swimlane = new Swimlane( name : "MySwimlane" )		.save()
		def role = new Authority( name: swimlane.getUserAuthorityName() ).save()
		UserAuthority.create(u,role)
		
		def su = swimlane.getUsers()
		assert su.size() == 1
		
		fail "Complete integration tests for swimlane"
    }
}
