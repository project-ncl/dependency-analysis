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
package org.jboss.da.common.version;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class SuffixedVersionTest {

    @Test
    public void testSimpleVersions() {
        SuffixedVersion v1 = new SuffixedVersion(1, 2, 3, "", "1.2.3");
        SuffixedVersion v2 = new SuffixedVersion(1, 2, 3, "", "1.2.3");
        SuffixedVersion v3 = new SuffixedVersion(1, 2, 4, "", "1.2.4");
        SuffixedVersion v4 = new SuffixedVersion(2, 3, 4, "foobar", "2.3.4.foobar");
        Assert.assertEquals(1, v1.getMajor());
        Assert.assertEquals(2, v1.getMinor());
        Assert.assertEquals(3, v1.getMicro());
        Assert.assertEquals("", v1.getQualifier());
        Assert.assertEquals("foobar", v4.getQualifier());
        Assert.assertTrue(v1.equals(v2));
        Assert.assertFalse(v1.equals(v3));
        Assert.assertEquals("1.2.3", v1.toString());
        Assert.assertEquals("2.3.4.foobar", v4.toString());
    }

    @Test
    public void testSuffixedVersions() {
        SuffixedVersion v1 = new SuffixedVersion(1, 2, 3, "", "suffix", 1, "1.2.3.suffix-1");
        SuffixedVersion v2 = new SuffixedVersion(1, 2, 3, "", "suffix", 1, "1.2.3.suffix-1");
        SuffixedVersion v3a = new SuffixedVersion(1, 2, 3, "", "suffix", 2, "1.2.3.suffix-2");
        SuffixedVersion v3b = new SuffixedVersion(1, 2, 3, "", "xiffus", 1, "1.2.3.xiffus-1");
        SuffixedVersion v4 = new SuffixedVersion(2, 3, 4, "foobar", "suffix", 1, "2.3.4.foobar-suffix-1");
        Assert.assertEquals("suffix", v1.getSuffix().get());
        Assert.assertEquals(Integer.valueOf(1), v1.getSuffixVersion().get());
        Assert.assertEquals(Integer.valueOf(2), v3a.getSuffixVersion().get());
        Assert.assertEquals("", v1.getQualifier());
        Assert.assertTrue(v1.equals(v2));
        Assert.assertFalse(v1.equals(v3a));
        Assert.assertFalse(v1.equals(v3b));
        Assert.assertEquals("1.2.3.suffix-1", v1.toString());
        Assert.assertEquals("2.3.4.foobar-suffix-1", v4.toString());
    }
}
