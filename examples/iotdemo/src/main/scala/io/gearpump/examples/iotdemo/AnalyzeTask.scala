package io.gearpump.examples.iotdemo

import java.io.{DataInputStream, ByteArrayInputStream}
import javax.imageio.ImageIO

//import com.google.zxing.{MultiFormatReader, BinaryBitmap}
//import com.google.zxing.client.j2se.BufferedImageLuminanceSource
//import com.google.zxing.common.HybridBinarizer
import io.gearpump.Message
import io.gearpump.cluster.UserConfig
import io.gearpump.streaming.task.{Task, TaskContext}

class AnalyzeTask(taskContext: TaskContext, conf: UserConfig) extends Task(taskContext, conf) {

  override def onNext(msg : Message) : Unit = {
    val in = new ByteArrayInputStream(msg.msg.asInstanceOf[Array[Byte]])
    val data = new DataInputStream(in)
    val image = ImageIO.read(data)

//    val source = new BufferedImageLuminanceSource(image)
//    val bitmap = new BinaryBitmap(new HybridBinarizer(source))
//
//    try {
//      val result = new MultiFormatReader().decode(bitmap)
//      if (result != null) {
//        LOG.info(s"Decode result: $result")
//      }
//    } catch {
//      case ex: Throwable =>
//        //
//    }

    data.close()
  }
}
