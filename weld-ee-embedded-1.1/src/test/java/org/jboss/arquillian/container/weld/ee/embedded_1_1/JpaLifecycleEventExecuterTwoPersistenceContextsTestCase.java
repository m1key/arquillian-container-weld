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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

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
public class JpaLifecycleEventExecuterTwoPersistenceContextsTestCase {

	@PersistenceContext(unitName = "testPu")
	private EntityManager entityManager;

	@PersistenceContext(unitName = "testPu2")
	private EntityManager entityManager2;

	@Deployment
	public static Archive<?> createTestArchive()
			throws IllegalArgumentException, IOException {
		return ShrinkWrap
				.create(JavaArchive.class)
				.addAsManifestResource(
						new File(
								"src/test/resources/META-INF/persistence-test-2-units.xml"),
						ArchivePaths.create("persistence.xml"))
				.addClasses(
						Dog.class,
						JpaLifecycleEventExecuterTwoPersistenceContextsTestCase.class);
	}

	@Test
	public void shouldInjectNonNullEntityManager() {
		assertNotNull(
				"Entity manager injected via @PersistenceContext should not be null.",
				entityManager);
		assertNotNull(
				"Second entity manager injected via @PersistenceContext should not be null.",
				entityManager2);
	}

	@Test
	public void twoEntityManagersWithDifferentPersistenceUnitNamesShouldNotBeTheSame() {
		assertFalse(
				"Two entity managers with different persistence unit names specified"
						+ " should not be the same.",
				entityManager.equals(entityManager2));
	}

	@Test
	public void shouldPersistAndRetrieveEntityWithFirstEntityManager() {
		String dogName = "Sega";
		Dog dog = createDog(entityManager, dogName);

		Dog retrievedDog = (Dog) entityManager
				.createQuery("FROM Dog where name = :name")
				.setParameter("name", dogName).getResultList().get(0);
		assertEquals(
				"Created and persisted dog should be the same as retrieved dog.",
				dog, retrievedDog);
	}

	@Test
	public void shouldPersistAndRetrieveEntityWithSecondEntityManager() {
		String dogName = "Figa";
		Dog dog = createDog(entityManager2, dogName);

		Dog retrievedDog = (Dog) entityManager2
				.createQuery("FROM Dog where name = :name")
				.setParameter("name", dogName).getResultList().get(0);
		assertEquals(
				"Created and persisted dog should be the same as retrieved dog.",
				dog, retrievedDog);
	}

	@Test
	public void shouldAllowToCreateTwoIdenticalDogsInDifferentEntityManagers() {
		createDog(entityManager, "Sega");
		createDog(entityManager2, "Sega");
	}

	@After
	public void cleanup() {
		entityManager.getTransaction().begin();
		entityManager.createQuery("DELETE FROM Dog").executeUpdate();
		entityManager.getTransaction().commit();

		entityManager2.getTransaction().begin();
		entityManager2.createQuery("DELETE FROM Dog").executeUpdate();
		entityManager2.getTransaction().commit();
	}

	private Dog createDog(EntityManager entityManager, String name) {
		try {
			entityManager.getTransaction().begin();
			Dog dog = new Dog(name);
			entityManager.persist(dog);
			entityManager.getTransaction().commit();
			return dog;
		} catch (PersistenceException e) {
			entityManager.getTransaction().rollback();
			throw e;
		}
	}

}
