/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.da.common.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(value = "global")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlobalConfig extends AbstractModuleGroup {

    private String cartographerUrl;
    private String indyUrl;

    public String getCartographerUrl() {
        return cartographerUrl;
    }

    public void setCartographerUrl(String cartographerUrl) {
        this.cartographerUrl = cartographerUrl;
    }

    public String getIndyUrl() {
        return indyUrl;
    }

    public void setIndyUrl(String indyUrl) {
        if (indyUrl.endsWith("/")) {
            this.indyUrl = indyUrl.substring(0, indyUrl.length() - 1);
        } else {
            this.indyUrl = indyUrl;
        }
    }

}
