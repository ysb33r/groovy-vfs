package org.kroovyban


import org.kroovyban.State
import org.kroovyban.SystemState
import org.kroovyban.WorkflowState
import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainUnitTestMixin} for usage instructions
 */
@TestFor(State)
class StateTests {

    void testState() {
        def tmpl = new State( name:"COMPLETED" )
        mockForConstraintsTests(State,[tmpl])

        def a= new State()
        assert !a.validate()
        assert a.errors["name"] == 'nullable'
        assert a.isSystem() == false

        a = new State(name:"COMPLETED1")
        assert a.validate()

        a = new State(name:"COMP_L_ETED")
        assert a.validate()

        def b = new State(name:"COMPLETED")
        assert !b.validate()
        assert b.errors["name"] == "unique"

        a= new State(name:" COMPLETED2")
        assert !a.validate()
        assert a.errors["name"] == 'matches'

        a= new State(name:"completed3")
        assert !a.validate()
        assert a.errors["name"] == 'matches'

    }

    void testSystemState() {
        def tmpl = new SystemState( name:"COMPLETED" )
        mockForConstraintsTests(SystemState,[tmpl])

        def a= new SystemState()
        assert !a.validate()
        assert a.errors["name"] == 'nullable'

        a = new SystemState( name:"COMPLETED1" )
        assert a.validate()
        assert a.isSystem() == true
        assert !a.isTerminal
        assert !a.isInitial
        assert !a.canPromote
        assert !a.canBatchPromote
        assert a.name == "COMPLETED1"

        a = new SystemState( name:"COMPLETED2", isTerminal:true, isInitial:true, canPromote:true, canBatchPromote:true )
        assert a.validate()
        assert a.isSystem() == true
        assert a.isTerminal
        assert a.isInitial
        assert a.canPromote
        assert a.canBatchPromote
        assert a.name == "COMPLETED2"
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
    }
}

