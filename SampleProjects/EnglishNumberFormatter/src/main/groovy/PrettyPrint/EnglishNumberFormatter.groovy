// ============================================================================
// (C) Copyright Schalk W. Cronje 2013
//
// This software is licensed under the Apche License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
// ============================================================================
package PrettyPrint

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
            return this.delegateFmt (100G , base100, mod100 )
        }
                
        if ( value < 1_000_000G ) {
            return delegateFmt (1000G , base1000, mod1000 )
        }
        
        return delegateFmt (1_000_000G , base1e6, mod1e6 )
   
    }
    
    static String fmt(String value) {
        if(!value.isBigInteger()) {
            throw new java.lang.NumberFormatException("Argument '${value}' cannot be parsed as an English number expression")
        }    
        
        return fmt(value.toBigInteger())
    }    
    
    private static String delegateFmt( BigInteger level, BigInteger base, BigInteger modulus) {
        def ret = "${fmt(base)} ${fixedBaseNames[level]}"
        return modulus ? "${ret} and ${fmt(modulus)}": ret
    }    
}
