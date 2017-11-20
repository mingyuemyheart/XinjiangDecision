package com.scene.file.zip.util;

public class ZipException extends RuntimeException {
  public ZipException(String msg) {
    super(msg);
  }

  public ZipException(Exception e) {
    super(e);
  }

  public ZipException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
