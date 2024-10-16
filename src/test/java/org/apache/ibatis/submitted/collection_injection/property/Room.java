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

import java.util.List;

public class Room {
  private int id;
  private String name;
  private RoomDetail roomDetail;
  private List<Furniture> furniture;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public RoomDetail getRoomDetail() {
    return roomDetail;
  }

  public void setRoomDetail(RoomDetail roomDetail) {
    this.roomDetail = roomDetail;
  }

  public List<Furniture> getFurniture() {
    return furniture;
  }

  public void setFurniture(List<Furniture> furniture) {
    this.furniture = furniture;
  }

  @Override
  public String toString() {
    return "Room{" + "id=" + id + ", name='" + name + '\'' + ", roomDetail=" + roomDetail + ", furniture=" + furniture
        + '}';
  }
}
