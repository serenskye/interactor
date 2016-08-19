package com.example;

import com.interactor.Interactor;

@Interactor
public class ExampleInteractor extends BaseInteractor<String> {

  private int myInt;
  private MyPoJo pojo;

  public ExampleInteractor(int myInt, MyPoJo pojo){
    this.myInt = myInt;
    this.pojo = pojo;
  }

  public ExampleInteractor(MyPoJo pojo){
    this.myInt = 0;
    this.pojo = pojo;
  }
}
