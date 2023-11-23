/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.datasource.pooled;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.util.LockKit;

/**
 * @author Clinton Begin
 */
public class PoolState {

  private final LockKit.ReentrantLock reentrantLock;

  protected PooledDataSource dataSource;

  protected final List<PooledConnection> idleConnections = new ArrayList<>();
  protected final List<PooledConnection> activeConnections = new ArrayList<>();
  protected long requestCount;
  protected long accumulatedRequestTime;
  protected long accumulatedCheckoutTime;
  protected long claimedOverdueConnectionCount;
  protected long accumulatedCheckoutTimeOfOverdueConnections;
  protected long accumulatedWaitTime;
  protected long hadToWaitCount;
  protected long badConnectionCount;

  public PoolState(PooledDataSource dataSource) {
    this.dataSource = dataSource;
    this.reentrantLock = LockKit.obtainLock(dataSource);
  }

  public long getRequestCount() {
    reentrantLock.lock();
    try {
      return requestCount;
    } finally {
      reentrantLock.unlock();
    }
  }

  public long getAverageRequestTime() {
    reentrantLock.lock();
    try {
      return requestCount == 0 ? 0 : accumulatedRequestTime / requestCount;
    } finally {
      reentrantLock.unlock();
    }
  }

  public long getAverageWaitTime() {
    reentrantLock.lock();
    try {
      return hadToWaitCount == 0 ? 0 : accumulatedWaitTime / hadToWaitCount;
    } finally {
      reentrantLock.unlock();
    }
  }

  public long getHadToWaitCount() {
    reentrantLock.lock();
    try {
      return hadToWaitCount;
    } finally {
      reentrantLock.unlock();
    }
  }

  public long getBadConnectionCount() {
    reentrantLock.lock();
    try {
      return badConnectionCount;
    } finally {
      reentrantLock.unlock();
    }
  }

  public long getClaimedOverdueConnectionCount() {
    reentrantLock.lock();
    try {
      return claimedOverdueConnectionCount;
    } finally {
      reentrantLock.unlock();
    }
  }

  public long getAverageOverdueCheckoutTime() {
    reentrantLock.lock();
    try {
      return claimedOverdueConnectionCount == 0 ? 0
          : accumulatedCheckoutTimeOfOverdueConnections / claimedOverdueConnectionCount;
    } finally {
      reentrantLock.unlock();
    }
  }

  public long getAverageCheckoutTime() {
    reentrantLock.lock();
    try {
      return requestCount == 0 ? 0 : accumulatedCheckoutTime / requestCount;
    } finally {
      reentrantLock.unlock();
    }
  }

  public int getIdleConnectionCount() {
    reentrantLock.lock();
    try {
      return idleConnections.size();
    } finally {
      reentrantLock.unlock();
    }
  }

  public int getActiveConnectionCount() {
    reentrantLock.lock();
    try {
      return activeConnections.size();
    } finally {
      reentrantLock.unlock();
    }
  }

  @Override
  public String toString() {
    reentrantLock.lock();
    try {
      StringBuilder builder = new StringBuilder();
      builder.append("\n===CONFIGURATION==============================================");
      builder.append("\n jdbcDriver                     ").append(dataSource.getDriver());
      builder.append("\n jdbcUrl                        ").append(dataSource.getUrl());
      builder.append("\n jdbcUsername                   ").append(dataSource.getUsername());
      builder.append("\n jdbcPassword                   ")
          .append(dataSource.getPassword() == null ? "NULL" : "************");
      builder.append("\n poolMaxActiveConnections       ").append(dataSource.poolMaximumActiveConnections);
      builder.append("\n poolMaxIdleConnections         ").append(dataSource.poolMaximumIdleConnections);
      builder.append("\n poolMaxCheckoutTime            ").append(dataSource.poolMaximumCheckoutTime);
      builder.append("\n poolTimeToWait                 ").append(dataSource.poolTimeToWait);
      builder.append("\n poolPingEnabled                ").append(dataSource.poolPingEnabled);
      builder.append("\n poolPingQuery                  ").append(dataSource.poolPingQuery);
      builder.append("\n poolPingConnectionsNotUsedFor  ").append(dataSource.poolPingConnectionsNotUsedFor);
      builder.append("\n ---STATUS-----------------------------------------------------");
      builder.append("\n activeConnections              ").append(getActiveConnectionCount());
      builder.append("\n idleConnections                ").append(getIdleConnectionCount());
      builder.append("\n requestCount                   ").append(getRequestCount());
      builder.append("\n averageRequestTime             ").append(getAverageRequestTime());
      builder.append("\n averageCheckoutTime            ").append(getAverageCheckoutTime());
      builder.append("\n claimedOverdue                 ").append(getClaimedOverdueConnectionCount());
      builder.append("\n averageOverdueCheckoutTime     ").append(getAverageOverdueCheckoutTime());
      builder.append("\n hadToWait                      ").append(getHadToWaitCount());
      builder.append("\n averageWaitTime                ").append(getAverageWaitTime());
      builder.append("\n badConnectionCount             ").append(getBadConnectionCount());
      builder.append("\n===============================================================");
      return builder.toString();
    } finally {
      reentrantLock.unlock();
    }
  }

}
