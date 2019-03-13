/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.craft.core.thrift;


import java.util.Objects;

public class TField {

    public TField() {
        this("", TType.STOP, (short) 0);
    }

    public TField(String name, TType type, short sequence) {
        this.name = name;
        this.type = type;
        this.sequence = sequence;
    }

    public final String name;
    public final TType type;
    public final short sequence;

    @Override
    public String toString() {
        return "<TField name:'" + name + "' type:" + type + " sequence:" + sequence + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TField field = (TField) o;
        return sequence == field.sequence &&
                Objects.equals(name, field.name) &&
                type == field.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, sequence);
    }
}
