package hello

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono

@SpringBootApplication
class KotlinApplication {

    var lastMove: String? = null
    var lastScore: Int = 0

    @Bean
    fun routes() = router {
        GET {
            ServerResponse.ok().body(Mono.just("Let the battle begin!"))
        }

        POST("/**", accept(APPLICATION_JSON)) { request ->
            request.bodyToMono(ArenaUpdate::class.java).flatMap { arenaUpdate ->
                //println(arenaUpdate)

                val myLink = arenaUpdate._links.self.href
                val myState = arenaUpdate.arena.state.get(myLink)
                val losePoint = myState?.wasHit?:false
                val scorePoint = if(losePoint){
                    myState?.score?:0 + 1 > lastScore
                } else {
                    myState?.score?:0 > lastScore
                }


                val nextMove = if(scorePoint){
                    "T"
                } else if (losePoint){
                    lastMove?.let { 
                    if(it == "F"){
                        "L"
                    } else {
                        "F"
                    } 
                    }?:"F"
                } else {
                    lastMove?.let { 
                    if(it == "T"){
                        "L"
                    } else {
                        "T"
                    } 
                    }?:"T"
                }

                
                lastMove = nextMove            
                lastScore = myState?.score?:0

                //ServerResponse.ok().body(Mono.just(listOf("F", "R", "L", "T").random()))
                ServerResponse.ok().body(Mono.just(nextMove))
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<KotlinApplication>(*args)
}

data class ArenaUpdate(val _links: Links, val arena: Arena)
data class PlayerState(val x: Int, val y: Int, val direction: String, val score: Int, val wasHit: Boolean)
data class Links(val self: Self)
data class Self(val href: String)
data class Arena(val dims: List<Int>, val state: Map<String, PlayerState>)
