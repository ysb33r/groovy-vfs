package kroovybanapp

import org.kroovyban.UserAuthority
import org.kroovyban.User
import org.kroovyban. Authority

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} 
 * for usage instructions
 */
class UserAuthorityIntegrationTests {

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
    void testBootStrap() {

        def username = grailsApplication.config.bootstrap.init.sysadmin
        assert UserAuthority.count() == 1 : "Expecting only 1 user mapping mapped to be added during bootStrap"

        def a = UserAuthority.get(1,1)
        assert a != null
        assert a.user.username == username : "Expecting user name to be " + username 
        assert a.authority.authority == "ROLE_SYSADMIN" : "Expecting initial user to be a 'ROLE_SYSADMIN'"
    }
	
	@Test
	void testCreate() {
		def u = User.create ( "joe" )            
                assert u != null
        
		def role = new Authority( authority:"ROLE_TEST" ).save()
                assert role != null
                
		def ua = UserAuthority.create(u,role)
		assert ua != null
		
		
		assert UserAuthority.findByAuthority(role) != null
		
                def cnt = UserAuthority.count()
                UserAuthority.remove(u,role)
                assert UserAuthority.count() == cnt-1
		assert UserAuthority.findByAuthority(role) == null
                
	}
	
	@Test
	void testRemoveAllByAuthority() {
		def u = User.create ( "joe" )            
		def role = new Authority( authority:"ROLE_TEST" ).save()
                def cnt = UserAuthority.count()

                def ua = UserAuthority.create(u,role)
		assert ua != null
                assert UserAuthority.count() == cnt+1
                UserAuthority.removeAll(role)
                assert UserAuthority.count() == cnt
		assert UserAuthority.findByAuthority(role) == null
        }

	@Test
	void testRemoveAllByUser() {
		def u = User.create ( "joe" )            
		def role = new Authority( authority:"ROLE_TEST" ).save()
                def cnt = UserAuthority.count()

                def ua = UserAuthority.create(u,role)
		assert ua != null
                assert UserAuthority.count() == cnt+1
                UserAuthority.removeAll(u)
                assert UserAuthority.count() == cnt
		assert UserAuthority.findByAuthority(role) == null
        }
}
