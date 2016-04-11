package io.gearpump.examples.iotdemo;

import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Util {
  private static final Logger log = LoggerFactory.getLogger(Util.class);

  public static VideoCapture getVideoCapture(String preferredName) {
    List<Device> devices = VideoCapture.getVideoDevices();
    Device result = null;
    for (Device device: devices) {
      if (device.getNameStr().startsWith(preferredName)) {
        result = device;
        break;
      }
    }
    VideoCapture videoCapture = null;
    if (result != null) {
      try {
        videoCapture = new VideoCapture(320, 240, result);
      } catch (VideoCaptureException e) {
        videoCapture = getVideoCapture();
      }
    }
    return videoCapture;
  }

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
