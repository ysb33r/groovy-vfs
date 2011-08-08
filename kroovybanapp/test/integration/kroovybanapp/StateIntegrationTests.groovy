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
/*       
        def systemState = State.findByName('UNCONFIRMED') ?:
            new SystemState( name : 'UNCONFIRMED', isInitial:true ).save(flush:true,failOnError:true)
            
        systemState = State.findByName('COMPLETED') ?:
            new SystemState( name:'COMPLETED', isTerminal:true ).save(flush:true,failOnError:true)
        
        systemState = State.findByName('PROMOTED') ?:
            new SystemState( name:'PROMOTED', isTerminal:true, canPromote = true ).save(flush:true,failOnError:true)
            
        systemState = State.findByName('BATCH_PROMOTED') ?:
            new SystemState( name:'BATCH_PROMOTED', isTerminal:true, canBatchPromote = true ).save(flush:true,failOnError:true)

        def newState = State.findByName('READY') ?:
            new WorkflowState( name:'READY', hasCompleted:false ).save(flush:true,failOnError:true)

        newState = State.findByName('SPEC') ?:
            new WorkflowState( name:'SPEC', hasCompleted:true ).save(flush:true,failOnError:true)

        newState = State.findByName('DEVTEST') ?:
            new WorkflowState( name:'DEVTEST', hasCompleted:true ).save(flush:true,failOnError:true)

        newState = State.findByName('VERIFY') ?:
            new WorkflowState( name:'VERIFY', hasCompleted:true ).save(flush:true,failOnError:true)
*/          

    }
}
