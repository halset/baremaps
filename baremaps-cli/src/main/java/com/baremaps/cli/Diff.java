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

package com.baremaps.cli;

import com.baremaps.blob.Blob;
import com.baremaps.blob.BlobStore;
import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.postgres.PostgresCoordinateCache;
import com.baremaps.osm.postgres.PostgresHeaderRepository;
import com.baremaps.osm.postgres.PostgresNodeRepository;
import com.baremaps.osm.postgres.PostgresReferenceCache;
import com.baremaps.osm.postgres.PostgresRelationRepository;
import com.baremaps.osm.postgres.PostgresWayRepository;
import com.baremaps.osm.repository.DiffService;
import com.baremaps.osm.repository.HeaderRepository;
import com.baremaps.osm.repository.Repository;
import com.baremaps.postgres.jdbc.PostgresUtils;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "diff", description = "List the tiles affected by changes (experimental).")
public class Diff implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Diff.class);

  @Mixin private Options options;

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of the database.",
      required = true)
  private String database;

  @Option(
      names = {"--tiles"},
      paramLabel = "TILES",
      description = "The tiles affected by the update.",
      required = true)
  private URI tiles;

  @Option(
      names = {"--zoom"},
      paramLabel = "ZOOM",
      description = "The zoom level at which to compute the diff.")
  private int zoom = 12;

  @Option(
      names = {"--srid"},
      paramLabel = "SRID",
      description = "The projection used by the database.")
  private int srid = 3857;

  @Override
  public Integer call() throws Exception {
    BlobStore blobStore = options.blobStore();
    DataSource datasource = PostgresUtils.datasource(database);
    Cache<Long, Coordinate> coordinateCache = new PostgresCoordinateCache(datasource);
    Cache<Long, List<Long>> referenceCache = new PostgresReferenceCache(datasource);
    HeaderRepository headerRepository = new PostgresHeaderRepository(datasource);
    Repository<Long, Node> nodeRepository = new PostgresNodeRepository(datasource);
    Repository<Long, Way> wayRepository = new PostgresWayRepository(datasource);
    Repository<Long, Relation> relationRepository = new PostgresRelationRepository(datasource);

    logger.info("Saving diff");
    Path tmpTiles = Files.createFile(Paths.get("diff.tmp"));
    try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(tmpTiles))) {
      new DiffService(
              blobStore,
              coordinateCache,
              referenceCache,
              headerRepository,
              nodeRepository,
              wayRepository,
              relationRepository,
              srid,
              zoom)
          .call();
    }
    blobStore.put(
        this.tiles,
        Blob.builder()
            .withContentLength(Files.size(tmpTiles))
            .withInputStream(Files.newInputStream(tmpTiles))
            .build());
    Files.deleteIfExists(tmpTiles);

    logger.info("Done");

    return 0;
  }
}
