/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.emc.pravega.common.util;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is similar to CountDownLatch and Semaphore. Many threads can await() the the call of
 * release() (blocking until it is invoked.) After this all calls to await() will not block until
 * reset is called.
 */
public class ReusableLatch {

    private final Semaphore impl;
    private final AtomicBoolean released;
    private final Object releasingLock = new Object();

    public ReusableLatch() {
        this(false);
    }

    public ReusableLatch(boolean startReleased) {
        released = new AtomicBoolean(startReleased);
        if (startReleased) {
            impl = new Semaphore(Integer.MAX_VALUE);
        } else {
            impl = new Semaphore(0);
        }
    }

    /**
     * Block until another thread calls release, or the thread is interrupted
     */
    public void await() throws InterruptedException {
        if (released.get()) {
            return;
        }
        impl.acquire();
    }

    /**
     * Block until release is called by another thread.
     */
    public void awaitUninterruptibly() {
        if (released.get()) {
            return;
        }
        impl.acquireUninterruptibly();
    }

    /**
     * Allow all waiting threads to go through, and all future threads to proceed without blocking.
     */
    public void release() {
        if (released.compareAndSet(false, true)) {
            synchronized (releasingLock) {
                if (released.get()) {
                    impl.release(Integer.MAX_VALUE);
                }
            }
        }
    }

    /**
     * Returns whether or not release has been called and threads can call await without blocking.
     */
    public boolean isReleased() {
        return released.get();
    }

    /**
     * Resets the latch to an un-release state.
     */
    public void reset() {
        if (released.compareAndSet(true, false)) {
            synchronized (releasingLock) {
                if (!released.get()) {
                    impl.drainPermits();
                }
            }
        }
    }

}