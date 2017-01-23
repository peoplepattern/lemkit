package com.peoplepattern.classify.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Static values and utilities for reading/writing resources
 */
public interface ClassifierIO {

  /** Binary code to ensure the data stream is encoding a classifier */
  public static final int MAGIC_NUMBER = 0x6A48B9DD;

  /** The major version of the classifier object for data integrity */
  public static final short MAJOR_VERSION = 1;

  /** The minor version of the classifier object for data integrity */
  public static final short MINOR_VERSION = 1;

  /** Binary code for "classifier labels follow" */
  public static final short LABELS = 100;

  /** Binary code for "classifier feature map follows" */
  public static final short FEATURE_MAP = 110;

  /** Binary code indicating feature map is "exact" */
  public static final short FEATURE_TYPE_EXACT = 1;

  /** Binary code indicating feature map uses hash trick */
  public static final short FEATURE_TYPE_HASHED = 2;

  /** Binary code for "features follow" */
  public static final short FEAUTRES = 111;

  /** Binary code for "number of features follows" */
  public static final short FEATURES_SIZE = 112;

  /** Binary code for "classifier parameter weights follow" */
  public static final short WEIGHTS = 120;

  /** Binary code for "this vector is stored in a dense format" */
  public static final short WEIGHTS_TYPE_DENSE = 1;

  /** Binary code for "this vector is stored in a sparse format" */
  public static final short WEIGHTS_TYPE_SPARSE = 2;

  /**
   * Write a string into a data stream ensuring UTF-8 encoding
   *
   * <p>This method does <i>not</i> close the stream.
   *
   * @param out the data stream to write to
   * @param str the string to write out
   * @throws IOException if anything goes wrong with the writing
   */
  public static void writeString(final DataOutputStream out, final String str) throws IOException {
    final byte[] bytes = str.getBytes(UTF_8);
    out.writeInt(bytes.length);
    out.write(bytes, 0, bytes.length);
  }

  /**
   * Read a UTF-8 encoded string from a data stream
   *
   * <p>This method does <i>not</i> close the stream.
   *
   * @param in the stream to read from
   * @return the decoded string
   * @throws IOException in case anything goes wrong with the IO writing
   */
  public static String readString(DataInputStream in) throws IOException {
    final int len = in.readInt();
    final byte[] bytes = new byte[len];
    in.read(bytes);
    return new String(bytes, UTF_8);
  }

  /**
   * Write a {@link PortableLinearClassifier} to a file at the provided path
   *
   * <p>TODO support paths ending with gz with GZip-compressed data
   *
   * @param classifier the classifier to write to disk
   * @param path the system path of the file to write the classifier to
   * @throws IOException if anything goes wrong with the writing
   */
  public static void writeClassifier(final PortableLinearClassifier classifier, final String path)
      throws IOException {
    writeClassifier(classifier, new File(path));
  }

  /**
   * Write a {@link PortableLinearClassifier} to a file
   *
   * <p>TODO support paths ending with gz with GZip-compressed data
   *
   * @param classifier the classifier to write to disk
   * @param file the file to write the classifier to
   * @throws IOException if anything goes wrong with the writing
   */
  public static void writeClassifier(final PortableLinearClassifier classifier, final File file)
      throws IOException {
    final DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
    try {
      classifier.writeToStream(out);
    } finally {
      try {
        out.close();
      } catch (Throwable e) { /* eat this, throw the other exception */
      }
    }
  }
}
