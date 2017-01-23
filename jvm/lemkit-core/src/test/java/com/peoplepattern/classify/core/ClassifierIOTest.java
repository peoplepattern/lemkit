package com.peoplepattern.classify.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.peoplepattern.classify.core.ClassifierIO.writeString;
import static com.peoplepattern.classify.core.ClassifierIO.readString;

public class ClassifierIOTest {

  @Test
  public void testStringIO() {
    try {
      final String expected = "THIS IS SPARTA!!!";

      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream(bos);
      writeString(dos, expected);
      dos.close();

      final byte[] buf = bos.toByteArray();
      final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buf));
      final String output = readString(dis);
      dis.close();

      assertEquals(expected, output);
    } catch (IOException e) {
      fail("Failed IOException: " + e);
    }
  }
}
