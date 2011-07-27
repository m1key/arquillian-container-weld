/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.container.weld.ee.embedded_1_1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.weld.ee.embedded_1_1.entities.Dog;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JpaLifecycleEventExecuterNamedPersistenceUnitPersistenceContextTestCase {

	@PersistenceUnit
	private EntityManagerFactory entityManagerFactory;

	@PersistenceContext
	private EntityManager entityManager;

	@Deployment
	public static Archive<?> createTestArchive()
			throws IllegalArgumentException, IOException {
		return ShrinkWrap
				.create(JavaArchive.class)
				.addAsManifestResource(
						new File(
								"src/test/resources/META-INF/persistence-test.xml"),
						ArchivePaths.create("persistence.xml"))
				.addClasses(
						Dog.class,
						JpaLifecycleEventExecuterPersistenceUnitPersistenceContextTestCase.class);
	}

	@Test
	public void shouldInjectNonNullEntityManagerAndEntityManagerFactory() {
		assertNotNull(
				"Entity manager factory injected via @PersistenceUnit should not be null.",
				entityManagerFactory);
		assertNotNull(
				"Entity manager injected via @PersistenceContext should not be null.",
				entityManager);
	}

	@Test
	public void shouldPersistAndRetrieveEntityWithFirstEntityManagerFactory() {
		EntityManager entityManager = entityManagerFactory
				.createEntityManager();

		String dogName = "Sega";
		Dog dog = createDog(entityManager, dogName);

		Dog retrievedDog = (Dog) entityManager
				.createQuery("SELECT d FROM Dog d WHERE d.name = :name")
				.setParameter("name", dogName).getResultList().get(0);
		assertEquals(
				"Created and persisted dog should be the same as retrieved dog.",
				dog, retrievedDog);
	}

	@Test
	public void shouldPersistAndRetrieveEntityWithSecondEntityManager() {
		String dogName = "Figa";
		Dog dog = createDog(entityManager, dogName);

		Dog retrievedDog = (Dog) entityManager
				.createQuery("SELECT d FROM Dog d WHERE d.name = :name")
				.setParameter("name", dogName).getResultList().get(0);
		assertEquals(
				"Created and persisted dog should be the same as retrieved dog.",
				dog, retrievedDog);
	}

	@Test(expected = PersistenceException.class)
	public void shouldNotAllowForTwoIdenticalDogsInDifferentEntityManagerFactories() {
		EntityManager entityManager2 = entityManagerFactory
				.createEntityManager();

		String dogName = "Sega";
		createDog(entityManager, dogName);
		createDog(entityManager2, dogName);
	}

	@After
	public void cleanup() {
		EntityManager entityManager2 = entityManagerFactory
				.createEntityManager();

		entityManager.getTransaction().begin();
		entityManager.createQuery("DELETE FROM Dog").executeUpdate();
		entityManager.getTransaction().commit();

		entityManager2.getTransaction().begin();
		entityManager2.createQuery("DELETE FROM Dog").executeUpdate();
		entityManager2.getTransaction().commit();
	}

	private Dog createDog(EntityManager entityManager, String name) {
		entityManager.getTransaction().begin();
		Dog dog = new Dog(name);
		entityManager.persist(dog);
		entityManager.getTransaction().commit();
		return dog;
	}

}
