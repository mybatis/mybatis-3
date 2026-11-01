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
package org.apache.ibatis.submitted.collection_injection.immutable;

import java.util.List;

public class ImmutableRoom {
  private final int id;
  private final String name;
  private final ImmutableRoomDetail roomDetail;
  private final List<ImmutableFurniture> furniture;

  public ImmutableRoom(int id, String name, ImmutableRoomDetail roomDetail, List<ImmutableFurniture> furniture) {
    this.id = id;
    this.name = name;
    this.roomDetail = roomDetail;
    this.furniture = furniture;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public ImmutableRoomDetail getRoomDetail() {
    return roomDetail;
  }

  public List<ImmutableFurniture> getFurniture() {
    return furniture;
  }

  @Override
  public String toString() {
    return "ImmutableRoom{" + "id=" + id + ", name='" + name + '\'' + ", roomDetail=" + roomDetail + ", furniture="
        + furniture + '}';
  }
}
