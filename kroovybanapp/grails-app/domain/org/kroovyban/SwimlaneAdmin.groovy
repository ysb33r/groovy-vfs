package org.kroovyban

class SwimlaneAdmin {

    Swimlane swimlane
    User user

    static constraints = {
        swimlane  nullable:false
        user nullable:false       
    }
}
