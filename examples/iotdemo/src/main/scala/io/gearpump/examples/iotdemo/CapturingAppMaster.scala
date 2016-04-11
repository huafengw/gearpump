package io.gearpump.examples.iotdemo

import akka.actor.{Deploy, Props}
import akka.remote.RemoteScope
import com.typesafe.config.Config
import io.gearpump.cluster.ClientToMaster.ShutdownApplication
import io.gearpump.cluster.appmaster.ExecutorSystemScheduler.{StartExecutorSystems, ExecutorSystemJvmConfig, StartExecutorSystemTimeout, ExecutorSystemStarted}
import io.gearpump.cluster.scheduler.{Relaxation, Resource, ResourceRequest}
import io.gearpump.cluster.{ExecutorContext, ApplicationMaster, AppDescription, AppMasterContext}
import io.gearpump.util.{Util => GUtil, LogUtil, Constants}
import org.slf4j.Logger

class CapturingAppMaster(appContext : AppMasterContext, app : AppDescription) extends ApplicationMaster  {
  import appContext._
  import context.dispatcher
  implicit val timeout = Constants.FUTURE_TIMEOUT
  private val LOG: Logger = LogUtil.getLogger(getClass, app = appId)
  private val workerList = app.userConfig.getString(CapturingAppMaster.WORKER_LIST).get.split(",")
  protected var currentExecutorId = 0

  override def preStart(): Unit = {
    LOG.info(s"Capturing AppMaster started")
    val resources = workerList.map{ workerId =>
      ResourceRequest(Resource(1), workerId.toInt, relaxation = Relaxation.SPECIFICWORKER)
    }
    masterProxy.tell(StartExecutorSystems(resources, getExecutorJvmConfig), self)
  }

  override def receive: Receive = {
    case ExecutorSystemStarted(executorSystem, _) =>
      import executorSystem.{address, resource => executorResource, worker}
      val executorContext = ExecutorContext(currentExecutorId, worker, appId, app.name, self, executorResource)
      //start executor
      val executor = context.actorOf(Props(classOf[CapturingExecutor], executorContext, app.userConfig)
        .withDeploy(Deploy(scope = RemoteScope(address))), currentExecutorId.toString)
      executorSystem.bindLifeCycleWith(executor)
      currentExecutorId += 1
    case StartExecutorSystemTimeout =>
      LOG.error(s"Failed to allocate resource in time")
      masterProxy ! ShutdownApplication(appId)
      context.stop(self)
  }

  private def getExecutorJvmConfig: ExecutorSystemJvmConfig = {
    val config: Config = app.clusterConfig
    val jvmSetting = GUtil.resolveJvmSetting(config.withFallback(context.system.settings.config)).executor
    ExecutorSystemJvmConfig(jvmSetting.classPath, jvmSetting.vmargs,
      appJar, username, config)
  }
}

object CapturingAppMaster {
  final val WORKER_LIST = "worker_list"
}
