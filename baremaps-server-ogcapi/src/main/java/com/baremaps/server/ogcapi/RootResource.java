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

package com.baremaps.server.ogcapi;

import com.baremaps.api.DefaultApi;
import com.baremaps.model.LandingPage;
import com.baremaps.model.Link;
import java.util.Arrays;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class RootResource implements DefaultApi {

  @Context UriInfo uriInfo;

  @Override
  public Response getLandingPage() {
    String address = uriInfo.getBaseUri().toString();
    LandingPage landingPage =
        new LandingPage()
            .title("Baremaps")
            .description("Baremaps OGC API Landing Page")
            .links(
                Arrays.asList(
                    new Link()
                        .title("This document (landing page)")
                        .href(address)
                        .type("application/json")
                        .rel("self"),
                    new Link()
                        .title("Conformance declaration")
                        .href(address + "conformance")
                        .type("application/json")
                        .rel("conformance"),
                    new Link()
                        .title("API description")
                        .href(address + "api")
                        .type("application/json")
                        .rel("service-desc"),
                    new Link()
                        .title("API description")
                        .href(address + "api")
                        .type("application/yaml")
                        .rel("service-desc"),
                    new Link()
                        .title("API documentation")
                        .href(address + "swagger")
                        .type("text/html")
                        .rel("service-doc")));
    return Response.ok().entity(landingPage).build();
  }
}
