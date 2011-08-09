package org.kroovyban



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainUnitTestMixin} for usage instructions
 */
@TestFor(SwimlaneUser)
class SwimlaneUserTests {

    void testUniqueness() {
        def tmpl1 = new User( username:"T_joe", password:"pw" )
        mockForConstraintsTests(User,[tmpl1])


        def u1 = new User( username:"joe", password:"pw" ) 
        assert u1.validate()

        def u11 = new User( username:"joe", password:"pw" ) 
        assert u11.validate()

        def tmpl2 = new Swimlane( name:"T_MySwimlane1" )
        mockForConstraintsTests(Swimlane,[tmpl2])

        def tmpl3 = new SwimlaneUser( user:tmpl1, swimlane:tmpl2 )
        mockForConstraintsTests(SwimlaneUser,[tmpl3])

        def u2 = new User( username:"bloggs", password:"pw" ) 
        assert u2.validate()
        
        def s1 = new Swimlane( name:"MySwimlane1" )
        assert s1.validate()

        def s2 = new Swimlane( name:"MySwimlane1" )
        assert s2.validate()

        def su1 = new SwimlaneUser( user:u1, swimlane:s1 )
        assert su1.validate()

        def su2 = new SwimlaneUser( user:u1, swimlane:s1 )
        assert !su2.validate() : "Cannot have the same swimlane-user combination more than once"
          
        def su3 = new SwimlaneUser( user:u1, swimlane:s2 )
        assert su3.validate()

        def su4 = new SwimlaneUser( user:u2, swimlane:s1 )
        assert su4.validate()
    }
}
