package com.peoplepattern.classify.train

case class Example[A](label: String, item: A, importance: Double = 1.0)
