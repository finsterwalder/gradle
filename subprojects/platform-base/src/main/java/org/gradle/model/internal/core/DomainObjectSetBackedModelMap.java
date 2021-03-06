/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.model.internal.core;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.gradle.api.*;
import org.gradle.api.specs.Spec;
import org.gradle.internal.Actions;
import org.gradle.internal.Cast;
import org.gradle.model.ModelMap;

import java.util.Set;

public class DomainObjectSetBackedModelMap<T> extends DomainObjectCollectionBackedModelMap<T, DomainObjectSet<T>> {

    public DomainObjectSetBackedModelMap(Class<T> elementClass, DomainObjectSet<T> backingSet, NamedEntityInstantiator<T> instantiator, Namer<Object> namer) {
        this(elementClass, backingSet, instantiator, namer, Actions.doNothing());
    }

    public DomainObjectSetBackedModelMap(Class<T> elementClass, DomainObjectSet<T> backingSet, NamedEntityInstantiator<T> instantiator, Namer<Object> namer,
                                         Action<? super T> onCreateAction) {
        super(elementClass, backingSet, instantiator, namer, onCreateAction);
    }

    @Override
    protected <S> ModelMap<S> toNonSubtypeMap(Class<S> type) {
        DomainObjectSet<S> cast = toNonSubtype(type);
        return new DomainObjectSetBackedModelMap<S>(type, cast, new NamedEntityInstantiator<S>() {
            @Override
            public <D extends S> D create(String name, Class<D> type) {
                throw new IllegalArgumentException(String.format("Cannot create an item of type %s as this is not a subtype of %s.", type, elementClass.toString()));
            }
        }, namer, Actions.doNothing());
    }

    private <S> DomainObjectSet<S> toNonSubtype(final Class<S> type) {
        DomainObjectSet<T> matching = backingCollection.matching(new Spec<T>() {
            @Override
            public boolean isSatisfiedBy(T element) {
                return type.isInstance(element);
            }
        });

        return Cast.uncheckedCast(matching);
    }

    protected <S extends T> ModelMap<S> toSubtypeMap(Class<S> itemSubtype) {
        NamedEntityInstantiator<S> instantiator = Cast.uncheckedCast(this.instantiator);
        return new DomainObjectSetBackedModelMap<S>(itemSubtype, backingCollection.withType(itemSubtype), instantiator, namer, onCreateAction);
    }

    @Nullable
    @Override
    public T get(String name) {
        return Iterables.find(backingCollection, new HasNamePredicate<T>(name, namer), null);
    }

    @Override
    public Set<String> keySet() {
        return Sets.newHashSet(Iterables.transform(backingCollection, new ToName<T>(namer)));
    }

    @Override
    public <S> void withType(Class<S> type, Action<? super S> configAction) {
        toNonSubtype(type).all(configAction);
    }

    private static class HasNamePredicate<T> implements Predicate<T> {
        private final String name;
        private final Namer<? super T> namer;

        public HasNamePredicate(String name, Namer<? super T> namer) {
            this.name = name;
            this.namer = namer;
        }

        @Override
        public boolean apply(@Nullable T input) {
            return namer.determineName(input).equals(name);
        }
    }

    private static class ToName<T> implements Function<T, String> {
        private final Namer<? super T> namer;

        public ToName(Namer<? super T> namer) {
            this.namer = namer;
        }

        @Override
        public String apply(@Nullable T input) {
            return namer.determineName(input);
        }
    }

    public static <T> DomainObjectSetBackedModelMap<T> ofNamed(Class<T> elementType, DomainObjectSet<T> domainObjectSet, NamedEntityInstantiator<T> instantiator) {
        return new DomainObjectSetBackedModelMap<T>(
            elementType,
            domainObjectSet,
            instantiator,
            new org.gradle.api.Namer<Object>() {
                public String determineName(Object object) {
                    return Cast.cast(Named.class, object).getName();
                }
            }
        );
    }
}
