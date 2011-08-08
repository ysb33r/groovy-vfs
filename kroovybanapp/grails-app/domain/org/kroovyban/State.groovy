package org.kroovyban

class State {

	String name
    
	static constraints = {
		name blank:false, unique:true, maxSize:25 , matches: '^[A-Z_0-9]+$', nullable:false
    }
	
	def isSystem  = { false; }
	
	// Add one db table
}

class SystemState extends State {
	
	boolean isTerminal = false
	boolean isInitial = false
	boolean canPromote = false
	boolean canBatchPromote = false
	
    def isSystem = { true; }
}

class WorkflowState extends State {
	
	boolean hasCompleted = false
}
