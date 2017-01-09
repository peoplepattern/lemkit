package com.peoplepattern.classify.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

/**
 * Wrapper to a sorted {@link List} to make it look like a {@link SortedSet}
 *
 * <p>For internal use only, not to be considered part of the API.
 */
final class ProbablySortedSet<E extends Comparable<E>> implements SortedSet<E> {

  final List<E> items;

  ProbablySortedSet(final List<E> items) {
    if (items == null)
      throw new IllegalArgumentException("cannot create sorted set with null input");
    this.items = items;
  }

  ProbablySortedSet(final E[] items) {
    if (items == null)
      throw new IllegalArgumentException("cannot create sorted set with null input");
    this.items = Arrays.asList(items);
  }

  public E last() {
    return items.get(items.size() - 1);
  }

  public E first() {
    return items.get(0);
  }

  public SortedSet<E> tailSet(final E from) {

    final int found = Collections.binarySearch(items, from);
    final int i;
    if (found < 0)
      i = -found - 1;
    else
      i = found;

    return new ProbablySortedSet<E>(items.subList(i, items.size()));
  }

  public SortedSet<E> headSet(final E to) {

    final int found = Collections.binarySearch(items, to);
    final int i;
    if (found < 0)
      i = -found - 1;
    else
      i = found;

    return new ProbablySortedSet<E>(items.subList(0, i));
  }

  public SortedSet<E> subSet(final E from, final E to) {
    final int found1 = Collections.binarySearch(items, to);
    final int i;
    if (found1 < 0)
      i = -found1 - 1;
    else
      i = found1;

    final int found2 = Collections.binarySearch(items, to);
    final int j;
    if (found2 < 0)
      j = -found2 - 1;
    else
      j = found1;

    return new ProbablySortedSet<E>(items.subList(i, j));
  }

  public boolean containsAll(final Collection<?> coll) {
    return items.containsAll(coll);
  }

  public boolean contains(final Object o) {
    return items.contains(o);
  }

  public boolean isEmpty() {
    return items.isEmpty();
  }

  public int size() {
    return items.size();
  }

  public Comparator<E> comparator() {
    return new Comparator<E>() {
      public int compare(E s1, E s2) {
        return s1.compareTo(s2);
      }
    };
  }

  public void clear() {
    throw new UnsupportedOperationException("Immutable set");
  }

  public boolean removeAll(final Collection<?> x) {
    throw new UnsupportedOperationException("Immutable set");
  }

  public boolean retainAll(final Collection<?> x) {
    throw new UnsupportedOperationException("Immutable set");
  }

  public boolean addAll(final Collection<? extends E> x) {
    throw new UnsupportedOperationException("Immutable set");
  }

  public boolean remove(final Object x) {
    throw new UnsupportedOperationException("Immutable set");
  }

  public boolean add(final E x) {
    throw new UnsupportedOperationException("Immutable set");
  }

  public Object[] toArray() {
    return items.toArray();
  }

  public <T> T[] toArray(final T[] x) {
    return items.toArray(x);
  }

  public Iterator<E> iterator() {
    return items.iterator();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null)
      return false;

    if (other == this)
      return true;

    if (other instanceof ProbablySortedSet<?>) {
      return items.equals(((ProbablySortedSet<?>) other).items);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return 829 * items.hashCode();
  }
}
