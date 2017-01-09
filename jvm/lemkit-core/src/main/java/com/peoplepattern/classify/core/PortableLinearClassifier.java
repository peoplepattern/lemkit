package com.peoplepattern.classify.core;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

import static com.peoplepattern.classify.core.BinarySupport.BinaryReader;
import static com.peoplepattern.classify.core.ClassifierIO.*;
import static com.peoplepattern.classify.core.JsonSupport.*;
import static java.lang.String.format;

public final class PortableLinearClassifier implements Classifier<FeatureBundle>, Serializable,
    JsonSupport, BinarySupport {

  static final long serialVersionUID = 1L;

  private final LinearClassifier model;
  private final FeatureMap map;

  public PortableLinearClassifier(final LinearClassifier c, final FeatureMap m) {
    model = c;
    map = m;
  }

  public Classification predict(final FeatureBundle bundle) {
    return model.predict(bundle.toDatum(map));
  }

  public FeatureMap getFeatureMap() {
    return map;
  }

  public String[] getLabels() {
    return model.getLabels();
  }

  public long functionSig() {
    return model.functionSig();
  }

  public void writeToStream(final DataOutputStream out) throws IOException {
    out.writeInt(MAGIC_NUMBER);
    out.writeShort(MAJOR_VERSION);
    out.writeShort(MINOR_VERSION);
    out.writeLong(functionSig());

    out.writeShort(LABELS);
    final String[] labels = getLabels();
    out.writeInt(labels.length);
    for (String label : labels)
      writeString(out, label);

    out.writeShort(FEATURE_MAP);
    map.writeToStream(out);

    out.writeShort(WEIGHTS);
    model.writeWeightsToStream(out);
  }

  public static BinaryReader<PortableLinearClassifier> BREADER =
      new BinaryReader<PortableLinearClassifier>() {
        public PortableLinearClassifier readFromStream(final DataInputStream in) throws IOException {
          {
            final int magicNum = in.readInt();
            if (magicNum != MAGIC_NUMBER)
              throw new IOException(format("Invalid magic number: %X", magicNum));

          }
          {
            final short majorVer = in.readShort();
            if (majorVer != MAJOR_VERSION)
              throw new IOException(format("Invalid major version: %d", majorVer));
          }
          {
            final short minorVer = in.readShort();
            if (minorVer != MINOR_VERSION)
              throw new IOException(format("Invalide minor version: %d", minorVer));
          }

          final long functionSig = in.readLong();

          String[] labels = null;
          Vec[] params = null;
          FeatureMap map = null;

          while (labels == null && params == null && map == null) {
            final short nextAction = in.readShort();
            switch (nextAction) {
              case LABELS: {

                if (labels != null)
                  throw new IOException("Labels encoded twice in stream");

                final int numLabels = in.readInt();
                labels = new String[numLabels];
                for (int i = 0; i < numLabels; i++)
                  labels[i] = readString(in);

                break;
              }

              case FEATURE_MAP: {
                if (map != null)
                  throw new IOException("Feature map encoded twice in stream");

                map = FeatureMap.binaryReader(functionSig).readFromStream(in);

                break;
              }

              case WEIGHTS: {
                if (params != null)
                  throw new IOException("Model parameters encoded twice in stream");

                final int num = in.readInt();
                params = new Vec[num];
                for (int i = 0; i < num; i++)
                  params[i] = Vec.readVec(in);

                break;
              }

              default:
                throw new IOException(format("Unexpected code: %d", nextAction));
            }
          }

          final LinearClassifier model = new LinearClassifier(functionSig, labels, params);
          return new PortableLinearClassifier(model, map);
        }

      };

  public JsonValue toJson() {
    final JsonObject json = Json.object();
    json.add("function_sig", functionSig());
    json.add("features", map.toJson());
    json.add("labels", Json.array(model.getLabels()));
    json.add("weights", model.getWeightsJson());
    return json;
  }

  /**
   * JSON parser and factory for {@link PortableLinearClassifier}
   */
  public static Parser<PortableLinearClassifier> JPARSER = new Parser<PortableLinearClassifier>() {
    public PortableLinearClassifier fromJson(JsonValue json) {
      if (!json.isObject())
        throw new IllegalArgumentException("JSON not formatted as classification");

      final JsonObject jsonO = json.asObject();

      final long functionSig;
      {
        final JsonValue sigV = jsonO.get("function_sig");
        if (sigV == null)
          throw new IllegalArgumentException("JSON must contain function_sig key");
        if (!sigV.isNumber())
          throw new IllegalArgumentException("function_sig must be numeric");

        functionSig = sigV.asLong();
      }

      final FeatureMap map = FeatureMap.jsonReader(functionSig).fromJson(jsonO.get("features"));

      final String[] labels;
      {
        final JsonValue labelsV = jsonO.get("labels");
        if (labelsV == null)
          throw new IllegalArgumentException("JSON must contain labels key");
        if (!labelsV.isArray())
          throw new IllegalArgumentException("labels must be an array of string");
        final JsonArray labelsA = labelsV.asArray();
        final int n = labelsA.size();
        labels = new String[n];
        for (int i = 0; i < n; i++) {
          final JsonValue labelV = labelsA.get(i);
          if (labelV == null)
            throw new IllegalArgumentException("label value cannot be null");
          if (!labelV.isString())
            throw new IllegalArgumentException("label value must be string");
          labels[i] = labelV.asString();
        }
      }

      final Vec[] params = LinearClassifier.readWeightsFromJson(jsonO.get("weights"));


      final LinearClassifier model = new LinearClassifier(functionSig, labels, params);
      return new PortableLinearClassifier(model, map);
    }
  };
}
