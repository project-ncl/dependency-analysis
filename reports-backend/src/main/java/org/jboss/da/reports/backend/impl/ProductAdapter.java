/*
 * Copyright 2018 Honza Brázdil &lt;jbrazdil@redhat.com&gt;.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.da.reports.backend.impl;

import org.jboss.da.listings.api.dao.ProductDAO;
import org.jboss.da.listings.api.dao.ProductVersionDAO;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.service.ProductVersionService;
import org.jboss.da.products.api.Product;
import org.jboss.da.products.api.ProductProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This bean is used to translate products identified by the DB identifiers to abstract product
 * objects used by Product Providers.
 *
 * @see Product
 * @see ProductProvider
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class ProductAdapter {

    @Inject
    private ProductDAO productDao;

    @Inject
    private ProductVersionDAO productVersionDao;

    @Inject
    private ProductVersionService productVersionService;

    public Set<Product> toProducts(Set<String> productNames, Set<Long> productVersionIds) {
        Set<ProductVersion> productVersions = new HashSet<>();
        StringBuilder errorMsg = new StringBuilder();

        if (productNames != null && !productNames.isEmpty()) {
            List<org.jboss.da.listings.api.model.Product> productsByName = productDao.findAllWithNames(new ArrayList<>(productNames));
            if (productNames.size() == productsByName.size()) {
                for (String productName : productNames) {
                    List<ProductVersion> prodVers = productVersionService.getAllForProduct(productName);
                    productVersions.addAll(prodVers);
                }
            } else {
                // Error
                Set<String> unexistingProductNames = new HashSet<>(productNames);
                productsByName.stream().forEach(x -> unexistingProductNames.remove(x.getName()));
                errorMsg.append("Product names do not exist: ");
                errorMsg.append(joinMissing(unexistingProductNames));
            }
        }

        if (productVersionIds != null && !productVersionIds.isEmpty()) {
            List<ProductVersion> prodVersionsById = productVersionDao.findAllWithIds(new ArrayList<>(productVersionIds));
            if (productVersionIds.size() == prodVersionsById.size()) {
                productVersions.addAll(prodVersionsById);
            } else {
                // Error
                Set<Long> unexistingProductVersionIds = new HashSet<>(productVersionIds);
                prodVersionsById.stream().forEach(x -> unexistingProductVersionIds.remove(x.getId()));
                errorMsg.append("Product Versions do not exist: ");
                errorMsg.append(joinMissing(unexistingProductVersionIds));
            }
        }

        if (errorMsg.length() > 0) {
            throw new IllegalArgumentException(errorMsg.toString());
        }

        return productVersions.stream()
                .map(ProductAdapter::toProduct)
                .collect(Collectors.toSet());
    }

    private <T> String joinMissing(Collection<T> invalidItems) {
        return invalidItems.stream()
                .map(Object::toString)
                .collect(Collectors.joining(",", "[", "];"));
    }

    private static Product toProduct(ProductVersion pv) {
        return new Product(pv.getProduct().getName(), pv.getProductVersion());
    }
}
