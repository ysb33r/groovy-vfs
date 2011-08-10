package org.kroovyban

class ClassOfServiceDelivery {

    String name
    String description

    static constraints = {
        name blank: false, unique: true
    }
}
