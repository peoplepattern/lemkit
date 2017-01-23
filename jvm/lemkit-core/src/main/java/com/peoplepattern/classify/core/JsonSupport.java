package com.peoplepattern.classify.core;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import static com.eclipsesource.json.WriterConfig.PRETTY_PRINT;

/**
 * Interface providing support for generating and writing JSON
 */
public interface JsonSupport {

  /**
   * Convert to JSON format
   *
   * @return binary JSON value
   */
  public JsonValue toJson();

  /**
   * Serialize as a JSON string
   *
   * @param pretty whether or not to return pretty-printed JSON
   * @return JSON string
   */
  default String toJsonString(boolean pretty) {
    if (pretty)
      return toJson().toString(PRETTY_PRINT);
    else
      return toJson().toString();
  }

  /**
   * Serialize as JSON string
   *
   * @return compact JSON string
   */
  default String toJsonString() {
    return toJson().toString();
  }

  /**
   * Write JSON format to a file
   *
   * @param f the file to write to
   * @throws IOException if any problems occur while writing
   */
  default void writeJson(File f) throws IOException {
    final Writer writer = new BufferedWriter(new FileWriter(f));
    try {
      writeJson(writer);
    } finally {
      writer.close();
    }
  }

  /**
   * Write JSON format to output
   *
   * @param w writer to write JSON to
   * @throws IOException if any problems occur while writing
   */
  default void writeJson(Writer w) throws IOException {
    toJson().writeTo(w);
  }

  /**
   * Interface for objects which can translate JSON to objects
   */
  public interface Parser<A> {

    /**
     * Translate a JSON value into an object
     *
     * @param json a binary JSON representation
     * @return a deserialized object
     * @throws IllegalArgumentException if any fields are missing, or the JSON
     *         is in an unexpected format
     */
    public A fromJson(JsonValue json);

    /**
     * Translate a string containing a JSON value into an object
     *
     * @param jsonStr a string containing JSON
     * @return a deserialized object
     * @throws IllegalArgumentException if any fields are missing, or the JSON
     *         is in an unexpected format
     */
    default A fromJsonString(final String jsonStr) {
      return fromJson(Json.parse(jsonStr));
    }

    /**
     * Read JSON from a reader, translate into an object
     *
     * @param reader a reader to read JSON content from
     * @return a deserialized object
     * @throws IOException if any problems occur with the IO read
     * @throws IllegalArgumentException if any fields are missing, or the JSON
     *         is in an unexpected format
     */
    default A readJson(final Reader reader) throws IOException {
      return fromJson(Json.parse(reader));
    }

    /**
     * Read JSON from a file, translate into an object
     *
     * @param file the file to read JSON content from
     * @return a deserialized object
     * @throws IOException if any problems occur with the IO read
     * @throws IllegalArgumentException if any fields are missing, or the JSON
     *         is in an unexpected format
     */
    default A readJson(final File file) throws IOException {
      final Reader reader = new BufferedReader(new FileReader(file));
      try {
        return readJson(reader);
      } finally {
        reader.close();
      }
    }
  }
}
