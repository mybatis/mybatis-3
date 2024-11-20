/*
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor.resultset;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;

/**
 * Represents an object that is still to be created once all nested results with collection values have been gathered
 *
 * @author Willie Scholtz
 */
final class PendingConstructorCreation {

  private final Class<?> resultType;
  private final List<Class<?>> constructorArgTypes;
  private final List<Object> constructorArgs;

  private final Map<Integer, PendingCreationMetaInfo> linkedCollectionMetaInfo;
  private final Map<PendingCreationKey, Collection<Object>> linkedCollectionsByKey;
  private final Map<PendingCreationKey, List<PendingConstructorCreation>> linkedCreationsByKey;

  PendingConstructorCreation(Class<?> resultType, List<Class<?>> types, List<Object> args) {
    // since all our keys are based on result map id, we know we will never go over args size
    final int maxSize = types.size();

    this.linkedCollectionMetaInfo = new HashMap<>(maxSize);
    this.linkedCollectionsByKey = new HashMap<>(maxSize);
    this.linkedCreationsByKey = new HashMap<>(maxSize);

    this.resultType = resultType;
    this.constructorArgTypes = types;
    this.constructorArgs = args;
  }

  @SuppressWarnings("unchecked")
  Collection<Object> initializeCollectionForResultMapping(ObjectFactory objectFactory, ResultMap resultMap,
      ResultMapping constructorMapping, Integer index) {
    final Class<?> parameterType = constructorMapping.getJavaType();
    if (!objectFactory.isCollection(parameterType)) {
      throw new ReflectionException(
          "Cannot add a collection result to non-collection based resultMapping: " + constructorMapping);
    }

    final PendingCreationKey creationKey = new PendingCreationKey(constructorMapping);
    return linkedCollectionsByKey.computeIfAbsent(creationKey, (k) -> {
      // this will allow us to verify the types of the collection before creating the final object
      linkedCollectionMetaInfo.put(index, new PendingCreationMetaInfo(resultMap.getType(), creationKey));

      // will be checked before we finally create the object) as we cannot reliably do that here
      return (Collection<Object>) objectFactory.create(parameterType);
    });
  }

  void linkCreation(ResultMapping constructorMapping, PendingConstructorCreation pcc) {
    final PendingCreationKey creationKey = new PendingCreationKey(constructorMapping);
    final List<PendingConstructorCreation> pendingConstructorCreations = linkedCreationsByKey
        .computeIfAbsent(creationKey, (k) -> new ArrayList<>());

    if (pendingConstructorCreations.contains(pcc)) {
      throw new ExecutorException("Cannot link inner constructor creation with same value, MyBatis internal error!");
    }

    pendingConstructorCreations.add(pcc);
  }

  void linkCollectionValue(ResultMapping constructorMapping, Object value) {
    // not necessary to add null results to the collection
    if (value == null) {
      return;
    }

    final PendingCreationKey creationKey = new PendingCreationKey(constructorMapping);
    if (!linkedCollectionsByKey.containsKey(creationKey)) {
      throw new ExecutorException("Cannot link collection value for key: " + constructorMapping
          + ", resultMap has not been seen/initialized yet! Mybatis internal error!");
    }

    linkedCollectionsByKey.get(creationKey).add(value);
  }

  /**
   * Verifies preconditions before we can actually create the result object, this is more of a sanity check to ensure
   * all the mappings are as we expect them to be.
   * <p>
   * And if anything went wrong, provide the user with more information as to what went wrong
   *
   * @param objectFactory
   *          the object factory
   */
  private void verifyCanCreate(ObjectFactory objectFactory) {
    // if a custom object factory was supplied, we cannot reasionably verify that creation will work
    // thus, we disable verification and leave it up to the end user.
    if (!DefaultObjectFactory.class.equals(objectFactory.getClass())) {
      return;
    }

    // before we create, we need to get the constructor to be used and verify our types match
    // since we added to the collection completely unchecked
    final Constructor<?> resolvedConstructor = resolveConstructor(resultType, constructorArgTypes);
    final Type[] genericParameterTypes = resolvedConstructor.getGenericParameterTypes();
    for (int i = 0; i < genericParameterTypes.length; i++) {
      if (!linkedCollectionMetaInfo.containsKey(i)) {
        continue;
      }

      final PendingCreationMetaInfo creationMetaInfo = linkedCollectionMetaInfo.get(i);
      final Class<?> resolvedItemType = checkResolvedItemType(creationMetaInfo, genericParameterTypes[i]);

      // ensure we have an empty collection if there are linked creations for this arg
      final PendingCreationKey pendingCreationKey = creationMetaInfo.getPendingCreationKey();
      if (linkedCreationsByKey.containsKey(pendingCreationKey)) {
        final Object emptyCollection = constructorArgs.get(i);
        if (emptyCollection == null || !objectFactory.isCollection(emptyCollection.getClass())) {
          throw new ExecutorException(
              "Expected empty collection for '" + resolvedItemType + "', MyBatis internal error!");
        }
      } else {
        final Object linkedCollection = constructorArgs.get(i);
        if (!linkedCollectionsByKey.containsKey(pendingCreationKey)) {
          throw new ExecutorException(
              "Expected linked collection for key '" + pendingCreationKey + "', not found! MyBatis internal error!");
        }

        // comparing memory locations here (we rely on that fact)
        if (linkedCollection != linkedCollectionsByKey.get(pendingCreationKey)) {
          throw new ExecutorException("Expected linked collection in creation to be the same as arg for resultMap '"
              + pendingCreationKey + "', not equal! MyBatis internal error!");
        }
      }
    }
  }

  private static <T> Constructor<T> resolveConstructor(Class<T> type, List<Class<?>> constructorArgTypes) {
    try {
      if (constructorArgTypes == null) {
        return type.getDeclaredConstructor();
      }

      return type.getDeclaredConstructor(constructorArgTypes.toArray(new Class[0]));
    } catch (Exception e) {
      String argTypes = Optional.ofNullable(constructorArgTypes).orElseGet(Collections::emptyList).stream()
          .map(Class::getSimpleName).collect(Collectors.joining(","));
      throw new ReflectionException(
          "Error resolving constructor for " + type + " with invalid types (" + argTypes + ") . Cause: " + e, e);
    }
  }

  private static Class<?> checkResolvedItemType(PendingCreationMetaInfo creationMetaInfo, Type genericParameterTypes) {
    final ParameterizedType genericParameterType = (ParameterizedType) genericParameterTypes;
    final Class<?> expectedType = (Class<?>) genericParameterType.getActualTypeArguments()[0];
    final Class<?> resolvedItemType = creationMetaInfo.getArgumentType();

    if (!expectedType.isAssignableFrom(resolvedItemType)) {
      throw new ReflectionException(
          "Expected type '" + resolvedItemType + "', while the actual type of the collection was '" + expectedType
              + "', ensure your resultMap matches the type of the collection you are trying to inject");
    }

    return resolvedItemType;
  }

  @Override
  public String toString() {
    return "PendingConstructorCreation(" + this.hashCode() + "){" + "resultType=" + resultType + '}';
  }

  /**
   * Recursively creates the final result of this creation.
   *
   * @param objectFactory
   *          the object factory
   * @param verifyCreate
   *          should we verify this object can be created, should only be needed once
   *
   * @return the new immutable result
   */
  Object create(ObjectFactory objectFactory, boolean verifyCreate) {
    if (verifyCreate) {
      verifyCanCreate(objectFactory);
    }

    final List<Object> newArguments = new ArrayList<>(constructorArgs.size());
    for (int i = 0; i < constructorArgs.size(); i++) {
      final PendingCreationMetaInfo creationMetaInfo = linkedCollectionMetaInfo.get(i);
      final Object existingArg = constructorArgs.get(i);

      if (creationMetaInfo == null) {
        // we are not aware of this argument wrt pending creations
        newArguments.add(existingArg);
        continue;
      }

      // time to finally build this collection
      final PendingCreationKey pendingCreationKey = creationMetaInfo.getPendingCreationKey();
      if (linkedCreationsByKey.containsKey(pendingCreationKey)) {
        @SuppressWarnings("unchecked")
        final Collection<Object> emptyCollection = (Collection<Object>) existingArg;
        final List<PendingConstructorCreation> linkedCreations = linkedCreationsByKey.get(pendingCreationKey);

        for (PendingConstructorCreation linkedCreation : linkedCreations) {
          emptyCollection.add(linkedCreation.create(objectFactory, verifyCreate));
        }

        newArguments.add(emptyCollection);
        continue;
      }

      // handle the base collection (it was built inline already)
      newArguments.add(existingArg);
    }

    return objectFactory.create(resultType, constructorArgTypes, newArguments);
  }
}