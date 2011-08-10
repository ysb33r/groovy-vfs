package org.kroovyban

import org.apache.commons.lang.builder.HashCodeBuilder

class SwimlaneServiceClass implements Serializable  {

    Swimlane swimlane
    ClassOfServiceDelivery classOfService

	boolean equals(other) {
		if (!(other instanceof SwimlaneServiceClass)) {
			return false
		}

		other.swimlane?.id == swimlane?.id &&
			other.classOfService?.id == classOfService?.id
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		builder.append(swimlane.id)
		builder.append(classOfService.id)
		builder.toHashCode()
	}

    static constraints = {
        swimlane  nullable:false
        classOfService nullable:false
    }

	static mapping = {
		id composite: ['swimlane', 'classOfService']
		version false
	}	
}


