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

package com.baremaps.osm.postgres;

import static com.baremaps.testing.TestConstants.DATABASE_URL;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.baremaps.blob.BlobStoreException;
import com.baremaps.blob.ResourceBlobStore;
import com.baremaps.osm.OpenStreetMap;
import com.baremaps.osm.repository.RepositoryException;
import com.baremaps.osm.repository.SaveBlockConsumer;
import com.baremaps.postgres.jdbc.PostgresUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class SaveBlockConsumerTest {

  public DataSource dataSource;
  public PostgresHeaderRepository headerRepository;
  public PostgresNodeRepository nodeRepository;
  public PostgresWayRepository tableRepository;
  public PostgresRelationRepository relationRepository;

  @BeforeEach
  void init() throws SQLException, IOException {
    dataSource = PostgresUtils.datasource(DATABASE_URL, 1);
    headerRepository = new PostgresHeaderRepository(dataSource);
    nodeRepository = new PostgresNodeRepository(dataSource);
    tableRepository = new PostgresWayRepository(dataSource);
    relationRepository = new PostgresRelationRepository(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      PostgresUtils.executeResource(connection, "osm_create_extensions.sql");
      PostgresUtils.executeResource(connection, "osm_drop_tables.sql");
      PostgresUtils.executeResource(connection, "osm_create_tables.sql");
    }
  }

  @Test
  @Tag("integration")
  void test() throws BlobStoreException, RepositoryException, URISyntaxException {
    // Import data
    SaveBlockConsumer dataImporter =
        new SaveBlockConsumer(
            headerRepository, nodeRepository, tableRepository, relationRepository);
    InputStream inputStream =
        new ResourceBlobStore().get(new URI("res://simple/data.osm.pbf")).getInputStream();
    OpenStreetMap.streamPbfBlocks(inputStream).forEach(dataImporter);

    // Check node importation
    assertNull(nodeRepository.get(0l));
    assertNotNull(nodeRepository.get(1l));
    assertNotNull(nodeRepository.get(2l));
    assertNotNull(nodeRepository.get(3l));
    assertNull(nodeRepository.get(4l));

    // Check way importation
    assertNull(tableRepository.get(0l));
    assertNotNull(tableRepository.get(1l));
    assertNull(tableRepository.get(2l));

    // Check relation importation
    assertNull(relationRepository.get(0l));
    assertNotNull(relationRepository.get(1l));
    assertNull(relationRepository.get(2l));
  }
}
