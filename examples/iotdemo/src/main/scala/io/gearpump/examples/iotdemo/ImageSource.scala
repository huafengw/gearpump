package io.gearpump.examples.iotdemo

import akka.actor.{Props, ActorRef, Actor}
import io.gearpump.Message
import io.gearpump.cluster.UserConfig
import io.gearpump.examples.iotdemo.ImageSource.InnerActor
import io.gearpump.streaming.task.{StartTime, Task, TaskContext}
import io.gearpump.util.{ActorUtil, LogUtil}
import org.slf4j.Logger

class ImageSource(taskContext: TaskContext, conf: UserConfig) extends Task(taskContext, conf) {
  taskContext.system.actorOf(Props(classOf[InnerActor], self), "receiver")

  override def onNext(msg: Message) : Unit = {
    taskContext.output(msg)
  }
}

object ImageSource {
  class InnerActor(parent: ActorRef) extends Actor {
    private val LOG: Logger = LogUtil.getLogger(getClass)
    LOG.info(s"Actor path: ${ActorUtil.getFullPath(context.system, self.path)}")

    override def receive: Receive = {
      case bytes: Array[Byte] =>
        parent.tell(Message(bytes, System.currentTimeMillis()), parent)
    }
  }
}
