package io.gearpump.streaming.examples.hbase

import java.io.File

import akka.actor.ActorSystem
import io.gearpump.cluster.UserConfig
import io.gearpump.cluster.client.ClientContext
import io.gearpump.cluster.main.{ArgumentsParser, CLIOption, ParseResult}
import io.gearpump.external.hbase.{HBaseSecurityUtil, HBaseSink}
import io.gearpump.partitioner.HashPartitioner
import io.gearpump.streaming.sink.DataSinkProcessor
import io.gearpump.streaming.{Processor, StreamApplication}
import io.gearpump.util.Graph._
import io.gearpump.util.{AkkaApp, FileUtils, Graph}

object HBaseTest extends AkkaApp with ArgumentsParser {
  override val options: Array[(String, CLIOption[Any])] = Array(
    "split" -> CLIOption[Int]("<how many split tasks>", required = false, defaultValue = Some(2)),
    "sink" -> CLIOption[Int]("<hom many kafka processor tasks", required = false, defaultValue = Some(2)),
    "principal" -> CLIOption[String]("<kerberos principal>", required = true),
    "keytab" -> CLIOption[String]("<kerberos keytab file>", required = true)
  )

  def application(config: ParseResult, system: ActorSystem) : StreamApplication = {
    implicit val actorSystem = system
    val splitNum = config.getInt("split")
    val sinkNum = config.getInt("sink")

    val principal = config.getString("principal")
    val keytab = config.getString("keytab")
    val keytabFile = new File(keytab)
    val keytabContent = FileUtils.readFileToByteArray(keytabFile)

    val appConfig = UserConfig.empty
      .withString(HBaseSecurityUtil.PRINCIPAL_KEY, principal)
      .withBytes(HBaseSecurityUtil.KEYTAB_FILE_KEY, keytabContent)

    val split = Processor[Split](splitNum)
    val sink = new HBaseSink(appConfig, "gear")
    val sinkProcessor = DataSinkProcessor(sink, sinkNum)
    val partitioner = new HashPartitioner
    val computation = split ~ partitioner ~> sinkProcessor
    val app = StreamApplication("HBaseTest", Graph(computation), appConfig)
    app
  }

  override def main(akkaConf: Config, args: Array[String]): Unit = {
    val config = parse(args)
    val context = ClientContext(akkaConf)
    val appId = context.submit(application(config, context.system))
    context.close()
  }
}
