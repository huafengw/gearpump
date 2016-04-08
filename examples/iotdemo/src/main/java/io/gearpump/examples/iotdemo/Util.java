package io.gearpump.examples.iotdemo;

import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

import java.util.List;

public class Util {

  public static VideoCapture getVideoCapture() {
    List<Device> devices = VideoCapture.getVideoDevices();
    VideoCapture videoCapture = null;
    for (Device device: devices) {
      try {
        videoCapture = new VideoCapture(320, 240, device);
        break;
      } catch (VideoCaptureException e) {

      }
    }
    return videoCapture;
  }
}
