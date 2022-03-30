/*
 *    Copyright 2009-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.autoconstructor;

public class ExtensiveSubject {
    private final byte aByte;
    private final short aShort;
    private final char aChar;
    private final int anInt;
    private final long aLong;
    private final float aFloat;
    private final double aDouble;
    private final boolean aBoolean;
    private final String aString;

    // enum types
    private final TestEnum anEnum;

    // array types

    // string to lob types:
    private final String aClob;
    private final String aBlob;

    public ExtensiveSubject(final byte aByte,
                            final short aShort,
                            final char aChar,
                            final int anInt,
                            final long aLong,
                            final float aFloat,
                            final double aDouble,
                            final boolean aBoolean,
                            final String aString,
                            final TestEnum anEnum,
                            final String aClob,
                            final String aBlob) {
        this.aByte = aByte;
        this.aShort = aShort;
        this.aChar = aChar;
        this.anInt = anInt;
        this.aLong = aLong;
        this.aFloat = aFloat;
        this.aDouble = aDouble;
        this.aBoolean = aBoolean;
        this.aString = aString;
        this.anEnum = anEnum;
        this.aClob = aClob;
        this.aBlob = aBlob;
    }

    public enum TestEnum {
        AVALUE, BVALUE, CVALUE;
    }
}
