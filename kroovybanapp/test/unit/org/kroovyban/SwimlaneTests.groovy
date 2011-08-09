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
//        assert a.errors["admins"] == "minSize"

        fail "complete implementation"
    }
}
