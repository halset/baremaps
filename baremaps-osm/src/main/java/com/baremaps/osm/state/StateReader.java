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

package com.baremaps.osm.state;

import com.baremaps.osm.domain.State;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/** A reader that extract state information from an OpenStreetMap state file. */
public class StateReader {

  private static final DateTimeFormatter format =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

  private final InputStreamReader reader;

  /**
   * Constructs the state reader.
   *
   * @param inputStream the OpenStreetMap state file
   */
  public StateReader(InputStream inputStream) {
    this.reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
  }

  /**
   * Returns the state information.
   *
   * @return the state information
   * @throws IOException
   */
  public State read() throws IOException {
    Map<String, String> map = new HashMap<>();
    for (String line : CharStreams.readLines(reader)) {
      String[] array = line.split("=");
      if (array.length == 2) {
        map.put(array[0], array[1]);
      }
    }
    long sequenceNumber = Long.parseLong(map.get("sequenceNumber"));
    LocalDateTime timestamp = LocalDateTime.parse(map.get("timestamp").replace("\\", ""), format);
    return new State(sequenceNumber, timestamp);
  }
}
