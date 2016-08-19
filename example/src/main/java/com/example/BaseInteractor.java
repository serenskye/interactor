package com.example;

import rx.Subscriber;

public class BaseInteractor<T> {
  //NOP
  public void execute(final Subscriber<T> useCaseSubscriber) {
    System.out.println("executing ...");
  }
}
