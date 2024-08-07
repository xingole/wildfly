/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.test.manual.management;

import static org.jboss.as.controller.client.helpers.ClientConstants.RESPONSE_HEADERS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ALLOW_RESOURCE_SERVICE_RESTART;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_HEADERS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PROCESS_STATE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;
import static org.jboss.as.test.integration.management.util.ModelUtil.createOpNode;

import java.net.URL;
import jakarta.inject.Inject;

import org.jboss.as.test.integration.management.util.WebUtil;
import org.jboss.as.test.shared.TestSuiteEnvironment;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.core.testrunner.ServerControl;
import org.wildfly.core.testrunner.ServerController;
import org.wildfly.core.testrunner.WildFlyRunner;

/**
 * @author Dominik Pospisil <dpospisi@redhat.com>
 * @author Tomaz Cerar
 */
@RunWith(WildFlyRunner.class)
@ServerControl(manual = true)
public class ManagementOnlyModeTestCase {

    private static final int TEST_PORT = 20491;

    @Inject
    private ServerController container;

    @Test
    public void testManagementOnlyMode() throws Exception {
        // restart server to management-only mode
        container.startInAdminMode();

        // update the model in admin-only mode - add a web connector
        ModelNode op = createOpNode("socket-binding-group=standard-sockets/socket-binding=my-test-binding", ADD);
        op.get("interface").set("public");
        op.get("port").set(TEST_PORT);

        container.getClient().executeForResult(op);

        op = createOpNode("subsystem=undertow/server=default-server/http-listener=my-test", ADD);
        op.get("socket-binding").set("my-test-binding");
        container.getClient().executeForResult(op);
        //reload to normal mode
        container.reload();

        // check that the changes made in admin-only mode have been applied - test the connector
        Assert.assertTrue("Could not connect to created connector.", WebUtil.testHttpURL(new URL(
                "http", TestSuiteEnvironment.getHttpAddress(), TEST_PORT, "/").toString()));

        // remove the conector
        op = createOpNode("subsystem=undertow/server=default-server/http-listener=my-test", REMOVE);
        op.get(OPERATION_HEADERS).get(ALLOW_RESOURCE_SERVICE_RESTART).set(false);
        container.getClient().executeForResult(op);
        op = createOpNode("socket-binding-group=standard-sockets/socket-binding=my-test-binding", REMOVE);
        op.get(OPERATION_HEADERS).get(ALLOW_RESOURCE_SERVICE_RESTART).set(false);
        ModelNode result = container.getClient().getControllerClient().execute(op);

        //reload shouldn't be required by operations above, if it is, there is a problem
        if (result.hasDefined(RESPONSE_HEADERS) && result.get(RESPONSE_HEADERS).hasDefined(PROCESS_STATE)) {
            Assert.assertTrue("reload-required".equals(result.get(RESPONSE_HEADERS).get(PROCESS_STATE).asString()));
        }
    }

}
