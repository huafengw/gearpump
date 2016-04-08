package io.gearpump.examples.iotdemo

import java.awt.Font
import java.io.{DataInputStream, ByteArrayInputStream}
import javax.swing.UIManager

import io.gearpump.examples.iotdemo.AnalyzeTask.FrameWrapper

import org.openimaj.image.{DisplayUtilities, ImageUtilities, MBFImage}
import io.gearpump.Message
import io.gearpump.cluster.UserConfig
import io.gearpump.streaming.task.{Task, TaskContext}

class AnalyzeTask(taskContext: TaskContext, conf: UserConfig) extends Task(taskContext, conf) {
  //java.awt.EventQueue.invokeLater(new FrameWrapper(this))
  UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel")
  UIManager.getLookAndFeelDefaults().put("defaultFont", new Font("Microsoft Yahei",Font.PLAIN,13))
  val frame = new CapturingFrame(this)
  frame.setVisible(true)

  override def onNext(msg: Message): Unit = {
    val remoteImage = msg.msg.asInstanceOf[RemoteImage]
    val in = new ByteArrayInputStream(remoteImage.bytes)
    val data = new DataInputStream(in)
    val image = ImageUtilities.readMBF(data)
    frame.remoteImageArrived(image, remoteImage.hostName, remoteImage.timeStamp)
    data.close()
  }

  override def onStop(): Unit = {
    frame.dispose()
  }
}

object AnalyzeTask {
  class FrameWrapper(analyzeTask: AnalyzeTask) extends Runnable {
    override def run(): Unit = {
      new CapturingFrame(analyzeTask).setVisible(true)
    }
  }
}
