package org.apereo.cas.support.spnego.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 3.1
 */
@Tag("Spnego")
class SpnegoCredentialsToPrincipalResolverTests {
    private SpnegoPrincipalResolver resolver;

    private SpnegoCredential spnegoCredentials;

    @Mock
    private ServicesManager servicesManager;

    @Mock
    private AttributeDefinitionStore attributeDefinitionStore;

    @BeforeEach
    public void initialize() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        val context = PrincipalResolutionContext.builder()
            .servicesManager(servicesManager)
            .attributeDefinitionStore(attributeDefinitionStore)
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        this.resolver = new SpnegoPrincipalResolver(context);
        this.spnegoCredentials = new SpnegoCredential(new byte[]{0, 1, 2});
    }

    @Test
    void verifyValidCredentials() {
        this.spnegoCredentials.setPrincipal(PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("test"));
        assertEquals("test", this.resolver.resolve(this.spnegoCredentials,
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService())).getId());
    }

    @Test
    void verifySupports() {
        assertFalse(this.resolver.supports(null));
        assertTrue(this.resolver.supports(this.spnegoCredentials));
        assertFalse(this.resolver.supports(new UsernamePasswordCredential()));
    }
}
