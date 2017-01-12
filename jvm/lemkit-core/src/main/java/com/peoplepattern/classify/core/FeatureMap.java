package com.peoplepattern.classify.core;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.peoplepattern.classify.core.ClassifierIO.*;
import static java.lang.String.format;

/**
 * Map from features (encoded as string) to their index in a vector space
 */
public interface FeatureMap {

  /**
   * Retrieve the index of a feature
   *
   * <p>The maximum value of this (+1) is given by {@link #size}
   *
   * @param feature the string representation of a feature
   * @return the integer index of the feature in a vector space; may return
   *         -1 if the feature isn't known to be in the feature set; it may
   *         <i>not</i>, too, if say implemented as a {@link HashedFeatureMap}
   */
  public int indexOfFeature(String feature);

  /**
   * Retrieve the size or dimensionality of the vector space mapped into
   *
   * <p>In the case of an exact feature map ({@link ExactFeatureMap}) returns
   * the total number of features in the problem space; for an approximate
   * feature map (e.g. {@link HashedFeatureMap}) there may be more features
   * in the problem space than the value returned here; either way, this
   * size is 1 + the maximum value of {@link #indexOfFeature}.
   *
   * @return the size of the feature space being mapped into; equals 1 +
   *         the maximum value of {@link #indexOfFeature}
   */
  public int size();

  /**
   * The signature of the {@link FeatureFunction} used to generate the
   * {@link FeatureBundle} this map is applied to
   *
   * <p>It is important to couple the vectors used in ML classification
   * with the feature functions used to generate them, for all but the
   * most trivial feature functions. This feature map plays a crucial
   * role translating between string-oriented {@link FeatureBundle}s
   * and the oblique vectors which are operated on by
   * {@link LinearClassifier}s. Thus, this function signature value
   * is used to ensure consistency and sanity during classification.
   *
   * @return the long-valued signature of the associated {@link FeatureFunction}
   * @see FeatureFunction#functionSig
   */
  public long functionSig();

  /**
   * Write to to a stream
   *
   * <p>The feature map is written efficiently to a binary stream.
   *
   * <p>Used for cross-languge interoperability; for JVM-internal
   * applications standard Java serialization should work too.
   *
   * <p><b>This method does NOT close the stream.</b>
   *
   * @param out the stream to write to
   * @throws IOException if any problem occurs while writing
   */
  public void writeToStream(DataOutputStream out) throws IOException;

  /**
   * Convert to a JSON object
   *
   * @return a JSON value representing this map
   */
  public JsonValue toJson();

  /**
   * Generate a reader to read a feature map from a binary stream
   *
   * <p>Used for cross-languge interoperability; for JVM-internal
   * applications standard Java serialization should work too.
   *
   * <p>Currently supports {@link HashedFeatureMap} and {@link ExactFeatureMap}.
   *
   * @param functionSig the feature function signature, probably stored earlier
   *                    in the data stream
   * @return a new binary reader for feature maps
   */
  public static BinarySupport.BinaryReader<FeatureMap> binaryReader(final long functionSig) {
    return new BinarySupport.BinaryReader<FeatureMap>() {
      public FeatureMap readFromStream(final DataInputStream in) throws IOException {
        final short code = in.readShort();
        switch (code) {
          case FEATURE_TYPE_HASHED: {
            final int size = in.readInt();
            final int seed = in.readInt();
            return new HashedFeatureMap(functionSig, size, seed);
          }

          case FEATURE_TYPE_EXACT: {
            final int size = in.readInt();
            final String[] features = new String[size];
            for (int i = 0; i < size; i++)
              features[i] = readString(in);
            return new ExactFeatureMap(functionSig, features);
          }

          default:
            throw new IOException(format("Unexpected code: %d", code));
        }
      }
    };
  }

  /**
   * Construct a {@link JsonSupport.Parser} to read {@link FeatureMap} from JSON data
   *
   * <p>Currently only supports hashed ({@link HashedFeatureMap}) and
   * exact ({@link ExactFeatureMap}) maps.
   *
   * @param functionSig the signature of the feature function of the
   *                    containing object
   * @return a new JSON reader for feature maps
   */
  public static JsonSupport.Parser<FeatureMap> jsonReader(final long functionSig) {
    return new JsonSupport.Parser<FeatureMap>() {
      public FeatureMap fromJson(final JsonValue json) {
        if (json == null)
          throw new IllegalArgumentException("JSON for features map cannot be null");

        if (!json.isObject())
          throw new IllegalArgumentException("JSON for feature map must be an object");

        final JsonObject jobj = json.asObject();

        final String typeS;
        {
          final JsonValue typeJ = jobj.get("type");
          if (typeJ == null)
            throw new IllegalArgumentException("must contain type field");
          if (!typeJ.isString())
            throw new IllegalArgumentException("type value must be string");
          typeS = typeJ.asString();
        }

        final JsonValue functionSigJ = jobj.get("function_sig");

        // It's OK if this value is missing in this context
        // But if it's present, check it equals the value we're expecting
        if (functionSigJ != null) {
          if (!functionSigJ.isNumber())
            throw new IllegalArgumentException("function_sig value must be numeric");

          if (functionSigJ.asLong() != functionSig)
            throw new IllegalArgumentException("function_sig does not match containing object");
        }

        switch (typeS) {
          case "exact":
            return ExactFeatureMap.jsonParser(functionSig).fromJson(jobj);

          case "hashed":
            return HashedFeatureMap.jsonParser(functionSig).fromJson(jobj);

          default:
            throw new IllegalArgumentException(format("feature map type %s not supported", typeS));
        }
      }
    };
  }
}
