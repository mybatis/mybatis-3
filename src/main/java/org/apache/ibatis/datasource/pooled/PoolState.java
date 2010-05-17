package org.apache.ibatis.datasource.pooled;

import java.util.ArrayList;
import java.util.List;

public class PoolState {

  protected PooledDataSource dataSource;

  protected List idleConnections = new ArrayList();
  protected List activeConnections = new ArrayList();
  protected long requestCount = 0;
  protected long accumulatedRequestTime = 0;
  protected long accumulatedCheckoutTime = 0;
  protected long claimedOverdueConnectionCount = 0;
  protected long accumulatedCheckoutTimeOfOverdueConnections = 0;
  protected long accumulatedWaitTime = 0;
  protected long hadToWaitCount = 0;
  protected long badConnectionCount = 0;

  public PoolState(PooledDataSource dataSource) {
    this.dataSource = dataSource;
  }

  public synchronized long getRequestCount() {
    return requestCount;
  }

  public synchronized long getAverageRequestTime() {
    return requestCount == 0 ? 0 : accumulatedRequestTime / requestCount;
  }

  public synchronized long getAverageWaitTime() {
    return hadToWaitCount == 0 ? 0 : accumulatedWaitTime / hadToWaitCount;

  }

  public synchronized long getHadToWaitCount() {
    return hadToWaitCount;
  }

  public synchronized long getBadConnectionCount() {
    return badConnectionCount;
  }

  public synchronized long getClaimedOverdueConnectionCount() {
    return claimedOverdueConnectionCount;
  }

  public synchronized long getAverageOverdueCheckoutTime() {
    return claimedOverdueConnectionCount == 0 ? 0 : accumulatedCheckoutTimeOfOverdueConnections / claimedOverdueConnectionCount;
  }

  public synchronized long getAverageCheckoutTime() {
    return requestCount == 0 ? 0 : accumulatedCheckoutTime / requestCount;
  }


  public synchronized int getIdleConnectionCount() {
    return idleConnections.size();
  }

  public synchronized int getActiveConnectionCount() {
    return activeConnections.size();
  }

  public synchronized String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("\n===CONFINGURATION==============================================");
    buffer.append("\n jdbcDriver                     ").append(dataSource.getDriver());
    buffer.append("\n jdbcUrl                        ").append(dataSource.getUrl());
    buffer.append("\n jdbcUsername                   ").append(dataSource.getUsername());
    buffer.append("\n jdbcPassword                   ").append((dataSource.getPassword() == null ? "NULL" : "************"));
    buffer.append("\n poolMaxActiveConnections       ").append(dataSource.poolMaximumActiveConnections);
    buffer.append("\n poolMaxIdleConnections         ").append(dataSource.poolMaximumIdleConnections);
    buffer.append("\n poolMaxCheckoutTime            ").append(dataSource.poolMaximumCheckoutTime);
    buffer.append("\n poolTimeToWait                 ").append(dataSource.poolTimeToWait);
    buffer.append("\n poolPingEnabled                ").append(dataSource.poolPingEnabled);
    buffer.append("\n poolPingQuery                  ").append(dataSource.poolPingQuery);
    buffer.append("\n poolPingConnectionsNotUsedFor  ").append(dataSource.poolPingConnectionsNotUsedFor);
    buffer.append("\n ---STATUS-----------------------------------------------------");
    buffer.append("\n activeConnections              ").append(getActiveConnectionCount());
    buffer.append("\n idleConnections                ").append(getIdleConnectionCount());
    buffer.append("\n requestCount                   ").append(getRequestCount());
    buffer.append("\n averageRequestTime             ").append(getAverageRequestTime());
    buffer.append("\n averageCheckoutTime            ").append(getAverageCheckoutTime());
    buffer.append("\n claimedOverdue                 ").append(getClaimedOverdueConnectionCount());
    buffer.append("\n averageOverdueCheckoutTime     ").append(getAverageOverdueCheckoutTime());
    buffer.append("\n hadToWait                      ").append(getHadToWaitCount());
    buffer.append("\n averageWaitTime                ").append(getAverageWaitTime());
    buffer.append("\n badConnectionCount             ").append(getBadConnectionCount());
    buffer.append("\n===============================================================");
    return buffer.toString();
  }

}
