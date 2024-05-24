// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.common.event;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class TaskEventHub {

  public static final String EVENT_WORKER = "event-worker-%d";
  public static final String ANY_EVENT = "*";

  private static final List<TaskEventListener> EMPTY = ImmutableList.of();

  // Event executor
  private static ExecutorService executor = null;

  private String name;
  private Map<String, List<TaskEventListener>> listeners;

  public TaskEventHub(String name) {
    log.info("Create new EventHub: {}", name);

    this.name = name;
    this.listeners = new ConcurrentHashMap<>();
    TaskEventHub.init(1);
  }

  public static synchronized void init(int poolSize) {
    if (executor != null) {
      return;
    }
    log.debug("Init pool(size {}) for EventHub", poolSize);
    executor = new ThreadPoolExecutor(
        poolSize,
        poolSize,
        0L,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(),
        new BasicThreadFactory.Builder()
            .namingPattern(EVENT_WORKER)
            .build()
    );
  }

  public static synchronized boolean destroy(long timeout)
      throws InterruptedException {
    log.debug("Destroy pool for EventHub");
    executor.shutdown();
    return executor.awaitTermination(timeout, TimeUnit.SECONDS);
  }

  private static ExecutorService executor() {
    ExecutorService e = executor;
    Preconditions.checkState(e != null, "The event executor has been destroyed");
    return e;
  }

  public String name() {
    return this.name;
  }

  public boolean containsListener(String event) {
    List<TaskEventListener> ls = this.listeners.get(event);
    return ls != null && ls.size() > 0;
  }

  public List<TaskEventListener> listeners(String event) {
    List<TaskEventListener> ls = this.listeners.get(event);
    return ls == null ? EMPTY : Collections.unmodifiableList(ls);
  }

  public void listen(String event, TaskEventListener listener) {
    Preconditions.checkNotNull(event, "event");
    Preconditions.checkNotNull(listener, "event listener");

    if (!this.listeners.containsKey(event)) {
      this.listeners.putIfAbsent(event, new CopyOnWriteArrayList<>());
    }
    List<TaskEventListener> ls = this.listeners.get(event);
    assert ls != null : this.listeners;
    ls.add(listener);
  }

  public List<TaskEventListener> unlisten(String event) {
    List<TaskEventListener> ls = this.listeners.remove(event);
    return ls == null ? EMPTY : Collections.unmodifiableList(ls);
  }

  public int unlisten(String event, TaskEventListener listener) {
    List<TaskEventListener> ls = this.listeners.get(event);
    if (ls == null) {
      return 0;
    }

    int count = 0;
    while (ls.remove(listener)) {
      count++;
    }
    return count;
  }

  public Future<Integer> notify(String event, @Nullable Object... args) {
    List<TaskEventListener> all = Collections.synchronizedList(new ArrayList<>());

    List<TaskEventListener> ls = this.listeners.get(event);
    if (ls != null && !ls.isEmpty()) {
      all.addAll(ls);
    }
    List<TaskEventListener> lsAny = this.listeners.get(ANY_EVENT);
    if (lsAny != null && !lsAny.isEmpty()) {
      all.addAll(lsAny);
    }

    if (all.isEmpty()) {
      return CompletableFuture.completedFuture(0);
    }

    ListenedEvent ev = new ListenedEvent(this, event, args);

    // The submit will catch params: `all`(Listeners) and `ev`(Event)
    return executor().submit(() -> {
      int count = 0;
      // Notify all listeners, and ignore the results
      for (TaskEventListener listener : all) {
        try {
          listener.event(ev);
          count++;
        } catch (Throwable e) {
          log.warn("Failed to handle event: {}", ev, e);
        }
      }
      return count;
    });
  }

  public Object call(String event, @Nullable Object... args) {
    List<TaskEventListener> ls = this.listeners.get(event);
    if (ls == null) {
      throw new RuntimeException("Not found listener for: " + event);
    } else if (ls.size() != 1) {
      throw new RuntimeException("Too many listeners for: " + event);
    }
    TaskEventListener listener = ls.get(0);
    return listener.event(new ListenedEvent(this, event, args));
  }

}