package kroovybanapp

import org.kroovyban.UserAuthority

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class UserAuthorityIntegrationTests {

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
        assert UserAuthority.count() == 1 : "Expecting only 1 user to be mapped to be added during bootStrap"

        def a = UserAuthority.get(1,1)
        assert a != null
        assert a.user.username == username : "Expecting user name to be " + username 
        assert a.authority.authority == "ROLE_SYSADMIN" : "Expecting initial user to be a 'ROLE_SYSADMIN'"
    }
}
