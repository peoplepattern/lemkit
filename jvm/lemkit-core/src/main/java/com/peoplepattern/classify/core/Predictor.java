package com.peoplepattern.classify.core;

public interface Predictor<I, O> {

  public O predict(I inputs);

}
