/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.arquillian.container.weld.ee.embedded_1_1.mock;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import org.jboss.weld.injection.spi.JpaInjectionServices;

public class MockJpaInjectionServices implements JpaInjectionServices
{
	private Map<String, EntityManager> entityManagersByUnitName = new HashMap<String, EntityManager>();
	private Map<String, EntityManagerFactory> entityManagerFactoriesByUnitName = new HashMap<String, EntityManagerFactory>();
   
   public EntityManager resolvePersistenceContext(InjectionPoint injectionPoint)
   {
	   String persistenceUnitName = getPersistenceUnitNameFromPersistenceContext(injectionPoint);
		return getEntityManagerByPersistenceUnitName(persistenceUnitName);
   }
   
   public EntityManagerFactory resolvePersistenceUnit(InjectionPoint injectionPoint)
   {
	   String persistenceUnitName = getPersistenceUnitNameFromPersistenceUnit(injectionPoint);
		return getEntityManagerFactoryByPersistenceUnitName(persistenceUnitName);
   }
   
   public void cleanup() {
	   closeAndClearEntityManagers();
	   closeAndClearEntityManagerFactories();
   }

   private String getPersistenceUnitNameFromPersistenceContext(InjectionPoint injectionPoint) {
	   String nameOnAnnotation = injectionPoint.getAnnotated().getAnnotation(PersistenceContext.class).unitName();
	   if (isEmpty(nameOnAnnotation)) {
		   return null;
	   } else {
		   return nameOnAnnotation;
	   }
   }

   private String getPersistenceUnitNameFromPersistenceUnit(InjectionPoint injectionPoint) {
	   String nameOnAnnotation = injectionPoint.getAnnotated().getAnnotation(PersistenceUnit.class).unitName();
	   if (isEmpty(nameOnAnnotation)) {
		   return null;
	   } else {
		   return nameOnAnnotation;
	   }
   }

	private EntityManager getEntityManagerByPersistenceUnitName(
			String persistenceUnitName) {
		if (entityManagerNotYetInitialised(persistenceUnitName)) {
			initialiseEntityManager(persistenceUnitName);
		}
	      return entityManagersByUnitName.get(persistenceUnitName);
	}

	private EntityManagerFactory getEntityManagerFactoryByPersistenceUnitName(
			String persistenceUnitName) {
		if (entityManagerFactoryNotYetInitialised(persistenceUnitName)) {
			initialiseEntityManagerFactory(persistenceUnitName);
		}
	      return entityManagerFactoriesByUnitName.get(persistenceUnitName);
	}

	private boolean entityManagerNotYetInitialised(String persistenceUnitName) {
		return !entityManagersByUnitName.containsKey(persistenceUnitName);
	}

	private boolean entityManagerFactoryNotYetInitialised(String persistenceUnitName) {
		return !entityManagerFactoriesByUnitName.containsKey(persistenceUnitName);
	}

	private void initialiseEntityManager(String persistenceUnitName) {
		EntityManagerFactory entityManagerFactory = getEntityManagerFactoryByPersistenceUnitName(persistenceUnitName);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManagersByUnitName.put(persistenceUnitName, entityManager);
	}

	private void initialiseEntityManagerFactory(String persistenceUnitName) {
		EntityManagerFactory entityManagerFactory = Persistence
				.createEntityManagerFactory(persistenceUnitName);
		entityManagerFactoriesByUnitName.put(persistenceUnitName, entityManagerFactory);
	}
	
	private boolean isEmpty(String nameOnAnnotation) {
		return nameOnAnnotation == null || nameOnAnnotation.equals("");
	}

	private void closeAndClearEntityManagerFactories() {
		for (String persistenceUnitName : entityManagerFactoriesByUnitName.keySet()) {
			   entityManagerFactoriesByUnitName.get(persistenceUnitName).close();
		   }
		entityManagerFactoriesByUnitName.clear();
	}
	
	private void closeAndClearEntityManagers() {
		for (String persistenceUnitName : entityManagersByUnitName.keySet()) {
			   entityManagersByUnitName.get(persistenceUnitName).close();
		   }
		   entityManagersByUnitName.clear();
	}

}
