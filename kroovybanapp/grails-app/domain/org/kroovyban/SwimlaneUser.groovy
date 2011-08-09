package org.kroovyban

class SwimlaneUser {

    Swimlane swimlane
    User user

    static constraints = {
        swimlane  nullable:false, unique:'user'
        user nullable:false, unique: 'swimlane'
    }
}
