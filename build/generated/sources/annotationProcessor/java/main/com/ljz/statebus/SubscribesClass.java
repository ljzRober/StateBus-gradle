package com.ljz.statebus;

import annotation.AbstractSubscribers;
import annotation.bean.SubscribeInfo;
import annotation.bean.SubscribeMethod;

public final class SubscribesClass extends AbstractSubscribers {
  static {
    putIndex(new SubscribeInfo("Main"));
    putMethod("Main", new SubscribeMethod("StateListener", "state.State$trigger"));
  }

  public static void importPackages(SubscribeInfo SubscribeInfo, SubscribeMethod SubscribeMethod) {
  }
}
