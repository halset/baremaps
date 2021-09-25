/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.tile.postgres;

class PostgresUnionTileStoreTest {

  /*
  @Test
  void sameQueries() {
    List<PostgresQuery> queries =
        Arrays.asList(
            new PostgresQuery("a", 0, 20, "SELECT id, tags, geom FROM table"),
            new PostgresQuery("b", 0, 20, "SELECT id, tags, geom FROM table"));
    PostgresUnionTileStore tileStore = new PostgresUnionTileStore(null, queries);
    String query = tileStore.unionQuery(new Tile(0, 0, 10));
    assertEquals("", query);
  }

  @Test
  void differentConditions() {
    List<PostgresQuery> queries =
        Arrays.asList(
            new PostgresQuery("a", 0, 20, "SELECT id, tags, geom FROM table WHERE condition = 1"),
            new PostgresQuery("b", 0, 20, "SELECT id, tags, geom FROM table WHERE condition = 2"));
    PostgresUnionTileStore tileStore = new PostgresUnionTileStore(null, queries);
    String query = tileStore.unionQuery(new Tile(0, 0, 10));
    assertEquals("", query);
  }
   */
}
