package com.peoplepattern.classify.core;

import static java.lang.Math.exp;

class Util {
  private Util() {}

  public static boolean sorted(final String[] labels) {
    if (labels.length < 2)
      return true;

    for (int i = 0; i < labels.length - 1; i++)
      if (labels[i].compareTo(labels[i + 1]) > 0)
        return false;

    return true;
  }
}
