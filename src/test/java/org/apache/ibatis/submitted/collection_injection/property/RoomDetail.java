/*
 *    Copyright 2009-2024 the original author or authors.
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
package org.apache.ibatis.submitted.collection_injection.property;

public class RoomDetail {

  private String wallType;
  private int wallHeight;
  private int roomSize;

  public String getWallType() {
    return wallType;
  }

  public void setWallType(String wallType) {
    this.wallType = wallType;
  }

  public int getWallHeight() {
    return wallHeight;
  }

  public void setWallHeight(int wallHeight) {
    this.wallHeight = wallHeight;
  }

  public int getRoomSize() {
    return roomSize;
  }

  public void setRoomSize(int roomSize) {
    this.roomSize = roomSize;
  }

  @Override
  public String toString() {
    return "RoomDetail{" + "wallType='" + wallType + '\'' + ", wallHeight=" + wallHeight + ", roomSize=" + roomSize
        + '}';
  }
}
