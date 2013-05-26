/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.ibatis.common.util;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 * Stopwatch class used for testing.
 */
public class Stopwatch {

  private static final Log log = LogFactory.getLog(Stopwatch.class);

  private Map taskMap = new HashMap();

  private String currentTaskName = null;
  private long currentTaskTime = 0;

  /*
   * Get an iterator of the tasks
   *
   * @return - the Iterator
   */
  public Iterator getTaskNames() {
    return taskMap.keySet().iterator();
  }

  /*
   * Get the number of times assigned to a task
   *
   * @param taskName - the name of the task
   * @return - the number of times
   */
  public long getTaskCount(String taskName) {
    return ((TaskStat) taskMap.get(taskName)).getCount();
  }

  /*
   * Get the total time added to a task
   *
   * @param taskName - the name of the task
   * @return - the total time added to the task
   */
  public long getTotalTaskTime(String taskName) {
    return ((TaskStat) taskMap.get(taskName)).getTotal();
  }

  /*
   * Get the maximum time added to a task
   *
   * @param taskName - the name of the task
   * @return - the maximum time added to a task
   */
  public long getMaxTaskTime(String taskName) {
    return ((TaskStat) taskMap.get(taskName)).getMax();
  }

  /*
   * Get the minimum time added to a task
   *
   * @param taskName - the name of the task
   * @return - the minimum time added to a task
   */
  public long getMinTaskTime(String taskName) {
    return ((TaskStat) taskMap.get(taskName)).getMin();
  }

  /*
   * Get the average time added to a task
   *
   * @param taskName - the name of the task
   * @return - the average time added to a task
   */
  public long getAvgTaskTime(String taskName) {
    return ((TaskStat) taskMap.get(taskName)).getAverage();
  }

  /*
   * Start (create) a task
   *
   * @param taskName - the name of the task
   */
  public void start(String taskName) {
    if (log.isDebugEnabled()) {
      log.debug("Starting: " + taskName);
    }
    this.currentTaskName = taskName;
    currentTaskTime = System.currentTimeMillis();
  }

  /*
   * Stop the timer on a task
   */
  public void stop() {
    if (log.isDebugEnabled()) {
      log.debug("Stopping: " + currentTaskName);
    }
    currentTaskTime = System.currentTimeMillis() - currentTaskTime;
    appendTaskTime(currentTaskName, currentTaskTime);
  }

  private synchronized void appendTaskTime(String taskName, long taskTime) {
    TaskStat stat = (TaskStat) taskMap.get(taskName);
    if (stat == null) {
      stat = new TaskStat();
      taskMap.put(taskName, stat);
    }
    stat.appendTaskTime(taskTime);
  }

  /*
   * Merge another StopWatch into this one
   *
   * @param watch - the StopWatch to merge into this one
   */
  public void mergeStopwatch(Stopwatch watch) {
    Iterator names = watch.getTaskNames();
    while (names.hasNext()) {
      String name = (String) names.next();
      long taskTime = watch.getTotalTaskTime(name);
      appendTaskTime(name, taskTime);
    }
  }

  /*
   * Reset all of the timers in this StopWatch
   */
  public synchronized void reset() {
    taskMap.clear();
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("Task,Count,Total,Max,Min,Avg\n");
    Iterator names = getTaskNames();
    while (names.hasNext()) {
      String name = (String) names.next();
      long taskCount = getTaskCount(name);
      long taskTime = getTotalTaskTime(name);
      long taskMin = getMinTaskTime(name);
      long taskMax = getMaxTaskTime(name);
      long taskAvg = getAvgTaskTime(name);
      buffer.append(name + "," + taskCount + "," + taskTime + "," + taskMax + "," + taskMin + "," + taskAvg + "\n");
    }
    return buffer.toString();
  }

  private class TaskStat {
    private static final long UNSET = -999999;

    private long count = 0;
    private long total = 0;
    private long min = UNSET;
    private long max = UNSET;

    /*
     * Add some time to a task
     *
     * @param taskTime - the time to add
     */
    public void appendTaskTime(long taskTime) {
      count++;
      total += taskTime;
      if (max == UNSET || taskTime > max) {
        max = taskTime;
      }
      if (min == UNSET || taskTime < min) {
        min = taskTime;
      }
    }

    /*
     * Get the total time for the task
     *
     * @return - the total time
     */
    public long getTotal() {
      return total;
    }

    /*
     * Get the maximum of the times added to the task
     *
     * @return - the max value
     */
    public long getMax() {
      return max;
    }

    /*
     * Get the minimum of the times added to the task
     *
     * @return - the minimum value
     */
    public long getMin() {
      return min;
    }

    /*
     * Get the number of times added to the task
     *
     * @return - the number of times
     */
    public long getCount() {
      return count;
    }

    /*
     * Get the average of the times added to the task
     *
     * @return - the average
     */
    public long getAverage() {
      if (count > 0) {
        return Math.round((double) total / (double) count);
      } else {
        return 0;
      }
    }

  }

}



