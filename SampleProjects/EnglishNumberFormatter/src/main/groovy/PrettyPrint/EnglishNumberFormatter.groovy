package PrettyPrint


import groovy.transform.CompileStatic


class EnglishNumberFormatter {

    static final def fixedNames = [
            0G : 'nil',
            1G : 'one',
            2G : 'two',
            3G : 'three',
            4G : 'four',
            5G : 'five',
            6G : 'six',
            7G : 'seven',
            8G : 'eight',
            9G : 'nine',
           10G : 'ten',
           11G : 'eleven',
           12G : 'twelve',
           13G : 'thirteen',
           14G : 'fourteen',
           15G : 'fifteen',
           16G : 'sixteen',
           17G : 'seventeen',
           18G : 'eighteen',
           19G : 'nineteen',
           20G : 'twenty',
           30G : 'thirty',
           40G : 'fourty',
           50G : 'fifty',
           60G : 'sixty',
           70G : 'seventy',
           80G : 'eighty',
           90G : 'ninety'
    ]

    static final def fixedBaseNames = [
        100G : 'hundred',
        1000G : 'thousand',
        1_000_000G : 'million'
    ]

    static String fmt(BigInteger value ) {

        if (value < 0 || value > 999_999_999G ) {
            throw new java.lang.NumberFormatException("Only integers in the range 0..999_999_999 are accepted")
        }
        
        BigInteger base10   = value / 10G
        BigInteger base100  = value / 100G
        BigInteger base1000 = value / 1000G
        BigInteger base1e6  = value / 1_000_000G
        
        BigInteger mod10    = value % 10G
        BigInteger mod100   = value % 100G
        BigInteger mod1000  = value % 1000G
        BigInteger mod1e6   = value % 1_000_000G
        
        if (fixedNames.containsKey(value)) {
            return fixedNames[value]
        }

        if ( value < 100G ) {
            return "${this.fmt(base10 * 10G)} ${this.fmt(mod10)}" 
        } 
        
        if ( value < 1000G ) {
            def ret = "${fmt(base100)} ${fixedBaseNames[100G]}" 
            return mod100 ? "${ret} and ${fmt(mod100)}": ret
        }
                
        if ( value < 1_000_000G ) {
            def ret = "${fmt(base1000)} ${fixedBaseNames[1000G]}"
            return mod1000 ? "${ret}${mod1000<100G ? ' and ' : ' '}${fmt(mod1000)}": ret   
        }
        
        def ret = "${fmt(base1e6)} ${fixedBaseNames[1_000_000G]}"
        return mod1e6 ? "${ret}${mod1e6<100G ? ' and ' : ' '}${fmt(mod1e6)}": ret    
    }
    
    static String fmt(String value) {
        
    }
    
    
}
