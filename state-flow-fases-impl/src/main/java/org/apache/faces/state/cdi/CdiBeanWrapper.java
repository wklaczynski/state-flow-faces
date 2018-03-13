/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class CdiBeanWrapper implements Bean {
    
       private final Class beanClass;
       private InjectionTarget injectionTarget = null;
       
       public CdiBeanWrapper( Class beanClass) {
           this.beanClass = beanClass;
           
       }
       
       public void setInjectionTarget(InjectionTarget injectionTarget) {
           this.injectionTarget = injectionTarget;
       }
       
       @Override
       public Class<?> getBeanClass() {
           return beanClass;
       }
       
       @Override
       public Set<InjectionPoint> getInjectionPoints() {
           return injectionTarget.getInjectionPoints();
       }
       
       @Override
       public String getName() {
           return null;
       }
       
       @Override
       public Set<Annotation> getQualifiers() {
           Set<Annotation> qualifiers = new HashSet<>();
           qualifiers.add( new DefaultAnnotationLiteral());
           qualifiers.add( new AnyAnnotationLiteral());
           return qualifiers;
       }
       
       @Override
       public Class<? extends Annotation> getScope() {
           return Dependent.class;
       }
       
       @Override
       public Set<Class<? extends Annotation>> getStereotypes() {
           return Collections.emptySet();
       }
       
       @Override
       public Set<Type> getTypes() {
           Set<Type> types = new HashSet<>();
           types.add( beanClass );
           types.add( Object.class );
           return types;
       }
       
       @Override
       public boolean isAlternative() {
           return false;
       }
       
       @Override
       public boolean isNullable() {
           return false;
       }
       
       @Override
       public Object create( CreationalContext ctx ) {
           Object instance = injectionTarget.produce( ctx );
           injectionTarget.inject( instance, ctx );
           injectionTarget.postConstruct( instance );
           return instance;
       }
       
       @Override
       public void destroy( Object instance, CreationalContext ctx ) {
           injectionTarget.preDestroy( instance );
           injectionTarget.dispose( instance );
           ctx.release();
       }
   }   