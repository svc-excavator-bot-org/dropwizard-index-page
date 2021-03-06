/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.indexpage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests using configured path for {@link IndexPageBundle}.
 */
public final class IndexPageBundleTests {

    @Rule
    public final DropwizardAppRule<TestApp.TestConfiguration> rule =
            new DropwizardAppRule<TestApp.TestConfiguration>(TestApp.class,
                    TestApp.class.getClassLoader().getResource("example.yml").getPath());

    @Test(expected = NullPointerException.class)
    public void testNewWithInvalidMappings() {
        new IndexPageBundle(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewWithEmptyPath() {
        new IndexPageBundle("", ImmutableSet.<String>of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewWithNullPath() {
        new IndexPageBundle(null, ImmutableSet.<String>of());
    }

    @Test(expected = NullPointerException.class)
    public void testRunWithInvalidEnvironment() throws Exception {
        IndexPageBundle bundle = new IndexPageBundle(ImmutableSet.of("/views/*"));
        bundle.run(mock(IndexPageConfigurable.class), null);
    }

    @Test(expected = NullPointerException.class)
    public void testRunWithInvalidConfiguration() throws Exception {
        IndexPageBundle bundle = new IndexPageBundle(ImmutableSet.of("/views/*"));
        bundle.run(null, mock(Environment.class));
    }

    @Test
    public void testGetIndexPage() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(String.format("http://localhost:%d/", rule.getLocalPort()))
                .request()
                .get();
        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertTrue(response.readEntity(String.class).contains("<base href=\"/\">"));
    }

    @Test
    public void testGetIndexPageWithHome() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(String.format("http://localhost:%d/home", rule.getLocalPort()))
                .request()
                .get();
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void testGetIndexPageWithWrongPath() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(String.format("http://localhost:%d/wrongpath", rule.getLocalPort()))
                .request()
                .get();
        assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
    }
}
