/*
 * Copyright 2018 Waldemar Kłaczyński.
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
package org.apache.common.faces.impl.state.cdi;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ParameterizedTypeImpl implements ParameterizedType {

    /**
     * Stores the owner type.
     */
    private final Type ownerType;

    /**
     * Stores the raw type.
     */
    private final Class<?> rawType;

    /**
     * Stores the actual type arguments.
     */
    private final Type[] actualTypeArguments;

    /**
     * Constructs an instance of ParameterizedType without an owner type
     *
     * @param rawType Type representing the class or interface that declares
     * this type.
     * @param actualTypeArguments Array of Types representing the actual type
     * arguments for this type
     */
    public ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments) {
        this(null, rawType, actualTypeArguments);
    }

    /**
     * Constructs an instance of ParameterizedType
     *
     * @param ownerType the Type representing the type that this type is
     * embedded in, if any. It can be null.
     * @param rawType the Type representing the class or interface that declares
     * this type.
     * @param actualTypeArguments Array of Types representing the actual type
     * arguments for this type
     */
    public ParameterizedTypeImpl(Type ownerType, Class<?> rawType,
            Type[] actualTypeArguments) {

        this.ownerType = ownerType;
        this.rawType = rawType;
        this.actualTypeArguments = actualTypeArguments;
    }

    /**
     * Get the owner type.
     *
     * @return the owner type.
     */
    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    /**
     * Get the raw type.
     *
     * @return the raw type.
     */
    @Override
    public Type getRawType() {
        return rawType;
    }

    /**
     * Get the actual type arguments.
     *
     * @return the actual type arguments.
     */
    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    /**
     * Equals method.
     *
     * @param other the object to compare against.
     * @return true if it is equals, false otherwise.
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof ParameterizedType
                ? equals((ParameterizedType) other) : false;
    }

    /**
     *
     * Tests if an other instance of ParameterizedType is "equal to" this
     * instance.
     *
     * @param other the other instance of ParameterizedType
     * @return true if instances equal, false otherwise.
     */
    public boolean equals(ParameterizedType other) {
        return this == other ? true
                : Objects.equals(getOwnerType(), other.getOwnerType())
                && Objects.equals(getRawType(), other.getRawType())
                && Arrays.equals(getActualTypeArguments(),
                        other.getActualTypeArguments());
    }

    /**
     * Hash code.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(getOwnerType())
                ^ Objects.hashCode(getRawType())
                ^ Arrays.hashCode(getActualTypeArguments());
    }
}
