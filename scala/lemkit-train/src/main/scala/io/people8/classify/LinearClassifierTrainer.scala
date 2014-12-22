package io.people8.classify

import data._

trait LinearClassifierTrainer {
  /**
   * Create a temporary file, with reasonable defaults for suffix and base dir.
   */
  def tmpFile(prefix: String, suffix: String = ".txt",
    baseDir: String = "/tmp") =
    java.io.File.createTempFile(prefix, suffix, new java.io.File(baseDir))
}
