/**
 * Copyright 2009-2018 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

/**
 * @author Helly Guo
 * <p>
 * Created on 19-1-12 下午10:14
 */
@Fork(value = 5)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 30, time = 1)
@Threads(value = 4)
public class SkipTypeDetectionBenchmark {
    private static final DemoInvoker[] INVOKERS = new DemoInvoker[]{
            (demoObject, blackhole) -> blackhole.consume(demoObject.getVal0()),
            (demoObject, blackhole) -> blackhole.consume(demoObject.getVal1()),
            (demoObject, blackhole) -> blackhole.consume(demoObject.getVal2()),
            (demoObject, blackhole) -> blackhole.consume(demoObject.getVal3()),
            (demoObject, blackhole) -> blackhole.consume(demoObject.getVal4()),
            (demoObject, blackhole) -> blackhole.consume(demoObject.getVal5())
    };

    @Benchmark
    public void testIfElse(DemoObject demoObject, Blackhole blackhole) {
        DemoEnum demoEnum = demoObject.getDemoEnum();
        if (DemoEnum.AAA.equals(demoEnum)) {
            blackhole.consume(demoObject.getVal0());
        } else if (DemoEnum.BBB.equals(demoEnum)) {
            blackhole.consume(demoObject.getVal1());
        } else if (DemoEnum.CCC.equals(demoEnum)) {
            blackhole.consume(demoObject.getVal2());
        } else if (DemoEnum.DDD.equals(demoEnum)) {
            blackhole.consume(demoObject.getVal3());
        } else if (DemoEnum.EEE.equals(demoEnum)) {
            blackhole.consume(demoObject.getVal4());
        } else if (DemoEnum.FFF.equals(demoEnum)) {
            blackhole.consume(demoObject.getVal5());
        } else {
            throw new RuntimeException("should not hit");
        }
    }

    @Benchmark
    public void testSwitchCase(DemoObject demoObject, Blackhole blackhole) {
        DemoEnum demoEnum = demoObject.getDemoEnum();
        switch (demoEnum) {
            case AAA:
                blackhole.consume(demoObject.getVal0());
                break;
            case BBB:
                blackhole.consume(demoObject.getVal1());
                break;
            case CCC:
                blackhole.consume(demoObject.getVal2());
                break;
            case DDD:
                blackhole.consume(demoObject.getVal3());
                break;
            case EEE:
                blackhole.consume(demoObject.getVal4());
                break;
            case FFF:
                blackhole.consume(demoObject.getVal5());
                break;
            default:
                throw new RuntimeException("should not hit");
        }
    }

    @Benchmark
    public void testArray(DemoObject demoObject, Blackhole blackhole) {
        INVOKERS[demoObject.getDemoEnum().ordinal()].invoke(demoObject, blackhole);
    }

    @Benchmark
    public void testEnum(DemoObject demoObject, Blackhole blackhole) {
        demoObject.getDemoEnum().getInvoker().invoke(demoObject, blackhole);
    }

    @State(Scope.Benchmark)
    public static class DemoObject {
        @Param(value = {"AAA", "BBB", "CCC", "DDD", "EEE", "FFF"})
        public DemoEnum demoEnum;

        private int val0 = 220183018;
        private int val1 = 220183018;
        private int val2 = 220183018;
        private int val3 = 220183018;
        private int val4 = 220183018;
        private int val5 = 220183018;


        public DemoEnum getDemoEnum() {
            return demoEnum;
        }

        public int getVal0() {
            return val0;
        }

        public int getVal1() {
            return val1;
        }

        public int getVal2() {
            return val2;
        }

        public int getVal3() {
            return val3;
        }

        public int getVal4() {
            return val4;
        }

        public int getVal5() {
            return val5;
        }
    }

    public enum DemoEnum {
        AAA((demoObject, blackhole) -> {
            blackhole.consume(demoObject.getVal0());
        }),
        BBB((demoObject, blackhole) -> {
            blackhole.consume(demoObject.getVal1());
        }),
        CCC((demoObject, blackhole) -> {
            blackhole.consume(demoObject.getVal2());
        }),
        DDD((demoObject, blackhole) -> {
            blackhole.consume(demoObject.getVal3());
        }),
        EEE((demoObject, blackhole) -> {
            blackhole.consume(demoObject.getVal4());
        }),
        FFF((demoObject, blackhole) -> {
            blackhole.consume(demoObject.getVal5());
        });

        private DemoInvoker invoker;

        DemoEnum(DemoInvoker invoker) {
            this.invoker = invoker;
        }

        public DemoInvoker getInvoker() {
            return invoker;
        }
    }

    public interface DemoInvoker {
        void invoke(DemoObject demoObject, Blackhole blackhole);
    }
}
