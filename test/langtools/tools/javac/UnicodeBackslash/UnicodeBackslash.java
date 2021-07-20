/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @bug 8269150
 * @summary Unicode \ u 0 0 5 C not treated as an escaping backslash
 * @run main UnicodeBackslash
 */

public class UnicodeBackslash {
    static boolean failed = false;
    static int counter = 0;

    public static void main(String... args) {
        //   source                           expected
        test("\\]",                           "\\]");
        test("\u005C\]",                      "\\]");
        test("\\u005C]",                      "\\u005C]");
        test("\u005C\u005C]",                 "\\]");

        test("\\\\]",                         "\\\\]");
        test("\u005C\\\]",                    "\\\\]");
        test("\\u005C\\]",                    "\\u005C\\]");
        test("\u005C\u005C\\]",               "\\\\]");
        test("\\\u005C\]",                    "\\\\]");
        test("\u005C\\u005C\]",               "\\\\]");
        test("\\u005C\u005C\]",               "\\u005C\\]");
        test("\u005C\u005C\u005C\]",          "\\\\]");
        test("\\\\u005C]",                    "\\\\u005C]");
        test("\u005C\\\u005C]",               "\\\\u005C]");
        test("\\u005C\\u005C]",               "\\u005C\\u005C]");
        test("\u005C\u005C\\u005C]",          "\\\\u005C]");
        test("\\\u005C\u005C]",               "\\\\]");
        test("\u005C\\u005C\u005C]",          "\\\\]");
        test("\\u005C\u005C\u005C]",          "\\u005C\\]");
        test("\u005C\u005C\u005C\u005C]",     "\\\\]");

        if (failed) {
            throw new RuntimeException("Unicode escapes not handled correctly");
        }
    }

    static void test(String source, String expected) {
        counter++;
        if (!source.equals(expected)) {
            System.err.println(counter + ": expected: " +  expected + ", found: " + source);
            failed = true;
        }
    }
}
