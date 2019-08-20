package node

import scala.util.Random

import com.twitter.finagle.Service
import com.twitter.finagle.buoyant.H2
import com.twitter.finagle.buoyant.h2.{Request, Response}
import com.twitter.io.Bufs
import com.twitter.util.{Await, Future}
import io.buoyant.grpc.runtime.Stream
import node.stubs.{DataChunk, StatusResponse, TransmitService}

object Client {

  def main(args: Array[String]): Unit = {
    val address = if (args.isEmpty) "localhost:9999" else args(0)
    Await.result(transmit(address))
  }

  def transmit(address: String): Future[StatusResponse] = {
    val client = new TransmitService.Client(buildService(address, "transmit-service"))
    val stream = makeStream()
    client.transmit(stream).foreach(response => {
      println(s"Got $response")
    })
  }

  def buildService(address: String, label: String): Service[Request, Response] = H2.client.newService(address, label)

  def makeStream(): Stream[DataChunk] = {
    // The number of chunks in the stream doesn't seem to make any difference regarding the leak
    Stream.value(generateChunk())
  }

  def generateChunk(): DataChunk = {
    val data = new Array[Byte](64)
    Random.nextBytes(data)
    new DataChunk(Option(Bufs.ownedBuf(data, 0, data.length)))
  }
}
