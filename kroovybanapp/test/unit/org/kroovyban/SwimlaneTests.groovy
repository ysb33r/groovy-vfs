package org.kroovyban



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainUnitTestMixin} for usage instructions
 */
@TestFor(Swimlane)
class SwimlaneTests {

    void testSwimlane() {
        def tmpl = new Swimlane( name:"My Swimlane" )
        mockForConstraintsTests(Swimlane,[tmpl])

        def a = new Swimlane()
        assert !a.validate()
        assert a.errors["name"] == "nullable"

		a = new Swimlane( name : "MySwimlane" )
		assert a.validate()
		assert a.enabled == true
		assert a.processUrl == null
		assert a.getUserAuthorityName() == "ROLE_SWIMLANE_USER_MySwimlane"
		assert a.getAdminAuthorityName() == "ROLE_SWIMLANE_ADMIN_MySwimlane"

		a = new Swimlane( name : "MySwimlane", processUrl : "abcde+" )
		assert !a.validate()
		assert a.errors["processUrl"] == "url"
		
        fail "complete implementation"
    }
}
