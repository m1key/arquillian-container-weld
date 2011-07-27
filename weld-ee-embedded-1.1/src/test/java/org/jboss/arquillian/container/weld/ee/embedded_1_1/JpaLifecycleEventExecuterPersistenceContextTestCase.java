package org.jboss.arquillian.container.weld.ee.embedded_1_1;

import static org.junit.Assert.assertEquals;
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

/**
 * 
 * @author Michal Huniewicz
 * 
 */
@RunWith(Arquillian.class)
public class JpaLifecycleEventExecuterPersistenceContextTestCase {

	@PersistenceContext
	private EntityManager entityManager;

	@PersistenceContext
	private EntityManager entityManager2;

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
						JpaLifecycleEventExecuterPersistenceContextTestCase.class);
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
	public void twoEntityManagersWithNoPersistenceUnitNameShouldBeTheSame() {
		assertEquals(
				"Two entity managers with no persistence unit name specified"
						+ " should be the same.", entityManager, entityManager2);
	}

	@Test
	public void shouldPersistAndRetrieveEntityWithFirstEntityManager() {
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
		Dog dog = createDog(entityManager2, dogName);

		Dog retrievedDog = (Dog) entityManager2
				.createQuery("SELECT d FROM Dog d WHERE d.name = :name")
				.setParameter("name", dogName).getResultList().get(0);
		assertEquals(
				"Created and persisted dog should be the same as retrieved dog.",
				dog, retrievedDog);
	}

	@Test(expected = PersistenceException.class)
	public void shouldNotAllowForDuplicateRowInSameEntityManager() {
		String dogName = "Sega";
		createDog(entityManager, dogName);
		createDog(entityManager2, dogName);
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
		entityManager.getTransaction().begin();
		Dog dog = new Dog(name);
		entityManager.persist(dog);
		entityManager.getTransaction().commit();
		return dog;
	}
}
