package com.peoplepattern.classify.core;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import static java.lang.String.format;

public class Scored<I extends Comparable<I>> implements Comparable<Scored<I>>, Serializable {

  final static long serialVersionUID = 1L;

  private final I item;
  private final double score;

  public Scored(final I item, final double score) {
    if (item == null)
      throw new IllegalArgumentException("null item");

    this.item = item;
    this.score = score;
  }

  public I item() {
    return item;
  }

  public double score() {
    return score;
  }

  public int compareTo(Scored<I> other) {
    final int itemcmp = item.compareTo(other.item);
    if (itemcmp == 0)
      if (score < other.score)
        return -1;
      else if (score > other.score)
        return 1;
      else
        return 0;
    else
      return itemcmp;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null)
      return false;

    if (other == this)
      return true;

    if (other instanceof Scored<?>) {
      final Scored<?> scored = (Scored<?>) other;
      return item.equals(scored.item) && score == scored.score;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return 7 * Double.hashCode(score) + 11 * item.hashCode();
  }

  @Override
  public String toString() {
    return format("<%s, %.4f>", item.toString(), score);
  }

  public static <I extends Comparable<I>> Scored<I> maxByScore(Iterable<Scored<I>> list) {
    final Iterator<Scored<I>> iter = list.iterator();
    if (!iter.hasNext())
      throw new IllegalArgumentException("Cannot take max of empty set");

    Scored<I> max = iter.next();
    while (iter.hasNext()) {
      final Scored<I> curr = iter.next();
      if (curr.score > max.score)
        max = curr;
    }
    return max;
  }

  public static <I extends Comparable<I>> boolean uniqueItems(Collection<Scored<I>> list) {
    final Set<I> items = new HashSet<I>();
    for (Scored<I> scored : list)
      items.add(scored.item);

    final int numItems = items.size();
    final int numScored = list.size();

    if (numItems > numScored) {
      final String msg = "Somehow the size of items > the size of scored items";
      throw new RuntimeException(msg);
    }

    return numItems == numScored;
  }

  public static <T extends Comparable<T>> JsonValue asJsonObject(final Collection<Scored<T>> c) {
    final JsonObject j = Json.object();
    for (Scored<T> s : c)
      j.add(s.item().toString(), s.score());
    return j;
  }

  public static Collection<Scored<String>> fromJsonObject(JsonValue json) {
    if (json == null)
      throw new IllegalArgumentException("JSON cannot be null");

    if (!json.isObject())
      throw new IllegalArgumentException("JSON must be an object");

    final Collection<Scored<String>> s = new ArrayList<Scored<String>>();

    for (JsonObject.Member item : json.asObject()) {
      final JsonValue value = item.getValue();
      if (value == null)
        throw new IllegalArgumentException("Score cannot be null");

      if (!value.isNumber())
        throw new IllegalArgumentException("Score must be a number");

      s.add(new Scored<String>(item.getName(), value.asDouble()));
    }

    return s;
  }
}
