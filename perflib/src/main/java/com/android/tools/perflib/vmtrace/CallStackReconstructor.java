/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.tools.perflib.vmtrace;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * {@link CallStackReconstructor} helps in reconstructing per thread call stacks from a sequence of
 * trace events (method entry/exit events).
 */
public class CallStackReconstructor {
    /** List of calls currently assumed to be at stack depth 0 (called from the top level) */
    private List<Call.Builder> mTopLevelCalls = new ArrayList<Call.Builder>();

    /** Current call stack based on the sequence of received trace events. */
    private Stack<Call.Builder> mCallStack = new Stack<Call.Builder>();

    private List<Call> mTopLevelCallees;

    public void addTraceAction(long methodId, TraceAction action, int threadTime, int globalTime) {
        if (action == TraceAction.METHOD_ENTER) {
            enterMethod(methodId, threadTime, globalTime);
        } else {
            exitMethod(methodId, threadTime, globalTime);
        }
    }

    private void enterMethod(long methodId, int threadTime, int globalTime) {
        Call.Builder cb = new Call.Builder(methodId);
        cb.setMethodEntryTime(threadTime, globalTime);

        if (mCallStack.isEmpty()) {
            mTopLevelCalls.add(cb);
        } else {
            Call.Builder caller = mCallStack.peek();
            caller.addCallee(cb);
        }

        mCallStack.push(cb);
    }

    private void exitMethod(long methodId, int threadTime, int globalTime) {
        if (!mCallStack.isEmpty()) {
            Call.Builder c = mCallStack.pop();
            if (c.getMethodId() != methodId) {
                String msg = String
                        .format("Error during call stack reconstruction. Attempt to exit from method 0x%1$x while in method 0x%2$x",
                                c.getMethodId(), methodId);
                throw new RuntimeException(msg);
            }

            c.setMethodExitTime(threadTime, globalTime);
        } else {
            // We are exiting out of a method that was entered into before tracing was started.
            // In such a case, create this method
            Call.Builder c = new Call.Builder(methodId);
            c.setMethodExitTime(threadTime, globalTime);

            // All the previous calls at the top level are now assumed to have been called from
            // this method. So mark this method as having called all of those methods, and reset
            // the top level to only include this method
            for (Call.Builder cb : mTopLevelCalls) {
                c.addCallee(cb);
            }
            mTopLevelCalls.clear();
            mTopLevelCalls.add(c);
        }
    }

    private void fixupCallStacks() {
        if (mTopLevelCallees != null) {
            return;
        }

        // TODO: use global / thread times to infer context switches

        // Build calls from their respective builders
        List<Call> topLevelCallees = new ArrayList<Call>(mTopLevelCalls.size());
        for (Call.Builder b : mTopLevelCalls) {
            b.setStackDepth(0);
            topLevelCallees.add(b.build());
        }

        mTopLevelCallees = new ImmutableList.Builder<Call>().addAll(topLevelCallees).build();
    }

    public List<Call> getTopLevelCallees() {
        fixupCallStacks();
        return mTopLevelCallees;
    }
}
