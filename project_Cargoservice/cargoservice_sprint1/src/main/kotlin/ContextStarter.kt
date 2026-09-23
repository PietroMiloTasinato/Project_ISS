package main.kotlin
import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay

fun main() = runBlocking{

    QakContext.createContexts(
        "localhost", this, "sprint1.pl", "sysRules.pl", "ctx_ioport"
    )

    delay(2000)

    QakContext.createContexts(
        "localhost", this, "sprint1.pl", "sysRules.pl", "ctx_cargoservice"
    )
    
}
