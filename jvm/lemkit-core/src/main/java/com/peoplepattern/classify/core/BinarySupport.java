package com.peoplepattern.classify.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Support for writing to a binary data stream
 *
 * <p>Engineered to support cross platform serialization.
 */
public interface BinarySupport {

  /**
   * Write to a binary data output stream.
   *
   * <p>This method must <i>not</i> close the stream
   *
   * @param out the stream to write to
   * @throws IOException if anything goes wrong with the writing
   */
  public void writeToStream(final DataOutputStream out) throws IOException;

  /**
   * Support for static factory objects which read resources from a stream
   *
   * <p>Engineered to support cross platform deserialization.
   */
  public interface BinaryReader<I> {

    /**
     * Deserializes and reads a resources from a binary stream
     *
     * <p>This method must <i>not</i> close the stream.
     *
     * @param in the stream being read from
     * @return a new object constructed from the data stream
     * @throws IOException in case anything goes wrong with the reading
     */
    public I readFromStream(final DataInputStream in) throws IOException;
  }
}
