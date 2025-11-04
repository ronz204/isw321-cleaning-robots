package com.isw.app.presentation.views;

import java.util.logging.Logger;

public class BaseView {
  protected Logger logger;

  public BaseView(String name) {
    this.logger = Logger.getLogger(name);
  }
}
