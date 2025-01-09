package com.ygo.robot.control.capability.function;

public interface EFunction {

  default void abc(boolean x) {
    if (x) {
      execute(x);
    }
  }

  <result> result execute(boolean canExecute);
}
