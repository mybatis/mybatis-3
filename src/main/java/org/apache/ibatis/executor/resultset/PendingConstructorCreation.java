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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.reflection.ReflectionException;
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

    return linkedCollectionsByKey.computeIfAbsent(new PendingCreationKey(constructorMapping), k -> {
      // this will allow us to verify the types of the collection before creating the final object
      linkedCollectionMetaInfo.put(index, new PendingCreationMetaInfo(resultMap.getType(), k));

      // will be checked before we finally create the object) as we cannot reliably do that here
      return (Collection<Object>) objectFactory.create(parameterType);
    });
  }

  void linkCreation(ResultMapping constructorMapping, PendingConstructorCreation pcc) {
    final PendingCreationKey creationKey = new PendingCreationKey(constructorMapping);
    final List<PendingConstructorCreation> pendingConstructorCreations = linkedCreationsByKey
        .computeIfAbsent(creationKey, k -> new ArrayList<>());

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

    linkedCollectionsByKey.computeIfAbsent(new PendingCreationKey(constructorMapping), k -> {
      throw new ExecutorException("Cannot link collection value for key: " + constructorMapping
          + ", resultMap has not been seen/initialized yet! Mybatis internal error!");
    }).add(value);
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
   *
   * @return the new immutable result
   */
  Object create(ObjectFactory objectFactory) {
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
      final List<PendingConstructorCreation> linkedCreations = linkedCreationsByKey.get(pendingCreationKey);
      if (linkedCreations != null) {
        @SuppressWarnings("unchecked")
        final Collection<Object> emptyCollection = (Collection<Object>) existingArg;

        for (PendingConstructorCreation linkedCreation : linkedCreations) {
          emptyCollection.add(linkedCreation.create(objectFactory));
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
