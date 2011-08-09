package org.kroovyban

class SwimlaneServiceClass {

    Swimlane swimlane
    ClassOfServiceDelivery classOfService

    static constraints = {
        swimlane  nullable:false
        classOfService nullable:false
    }
}
