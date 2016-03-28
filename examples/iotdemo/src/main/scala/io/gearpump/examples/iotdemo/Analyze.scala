package io.gearpump.examples.iotdemo

import io.gearpump.cluster.UserConfig
import io.gearpump.cluster.client.ClientContext
import io.gearpump.cluster.main.ArgumentsParser
import io.gearpump.partitioner.HashPartitioner
import io.gearpump.streaming.{Processor, StreamApplication}
import io.gearpump.util.Graph.Node
import io.gearpump.util.{AkkaApp, Graph}

//io.gearpump.examples.iotdemo.Analyze

object Analyze extends AkkaApp with ArgumentsParser  {

  override def main(akkaConf: Analyze.Config, args: Array[String]): Unit = {
    val source = Processor[ImageSource](1)
    val analyzer = Processor[AnalyzeTask](1)
    val partitioner = new HashPartitioner
    val app = StreamApplication("analyze", Graph(source ~ partitioner ~> analyzer), UserConfig.empty)

    val clientContext = ClientContext(akkaConf)
    clientContext.submit(app)
    clientContext.close()
  }

}
