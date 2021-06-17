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
 * @bug 8266459 8268349
 * @summary check various warnings
 * @library /test/lib
 */

import jdk.test.lib.process.OutputAnalyzer;
import jdk.test.lib.process.ProcessTools;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.Permission;

public class SecurityManagerWarnings {
    public static void main(String args[]) throws Exception {
        if (args.length == 0) {
            run(null)
                    .shouldHaveExitValue(0)
                    .shouldContain("SM is enabled: false")
                    .shouldNotContain("A command line option has enabled the Security Manager")
                    .shouldContain("System::setSecurityManager will be removed in a future release");

            run("allow")
                    .shouldHaveExitValue(0)
                    .shouldContain("SM is enabled: false")
                    .shouldNotContain("A command line option has enabled the Security Manager")
                    .shouldContain("System::setSecurityManager will be removed in a future release");

            run("disallow")
                    .shouldNotHaveExitValue(0)
                    .shouldContain("SM is enabled: false")
                    .shouldNotContain("A command line option has enabled the Security Manager")
                    .shouldContain("UnsupportedOperationException");

            run("SecurityManagerWarnings$MySM")
                    .shouldHaveExitValue(0)
                    .shouldContain("SM is enabled: true")
                    .shouldContain("A command line option has enabled the Security Manager")
                    .shouldContain("System::setSecurityManager will be removed in a future release");

            // Default SecurityManager does not allow setSecurityManager

            run("")
                    .shouldNotHaveExitValue(0)
                    .shouldContain("SM is enabled: true")
                    .shouldContain("A command line option has enabled the Security Manager")
                    .shouldContain("AccessControlException");

            run("default")
                    .shouldNotHaveExitValue(0)
                    .shouldContain("SM is enabled: true")
                    .shouldContain("A command line option has enabled the Security Manager")
                    .shouldContain("AccessControlException");
        } else {
            System.out.println("SM is enabled: " + (System.getSecurityManager() != null));
            PrintStream oldErr = System.err;
            // Modify System.err, make sure warnings are printed to the
            // original System.err and will not be swallowed.
            System.setErr(new PrintStream(new ByteArrayOutputStream()));
            Exception ex = null;
            try {
                System.setSecurityManager(new MySM());
            } catch (Exception e) {
                ex = e;
            } finally {
                System.setErr(oldErr);
            }
            // Revert System.err to make sure the exception is
            // printed to the original System.err.
            if (ex != null) {
                throw ex;
            }
        }
    }

    static OutputAnalyzer run(String prop) throws Exception {
        if (prop == null) {
            return ProcessTools.executeTestJvm(
                    "SecurityManagerWarnings", "run");
        } else {
            return ProcessTools.executeTestJvm(
                    "-Djava.security.manager=" + prop,
                    "SecurityManagerWarnings", "run");
        }
    }

    // This SecurityManager allows everything!
    public static class MySM extends SecurityManager {
        @Override
        public void checkPermission(Permission perm) {
        }
    }
}
