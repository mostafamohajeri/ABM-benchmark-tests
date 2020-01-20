/*
scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.1"
 */

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import language.postfixOps

object TimeHelper {
  var t1 : Long = 0
}

case object EndToken

class RingActor(var id : Int , var next: ActorRef) extends Actor {
  def receive = {
    case msg : Int =>
      if (msg > 0) next ! msg - 1
      else context.parent ! EndToken
    case actorRef : ActorRef =>
      next = actorRef
  }
}

class DistributorActor extends Actor {
  var ended = 0
  val t_total = 250
  val w_total = 500

  def receive = {
    case start : Boolean =>
      if(start) {
        val actorRefs = new Array[ActorRef](500)

        for (w <- actorRefs.indices) {
          val actorRef = context.actorOf(Props(classOf[RingActor],w,if (w > 0) actorRefs(w-1) else null))
          actorRefs(w) = actorRef
        }
        TimeHelper.t1 = System.nanoTime

        actorRefs(0) ! actorRefs.last

        for(i <- 1 to t_total) {
          val w = i * ( w_total / t_total )
          actorRefs(w-1) ! 500000
        }
      }
    case EndToken =>
      ended+=1
      if(ended == t_total)
        println(" ^^^ FINISH ^^^ " + (System.nanoTime - TimeHelper.t1) / 1000000)

  }
}

object Ring extends App {
  val system = ActorSystem("ring")
  val distributorActor = system.actorOf(Props[DistributorActor])
  distributorActor ! true
}
