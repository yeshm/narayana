/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: CurrentTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.hammer;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.ORBInfo;
import com.arjuna.orbportability.RootOA;
import io.narayana.perf.PerformanceProfileStore;
import io.narayana.perf.Result;

public class PerfHammer
{
    private static int getArg(String[] args, int index, int defaultValue) {
        try {
            if (index >= 0 && index < args.length)
                return Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            throw new NullPointerException(new PerfHammer().getClass().getName() + "test arguments in the PerformanceProfileStore invalid: " + e.getMessage());
        }

        return defaultValue;

    }

    public static void main(String[] args) throws Exception
    {
        String gridReference = args[0];
        int numberOfCalls = 1000;
        int threadCount = 10;
        int batchSize = 100;

        ORB myORB = ORB.getInstance("test");
        RootOA myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);

        String metricName = "JTSRemote_PerfTest_PerfHammer_" + myORB.orb().getClass().getName();
        boolean failOnRegression = PerformanceProfileStore.isFailOnRegression();

        if (failOnRegression) {
            String[] xargs = PerformanceProfileStore.getTestArgs(metricName);

            numberOfCalls = getArg(xargs, 0, numberOfCalls);
            threadCount = getArg(xargs, 1, threadCount);
            batchSize = getArg(xargs, 2, batchSize);
        }

        GridWorker worker = new GridWorker(myORB, gridReference);
        Result opts = new Result(threadCount, numberOfCalls, batchSize).measure(worker);

        boolean correct = PerformanceProfileStore.checkPerformance(
                metricName, PerformanceProfileStore.getVariance(metricName), opts.getThroughput(), true);

        System.out.printf("Test performance (for orb type %s): %d calls/sec (%d invocations using %d threads with %d errors. Total time %d ms)%n",
                ORBInfo.getOrbName(), opts.getThroughput(), opts.getNumberOfCalls(), opts.getThreadCount(),
                opts.getErrorCount(), opts.getTotalMillis());

        System.out.printf("%s%n", (correct ? "Passed" : "Failed"));
    }
}
