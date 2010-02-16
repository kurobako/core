/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.resolution;

import static org.jboss.weld.logging.messages.BeanManagerMessage.DUPLICATE_QUALIFIERS;
import static org.jboss.weld.logging.messages.BeanManagerMessage.INVALID_QUALIFIER;
import static org.jboss.weld.logging.messages.ResolutionMessage.CANNOT_EXTRACT_RAW_TYPE;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.Container;
import org.jboss.weld.exceptions.ForbiddenArgumentException;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.reflection.Reflections;

public class ResolvableBuilder
{

   protected Class<?> rawType;
   protected final Set<Type> types;
   protected final Set<Annotation> qualifiers;
   protected final Map<Class<? extends Annotation>, Annotation> mappedQualifiers;
   protected Bean<?> declaringBean;

   public ResolvableBuilder()
   {
      this.types = new HashSet<Type>();
      this.qualifiers = new HashSet<Annotation>();
      this.mappedQualifiers = new HashMap<Class<? extends Annotation>, Annotation>();
   }

   public ResolvableBuilder setType(Type type)
   {
      if (rawType != null)
      {
         throw new IllegalStateException("Cannot change type once set");
      }
      if (type == null)
      {
         throw new IllegalArgumentException("type cannot be null");
      }
      this.rawType = Reflections.getRawType(type);
      if (rawType == null)
      {
         throw new ForbiddenArgumentException(CANNOT_EXTRACT_RAW_TYPE, type);
      }
      this.types.add(type);
      return this;
   }
   
   public ResolvableBuilder setInjectionPoint(InjectionPoint injectionPoint)
   {
      setType(injectionPoint.getType());
      addQualifiers(injectionPoint.getQualifiers());
      setDeclaringBean(injectionPoint.getBean());
      return this;
   }
   
   public ResolvableBuilder setDeclaringBean(Bean<?> declaringBean)
   {
      this.declaringBean = declaringBean;
      return this;
   }
   
   public ResolvableBuilder addType(Type type)
   {
      this.types.add(type);
      return this;
   }
   
   public ResolvableBuilder addTypes(Set<Type> types)
   {
      this.types.addAll(types);
      return this;
   }

   public Resolvable create()
   {
      if (qualifiers.size() == 0)
      {
         this.qualifiers.add(DefaultLiteral.INSTANCE);
      }
      return new ResolvableImpl(rawType, types, qualifiers, mappedQualifiers, declaringBean);
   }

   public ResolvableBuilder addQualifier(Annotation qualifier)
   {
      checkQualifier(qualifier);
      this.qualifiers.add(qualifier);
      this.mappedQualifiers.put(qualifier.annotationType(), qualifier);
      return this;
   }
   
   public ResolvableBuilder addQualifierIfAbsent(Annotation qualifier)
   {
      if (!qualifiers.contains(qualifier))
      {
         addQualifier(qualifier);
      }
      return this;
   }

   public ResolvableBuilder addQualifiers(Annotation[] qualifiers)
   {
      for (Annotation qualifier : qualifiers)
      {
         addQualifier(qualifier);
      }
      return this;
   }

   public ResolvableBuilder addQualifiers(Set<Annotation> qualifiers)
   {
      for (Annotation qualifier : qualifiers)
      {
         addQualifier(qualifier);
      }
      return this;
   }

   protected void checkQualifier(Annotation qualifier)
   {
      if (!Container.instance().services().get(MetaAnnotationStore.class).getBindingTypeModel(qualifier.annotationType()).isValid())
      {
         throw new ForbiddenArgumentException(INVALID_QUALIFIER, qualifier);
      }
      if (qualifiers.contains(qualifier))
      {
         throw new ForbiddenArgumentException(DUPLICATE_QUALIFIERS, Arrays.asList(qualifiers));
      }
   }

   protected static class ResolvableImpl implements Resolvable
   {

      private final Set<Annotation> qualifiers;
      private final Map<Class<? extends Annotation>, Annotation> mappedQualifiers;
      private final Set<Type> typeClosure;
      private final Class<?> rawType;
      private final Bean<?> declaringBean;

      protected ResolvableImpl(Class<?> rawType, Set<Type> typeClosure, Set<Annotation> qualifiers, Map<Class<? extends Annotation>, Annotation> mappedQualifiers, Bean<?> declaringBean)
      {
         this.qualifiers = qualifiers;
         this.mappedQualifiers = mappedQualifiers;
         this.typeClosure = typeClosure;
         this.rawType = rawType;
         this.declaringBean = declaringBean;
      }

      public Set<Annotation> getQualifiers()
      {
         return qualifiers;
      }

      public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
      {
         return mappedQualifiers.containsKey(annotationType);
      }

      public Set<Type> getTypes()
      {
         return typeClosure;
      }

      public boolean isAssignableTo(Class<?> clazz)
      {
         return Reflections.isAssignableFrom(clazz, typeClosure);
      }

      public <A extends Annotation> A getAnnotation(Class<A> annotationType)
      {
         return (A) mappedQualifiers.get(annotationType);
      }

      public Class<?> getJavaClass()
      {
         return rawType;
      }

      public Bean<?> getDeclaringBean()
      {
         return declaringBean;
      }

      @Override
      public String toString()
      {
         return "Types: " + getTypes() + "; Bindings: " + getQualifiers();
      }

   }


}
