package com.scene.file.zip.util;

public class ZipBreakException extends RuntimeException {
  public ZipBreakException(String msg) {
    super(msg);
  }

  public ZipBreakException(Exception e) {
    super(e);
  }

  public ZipBreakException() {
    super();
  }
}
