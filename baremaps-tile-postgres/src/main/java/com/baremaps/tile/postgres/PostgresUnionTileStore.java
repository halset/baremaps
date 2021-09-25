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

import com.baremaps.tile.Tile;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.TileStoreException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import javax.sql.DataSource;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgresUnionTileStore implements TileStore {

  private static Logger logger = LoggerFactory.getLogger(PostgresUnionTileStore.class);

  private static final String TILE_ENVELOPE = "st_tileenvelope(%1$s, %2$s, %3$s)";

  private static final String LAYER_QUERY =
      "" + "select " + "st_asmvt(layer, '%1$s', 4096, 'geom', 'id') " + "from (%2$s) as layer";

  private static final String SOURCE_QUERY =
      ""
          + "select "
          + "id, "
          + "(tags || hstore('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags, "
          + "st_asmvtgeom(geom, %2$s, 4096, 256, true) as geom "
          + "from (%1$s and st_intersects(geom, %2$s)) as source "
          + "";

  private static final String UNION = " union all ";

  private final DataSource datasource;

  private final List<PostgresQuery> queries;

  public PostgresUnionTileStore(DataSource datasource, List<PostgresQuery> queries) {
    this.datasource = datasource;
    this.queries = queries;
  }

  public byte[] read(Tile tile) throws TileStoreException {
    try (Connection connection = datasource.getConnection();
        Statement statement = connection.createStatement();
        ByteArrayOutputStream data = new ByteArrayOutputStream()) {
      String sql = unionQuery(tile);
      logger.debug("Executing query: {}", sql);

      int length = 0;
      GZIPOutputStream gzip = new GZIPOutputStream(data);
      ResultSet resultSet = statement.executeQuery(sql);
      while (resultSet.next()) {
        byte[] bytes = resultSet.getBytes(1);
        length += bytes.length;
        gzip.write(bytes);
      }
      gzip.close();

      if (length > 0) {
        return data.toByteArray();
      } else {
        return null;
      }
    } catch (SQLException | IOException e) {
      throw new TileStoreException(e);
    }
  }

  protected String unionQuery(Tile tile) {
    return queries.stream()
        .filter(query -> zoomFilter(tile, query))
        .collect(
            Collectors.groupingBy(PostgresQuery::getLayer, LinkedHashMap::new, Collectors.toList()))
        .entrySet()
        .stream()
        .map(entry -> layerQuery(entry.getKey(), entry.getValue(), tile))
        .collect(Collectors.joining(UNION));
  }

  protected String layerQuery(String layer, List<PostgresQuery> queries, Tile tile) {
    var sourceQueries =
        queries.stream().map(query -> sourceQuery(query, tile)).collect(Collectors.joining(UNION));
    return String.format(LAYER_QUERY, layer, sourceQueries);
  }

  protected String sourceQuery(PostgresQuery query, Tile tile) {
    var ast = query.getAst();
    setAlias(ast.getSelectItems().get(0), "id");
    setAlias(ast.getSelectItems().get(1), "tags");
    setAlias(ast.getSelectItems().get(2), "geom");
    return String.format(SOURCE_QUERY, ast, tileEnvelope(tile));
  }

  protected void setAlias(SelectItem item, String alias) {
    item.accept(
        new SelectItemVisitorAdapter() {
          @Override
          public void visit(SelectExpressionItem item) {
            item.setAlias(new Alias(alias));
          }
        });
  }

  protected boolean zoomFilter(Tile tile, PostgresQuery query) {
    return query.getMinzoom() <= tile.z() && tile.z() < query.getMaxzoom();
  }

  protected String tileEnvelope(Tile tile) {
    return String.format(TILE_ENVELOPE, tile.z(), tile.x(), tile.y());
  }

  public void write(Tile tile, byte[] bytes) {
    throw new UnsupportedOperationException("The postgis tile store is read only");
  }

  public void delete(Tile tile) {
    throw new UnsupportedOperationException("The postgis tile store is read only");
  }
}
