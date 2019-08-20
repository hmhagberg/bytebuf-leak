package node

import com.twitter.finagle.ListeningServer
import com.twitter.finagle.buoyant.H2
import com.twitter.util.{Await, Future}
import io.buoyant.grpc.runtime
import io.buoyant.grpc.runtime.{GrpcStatus, ServerDispatcher}
import node.stubs.{DataChunk, StatusResponse, TransmitService}

object Server {

  def main(args: Array[String]): Unit = {
    val address = if (args.isEmpty) "localhost:9999" else args(0)
    Await.ready(listen(address))
  }

  def listen(address: String): Future[Unit] = {
    GrpcServer.start(address, new TransmitService.Server(TransmitServiceImpl))
    Future.never // Stay alive until explicitly terminated
  }
}

private object TransmitServiceImpl extends TransmitService {

  override def transmit(req: runtime.Stream[DataChunk]): Future[StatusResponse] = {
    consumeChunk(req)
      .foreach(_ => System.gc()) // Run GC so we get the leak report faster
  }


  def consumeChunk(req: runtime.Stream[DataChunk]): Future[StatusResponse] = {
    req.recv()
      .flatMap { releasableChunk =>
        try {
          val dataChunk = releasableChunk.value
          println(s"Got $dataChunk")
          // Process the data here
        } finally {
          releasableChunk.release()
        }
        consumeChunk(req)
      }
      .rescue {
        case _: GrpcStatus.Ok =>
          Future.value(new StatusResponse(Option(StatusResponse.Status.DONE)))
        case t: Throwable =>
          t.printStackTrace()
          Future.value(new StatusResponse(Option(StatusResponse.Status.ERROR)))
      }
  }
}

private object GrpcServer {

  def start(address: String, service: ServerDispatcher.Service): ListeningServer = {
    H2.server.serve(address, new ServerDispatcher(Seq(service)))
  }

}
