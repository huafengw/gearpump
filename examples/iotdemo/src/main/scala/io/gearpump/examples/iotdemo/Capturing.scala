package io.gearpump.examples.iotdemo

import io.gearpump.cluster.{Application, UserConfig}
import io.gearpump.cluster.client.ClientContext
import io.gearpump.cluster.main.{CLIOption, ArgumentsParser}
import io.gearpump.util.{LogUtil, AkkaApp}
import org.slf4j.Logger

//io.gearpump.examples.iotdemo.Capturing -workers 0 -remote

object Capturing extends AkkaApp with ArgumentsParser {
  private val LOG: Logger = LogUtil.getLogger(getClass)

  override val options: Array[(String, CLIOption[Any])] = Array(
    "workers" -> CLIOption[String]("<the worker list to deploy CapturingExecutor>", required = true),
    "remote" -> CLIOption[String]("<teh remote actor path>", required = true)
  )

  override def main(akkaConf: Config, args: Array[String]): Unit = {
    val config = parse(args)
    val context = ClientContext(akkaConf)
    val workerList = config.getString("workers")
    val remote = config.getString("remote")
    val userConfig = UserConfig.empty.withString(CapturingAppMaster.WORKER_LIST, workerList)
      .withString(CapturingExecutor.REMOTE_ACTOR, remote)
    val appId = context.submit(Application[CapturingAppMaster]("CapturingApp", userConfig))
    context.close()
    LOG.info(s"Capturing Application started with appId $appId !")
  }
}
