package io.gearpump.examples.iotdemo

import java.io.{DataInputStream, ByteArrayInputStream}

import io.gearpump.examples.iotdemo.AnalyzeTask.FrameWrapper

import org.openimaj.image.{DisplayUtilities, ImageUtilities, MBFImage}
import io.gearpump.Message
import io.gearpump.cluster.UserConfig
import io.gearpump.streaming.task.{Task, TaskContext}

class AnalyzeTask(taskContext: TaskContext, conf: UserConfig) extends Task(taskContext, conf) {
  //val frame = DisplayUtilities.makeFrame("Analyze")
  //java.awt.EventQueue.invokeLater(new FrameWrapper(this))
  val frame = new CaptruingFrame(this)
  frame.setVisible(true)

  override def onNext(msg: Message): Unit = {
    val in = new ByteArrayInputStream(msg.msg.asInstanceOf[Array[Byte]])
    val data = new DataInputStream(in)
    val image = ImageUtilities.readMBF(data)
    //DisplayUtilities.display(image, frame)

    data.close()
  }

  override def onStop(): Unit = {

  }
}

object AnalyzeTask {
  class FrameWrapper(analyzeTask: AnalyzeTask) extends Runnable {
    override def run(): Unit = {
      new CaptruingFrame(analyzeTask).setVisible(true)
    }
  }
}
