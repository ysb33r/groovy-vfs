package kroovybanapp


import static org.junit.Assert.*
import org.junit.*
import org.kroovyban.State
import org.kroovyban.SystemState
import org.kroovyban.WorkflowState

class StateIntegrationTests {

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
    void testBootstrap() {


        assert State.count() == 8

        // Check that we can find all of the state via the base class
        [ 'UNCONFIRMED','COMPLETED','PROMOTED','BATCH_PROMOTED','READY','SPEC','DEVTEST','VERIFY' ]
            .each({ 
                def s = State.findByName(it)
                assert s != null : "Needs a state called '${it}'"
        })


        // Check that the following are system states
        [ 'UNCONFIRMED','COMPLETED','PROMOTED','BATCH_PROMOTED' ]
            .each({ 
                def s = State.findByName(it)
                assert s.isSystem() : "The state called '${it}' is expected to be a system state"
        })

        // Check that the following are not system states
        [ 'READY','SPEC','DEVTEST','VERIFY' ]
            .each({ 
                def s = State.findByName(it)
                assert !s.isSystem() : "The state called '${it}' is expected not to be a system state"
        })


        fail "The following tests need checking when GRAILS-7870 is fixed"

        def ss=SystemState.findByName('UNCONFIRMED')
        assert ss.effect == 'INITIAL' 

        ss=SystemState.findByName('COMPLETED')
        assert ss.effect == 'FINAL' 

        ss=SystemState.findByName('PROMOTED')
        assert ss.effect == 'PROMOTE' 

        ss=SystemState.findByName('BATCH_PROMOTED')
        assert ss.effect == 'BATCH_PROMOTE' 


        def ws=WorkflowState.findByName('READY')
        assert !ws.hasCompleted

        [ 'SPEC','DEVTEST','VERIFY' ]
            .each({
                ws=WorkflowState.findByName(it)
                assert ws.hasCompleted : "State '${it}' is expected to have a hasCompleted flag"
            })



    }
}
