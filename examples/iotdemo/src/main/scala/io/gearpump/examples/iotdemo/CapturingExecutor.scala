package io.gearpump.examples.iotdemo

import java.io.{DataOutputStream, ByteArrayOutputStream}
import javax.imageio.ImageIO

import akka.actor.Actor
import io.gearpump.cluster.{UserConfig, ExecutorContext}
import io.gearpump.util.LogUtil
import org.openimaj.image.{ImageUtilities, MBFImage}
import org.openimaj.image.colour.{RGBColour, Transforms}
import org.openimaj.image.processing.face.detection.HaarCascadeDetector
import org.openimaj.video.{VideoDisplayListener, VideoDisplay}
import org.openimaj.video.capture.VideoCapture
import org.slf4j.Logger
import CapturingExecutor._
import scala.collection.JavaConversions._

class CapturingExecutor(executorContext: ExecutorContext, userConf: UserConfig) extends Actor {
  import executorContext._
  implicit val dispatcher = context.system.dispatcher
  private val remote = context.actorSelection(userConf.getString(REMOTE_ACTOR).get)
  private val LOG: Logger = LogUtil.getLogger(getClass, executor = executorId, app = appId)
  private val faceDetector = new HaarCascadeDetector()
  private val video = new VideoCapture(320, 240)
  private val display = VideoDisplay.createVideoDisplay(video)
  private val out = new ByteArrayOutputStream()
  private val dataOut = new DataOutputStream(out)
  //private val duration = FiniteDuration(1000 / video.getFPS.toInt, TimeUnit.MILLISECONDS)

  display.addVideoListener(new VideoDisplayListener[MBFImage] {
    override def beforeUpdate(frame: MBFImage): Unit = {
      val faces = faceDetector.detectFaces(Transforms.calculateIntensity(frame))
      if (faces.size() > 0) {
        val image = ImageUtilities.createBufferedImageForDisplay(frame)
        ImageIO.write(image, "png", dataOut)
        val bytes = out.toByteArray
        remote ! bytes
        out.reset()
        faces.foreach(face => frame.drawShape(face.getBounds, RGBColour.RED))
      }
    }

    override def afterUpdate(videoDisplay: VideoDisplay[MBFImage]): Unit = {}
  })

  LOG.info(s"CapturingExecutor started! FPS: ${video.getFPS}")

  override def preStart() = {
  }

  override def receive: Receive = {
    case CaptureImage =>
  }

  override def postStop = {
    video.close()
    display.close()
  }
}

object CapturingExecutor {
  final val REMOTE_ACTOR = "remote_actor"
  case object CaptureImage
}
