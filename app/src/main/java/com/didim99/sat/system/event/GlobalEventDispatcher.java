package com.didim99.sat.system.event;

/**
 * Created by didim99 on 31.05.20.
 */
public class GlobalEventDispatcher {
  private GlobalEventListener globalEventListener;
  private GlobalEvent lastPendingEvent;

  public void registerEventListener(GlobalEventListener listener) {
    globalEventListener = listener;
    if (lastPendingEvent != null) {
      listener.onGlobalEvent(lastPendingEvent);
      lastPendingEvent = null;
    }
  }

  public void unregisterEventListener() {
    globalEventListener = null;
  }

  public void dispatchGlobalEvent(GlobalEvent event) {
    if (globalEventListener != null)
      globalEventListener.onGlobalEvent(event);
    else lastPendingEvent = event;
  }
}
