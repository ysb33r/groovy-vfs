package org.kroovyban

abstract class State {

	String name
    
	static constraints = {
		name blank:false, unique:true, maxSize:25 , matches: '^[A-Z_0-9]+$', nullable:false
    }
	
    static mapping = {
        tablePerHierarchy false
    }

	def isSystem  = { false; }
	
}

class SystemState extends State {
	
	String effect 

    static constraints = {
        effect nullable:false, inList : [ 'INITIAL','TERMINAL','PROMOTE','BATCH_PROMOTE' ]
    }

    def isSystem = { true; }
}

class WorkflowState extends State {
	
	boolean hasCompleted = false
}
