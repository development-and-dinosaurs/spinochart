package uk.co.developmentanddinosaurs.spinochart.sample

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.*
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger

class SampleSimulation : Simulation() {

  companion object {
    private var server: HttpServer? = null
    var serverPort: Int = 0

    init {
      val s = HttpServer.create(InetSocketAddress("localhost", 0), 0)
      val counter = AtomicInteger(0)

      s.createContext(
          "/api/dinosaurs",
          HttpHandler { exchange: HttpExchange ->
            val count = counter.incrementAndGet()
            Thread.sleep(15) // small latency
            val response =
                """[{"id": 1, "name": "Spinosaurus"}, {"id": 2, "name": "Stegosaurus"}]"""
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
          },
      )

      s.createContext(
          "/api/fossil",
          HttpHandler { exchange: HttpExchange ->
            Thread.sleep(30)
            val response = """{"status": "excavated", "depthMeters": 14.5}"""
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
          },
      )

      s.createContext(
          "/api/unstable",
          HttpHandler { exchange: HttpExchange ->
            val count = counter.incrementAndGet()
            if (count % 4 == 0) {
              val err = """{"error": "Meteor impact"}"""
              exchange.sendResponseHeaders(500, err.toByteArray().size.toLong())
              exchange.responseBody.use { it.write(err.toByteArray()) }
            } else {
              val ok = """{"status": "safe"}"""
              exchange.sendResponseHeaders(200, ok.toByteArray().size.toLong())
              exchange.responseBody.use { it.write(ok.toByteArray()) }
            }
          },
      )

      s.start()
      server = s
      serverPort = s.address.port
    }
  }

  private val httpProtocol =
      http.baseUrl("http://localhost:$serverPort").acceptHeader("application/json")

  private val dinosaurScenario =
      scenario("Dinosaur Excavation Workflow")
          .exec(http("Get Dinosaurs").get("/api/dinosaurs").check(status().shouldBe(200)))
          .pause(1)
          .exec(http("Get Fossil Data").get("/api/fossil").check(status().shouldBe(200)))
          .pause(1)
          .exec(
              http("Check Perimeter Stability").get("/api/unstable").check(status().shouldBe(200))
          )

  init {
    setUp(dinosaurScenario.injectOpen(rampUsers(15).during(5))).protocols(httpProtocol)
  }

  override fun after() {
    server?.stop(0)
  }
}
