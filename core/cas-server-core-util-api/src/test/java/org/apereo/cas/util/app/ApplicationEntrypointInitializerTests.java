package org.apereo.cas.util.app;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ApplicationEntrypointInitializerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Simple")
public class ApplicationEntrypointInitializerTests {
    @Test
    void verifyOperation() {
        ApplicationEntrypointInitializer.noOp().initialize(ArrayUtils.EMPTY_STRING_ARRAY);
        assertTrue(ApplicationEntrypointInitializer.noOp().getApplicationSources().isEmpty());
    }
}
