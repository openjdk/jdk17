/*
 * Copyright (c) 2020, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import jdk.incubator.vector.VectorShape;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorSpecies;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.Assert;

import java.util.function.IntFunction;
import java.util.List;

/**
 * @test
 * @modules jdk.incubator.vector
 * @modules java.base/jdk.internal.vm.annotation
 * @run testng/othervm  -XX:-TieredCompilation --add-opens jdk.incubator.vector/jdk.incubator.vector=ALL-UNNAMED
 * Vector512ConversionTests
 */

@Test
public class Vector512ConversionTests extends AbstractVectorConversionTest {

    static final VectorShape SHAPE = VectorShape.S_512_BIT;
    static final int BUFFER_SIZE = Integer.getInteger("jdk.incubator.vector.test.buffer-size", 1024);

    @DataProvider
    public Object[][] fixedShapeXfixedShape() {
        return fixedShapeXFixedShapeSpeciesArgs(SHAPE);
    }

    @DataProvider
    public Object[][] fixedShapeXShape() {
        return fixedShapeXShapeSpeciesArgs(SHAPE);
    }

    @DataProvider
    public Object[][] fixedShapeXSegmentedLegalCastSpecies() {
        return fixedShapeXSegmentedCastSpeciesArgs(SHAPE, true);
    }

    @DataProvider
    public Object[][] fixedShapeXSegmentedIllegalCastSpecies() {
        return fixedShapeXSegmentedCastSpeciesArgs(SHAPE, false);
    }

    @Test(dataProvider = "fixedShapeXfixedShape")
    static <I, O> void convert(VectorSpecies<I> src, VectorSpecies<O> dst, IntFunction<?> fa) {
        Object a = fa.apply(BUFFER_SIZE);
        conversion_kernel(src, dst, a, ConvAPI.CONVERT);
    }

    @Test(dataProvider = "fixedShapeXShape")
    static <I, O> void convertShape(VectorSpecies<I> src, VectorSpecies<O> dst, IntFunction<?> fa) {
        Object a = fa.apply(BUFFER_SIZE);
        conversion_kernel(src, dst, a, ConvAPI.CONVERTSHAPE);
    }

    @Test(dataProvider = "fixedShapeXShape")
    static <I, O> void castShape(VectorSpecies<I> src, VectorSpecies<O> dst, IntFunction<?> fa) {
        Object a = fa.apply(BUFFER_SIZE);
        conversion_kernel(src, dst, a, ConvAPI.CASTSHAPE);
    }

    @Test(dataProvider = "fixedShapeXShape")
    static <I, O> void reinterpret(VectorSpecies<I> src, VectorSpecies<O> dst, IntFunction<?> fa) {
        Object a = fa.apply(BUFFER_SIZE);
        reinterpret_kernel(src, dst, a);
    }

    @Test(dataProvider = "fixedShapeXSegmentedLegalCastSpecies")
    static void shuffleCast(VectorSpecies src, List<VectorSpecies> legal) {
        int [] arr = new int[src.length()];
        for(int i = 0; i < arr.length; i++) {
            arr[i] = i;
        }
        VectorShuffle shuffle = VectorShuffle.fromArray(src, arr, 0);

        for(var sps : legal) {
            VectorShuffle res = shuffle.cast(sps);
            Assert.assertEquals(res.toArray(), arr);
        }
    }

    @Test(dataProvider = "fixedShapeXSegmentedIllegalCastSpecies")
    static void shuffleCastNeg(VectorSpecies src, List<VectorSpecies> illegal) {
        int [] arr = new int[src.length()];
        for(int i = 0; i < arr.length; i++) {
            arr[i] = i;
        }
        VectorShuffle shuffle = VectorShuffle.fromArray(src, arr, 0);
        for(var sps : illegal) {
            try {
                shuffle.cast(sps);
                Assert.fail();
            } catch (IllegalArgumentException e) {
            }
        }
    }

    @Test(dataProvider = "fixedShapeXSegmentedLegalCastSpecies")
    static void maskCast(VectorSpecies src, List<VectorSpecies> legal) {
        long val = (1L << (src.length() & 63)) - 1L;
        VectorMask mask = VectorMask.fromLong(src, val);
        for(var sps : legal) {
            VectorMask res = mask.cast(sps);
            Assert.assertEquals(res.toLong(), val);
        }
    }

    @Test(dataProvider = "fixedShapeXSegmentedIllegalCastSpecies")
    static void maskCastNeg(VectorSpecies src, List<VectorSpecies> illegal) {
        long val = (1L << (src.length() & 63)) - 1L;
        VectorMask mask = VectorMask.fromLong(src, val);
        for(var sps : illegal) {
            try {
                mask.cast(sps);
                Assert.fail();
            } catch (IllegalArgumentException e) {
            }
        }
    }

}
