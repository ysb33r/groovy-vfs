package org.kroovyban


import org.kroovyban.State
import org.kroovyban.SystemState
import org.kroovyban.WorkflowState
import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainUnitTestMixin} for usage instructions
 */
@TestFor(SystemState)
class StateTests {

    void testSystemState() {
        def tmpl = new SystemState( name:"COMPLETED", effect:"INITIAL")
        mockForConstraintsTests(SystemState,[tmpl])

        def a= new SystemState()
        assert !a.validate()
        assert a.errors["name"] == 'nullable'
        assert a.errors["effect"] == 'nullable'

        a = new SystemState( name:"COMPLETED1", effect:"INITIAL" )
        assert a.validate()
        assert a.isSystem() == true
        assert a.name == "COMPLETED1"
        assert a.effect == "INITIAL"

        a = new SystemState( name:"COMPLETED2", effect:"PROMOTE" )
        assert a.validate()
        assert a.isSystem() == true
        assert a.name == "COMPLETED2"
        assert a.effect == "PROMOTE"

        a = new SystemState(name:"COMP_L_ETED", effect:"PROMOTE")
        assert a.validate()
        assert a.name == "COMP_L_ETED"

        def b = new SystemState(name:"COMPLETED", effect:"PROMOTE")
        assert !b.validate()
        assert b.errors["name"] == "unique"

        a= new SystemState(name:" COMPLETED2", effect:"PROMOTE")
        assert !a.validate()
        assert a.errors["name"] == 'matches'

        a= new SystemState(name:"completed3", effect:"PROMOTE")
        assert !a.validate()
        assert a.errors["name"] == 'matches'

        a= new SystemState(name:"COMPLETED2", effect:"PROMOTEDX")
        assert !a.validate()
        assert a.errors["effect"] == 'inList'
    }

    void testWorkflowState() {
        def tmpl = new WorkflowState( name:"COMPLETED" )
        mockForConstraintsTests(WorkflowState,[tmpl])

        def a= new WorkflowState()
        assert !a.validate()
        assert a.errors["name"] == 'nullable'

        a = new WorkflowState( name:"COMPLETED1" )
        assert a.validate()
        assert a.isSystem() == false
        assert a.hasCompleted == false

        def b = new WorkflowState( name:"COMPLETED2",hasCompleted:true )
        assert a.validate()
        assert b.isSystem() == false
        assert b.hasCompleted == true

        a = new WorkflowState(name:"COMP_L_ETED")
        assert a.validate()
        assert a.name == "COMP_L_ETED"

        b = new WorkflowState(name:"COMPLETED")
        assert !b.validate()
        assert b.errors["name"] == "unique"

        a= new WorkflowState(name:" COMPLETED2")
        assert !a.validate()
        assert a.errors["name"] == 'matches'

        a= new WorkflowState(name:"completed3")
        assert !a.validate()
        assert a.errors["name"] == 'matches'

    }

}

