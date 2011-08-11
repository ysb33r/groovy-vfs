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
    void testSwimlaneUsers() {
        def u = User.create ( "joe" )
        def swimlane = new Swimlane( name : "MySwimlane" ).save()
        def role = new Authority( authority: swimlane.getUserAuthorityName() ).save()     
        assert role != null
        
        def ua = UserAuthority.create(u,role)
        assert ua != null    
        
        def su = swimlane.getUsers()
        assert su.size() == 1
        assert su.get(0).username == "joe"

    }

    @Test
    void testSwimlaneAdmins() {
        def u = User.create ( "joe" )
        def swimlane = new Swimlane( name : "MySwimlane" ).save()
        def role = new Authority( authority: swimlane.getAdminAuthorityName() ).save()     
        assert role != null
        
        def ua = UserAuthority.create(u,role)
        assert ua != null    
        
        def su = swimlane.getUsers()
        assert su.size() == 1
        assert su.get(0).username == "joe"
    }
    
    @Test
    void testMore()
    {        
        fail "Complete integration tests for swimlane"
    }
}
