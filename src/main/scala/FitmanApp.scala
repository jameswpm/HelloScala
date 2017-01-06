package com.jameswpm.fitman

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.filters.CommonFilters
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.finatra.validation.Size
import com.twitter.finatra.validation.Range
import org.joda.time.Instant


import scala.collection.mutable

object FitmanApp extends FitmanServer

class FitmanServer extends HttpServer {
  override protected def configureHttp(router: HttpRouter): Unit = {

    router
      .filter[CommonFilters]
      .add[HelloController]
      .add[WeightController]
  }
}

class HelloController extends Controller {
  get("/hello") { request: Request =>
    "Fitman Says hello"
  }
}


class WeightController extends Controller {
  val db: mutable.Map[String, List[Weight]] = mutable.Map[String, List[Weight]]()

  post("/weights") {weight : Weight =>
    val r = time(s"Total time take to post weight for user '${weight.user}' is %d ms") {
      val weightsForUser = db.get(weight.user) match {
        case Some(weights) => weights :+ weight
        case None => List(weight)
      }
      db.put(weight.user, weightsForUser)
      response.created.location(s"/weights/${weight.user}")
    }
    r
  }

  get("/weights/:user") { request: Request =>
    info( s"""Finding weight for user ${request.params("user")}""")
    db.getOrElse(request.params("user"), List())
  }
}

case class Weight(
                   @Size(min = 1, max = 25) user: String,
                   @Range(min = 25, max = 200) weight: Int,
                   status: Option[String],
                   postedAt: Instant = Instant.now()
                 )