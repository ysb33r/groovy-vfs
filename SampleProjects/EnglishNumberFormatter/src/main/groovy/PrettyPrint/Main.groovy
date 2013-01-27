package PrettyPrint

/** A simplistic Runner class to provide the ability to run
 * the code from the command-line
 * @author schalkc
 *
 */
class Main {

    static main(args) {
        if(args.size() >= 1) {
            println EnglishNumberFormatter.fmt(args[0]) 
        } 
    }
}
