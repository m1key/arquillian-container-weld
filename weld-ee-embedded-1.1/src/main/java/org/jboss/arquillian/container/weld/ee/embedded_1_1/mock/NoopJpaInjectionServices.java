package org.jboss.arquillian.container.weld.ee.embedded_1_1.mock;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jboss.weld.injection.spi.JpaInjectionServices;

public class NoopJpaInjectionServices implements JpaInjectionServices {

	@Override
	public void cleanup() {
	}

	@Override
	public EntityManager resolvePersistenceContext(InjectionPoint injectionPoint) {
		return null;
	}

	@Override
	public EntityManagerFactory resolvePersistenceUnit(
			InjectionPoint injectionPoint) {
		return null;
	}

}
