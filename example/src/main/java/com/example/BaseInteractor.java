package com.example;

import rx.Observable;
import rx.Subscriber;

public class BaseInteractor<T> {
  //NOP
  public void execute(final Subscriber<T> useCaseSubscriber) {
    System.out.println("executing ...");
  }

  public Observable<T> getObservable() {
    //nop
    return null;
  }
}
