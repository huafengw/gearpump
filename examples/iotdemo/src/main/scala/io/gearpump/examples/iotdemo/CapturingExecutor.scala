package io.gearpump.examples.iotdemo

import java.awt.Font
import java.io.{DataOutputStream, ByteArrayOutputStream}
import java.net.InetAddress
import javax.imageio.ImageIO
import javax.swing.UIManager

import akka.actor.Actor
import io.gearpump.cluster.{UserConfig, ExecutorContext}
import io.gearpump.util.LogUtil
import org.openimaj.image.{ImageUtilities, MBFImage}
import org.openimaj.image.colour.{RGBColour, Transforms}
import org.openimaj.image.processing.face.detection.HaarCascadeDetector
import org.openimaj.video.{VideoDisplayListener, VideoDisplay}
import org.slf4j.Logger
import CapturingExecutor._
import scala.collection.JavaConversions._

class CapturingExecutor(executorContext: ExecutorContext, userConf: UserConfig) extends Actor {
  import executorContext._
  implicit val dispatcher = context.system.dispatcher

  private val hostName = InetAddress.getLocalHost().getHostName()
  private val remote = context.actorSelection(userConf.getString(REMOTE_ACTOR).get)
  private val LOG: Logger = LogUtil.getLogger(getClass, executor = executorId, app = appId)
  private val video = Util.getVideoCapture(userConf.getString(CapturingExecutor.CAMERA).get)
  private val display = VideoDisplay.createVideoDisplay(video)
  private val out = new ByteArrayOutputStream()
  private val dataOut = new DataOutputStream(out)

  display.addVideoListener(new VideoDisplayListener[MBFImage] {
    private val faceDetector = new HaarCascadeDetector()

    override def beforeUpdate(frame: MBFImage): Unit = {
      val faces = faceDetector.detectFaces(Transforms.calculateIntensity(frame))
      if (faces.size() > 0) {
        val image = ImageUtilities.createBufferedImageForDisplay(frame)
        ImageIO.write(image, "png", dataOut)
        val bytes = out.toByteArray
        remote ! RemoteImage(bytes, hostName, System.currentTimeMillis())
        out.reset()
        faces.foreach(face => frame.drawShape(face.getBounds, RGBColour.RED))
      }
    }

    override def afterUpdate(videoDisplay: VideoDisplay[MBFImage]): Unit = {}
  })

  override def receive: Receive = null

  override def postStop = {
    video.close()
    display.close()
  }
}

object CapturingExecutor {
  final val REMOTE_ACTOR = "remote_actor"
  final val CAMERA = "camera"
}
