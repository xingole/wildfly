/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.as.test.integration.weld.jta;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Status;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.Transactional;

@ApplicationScoped
public class CdiBean {

    @Resource
    TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    public boolean isTransactionSynchronizationRegistryInjected() {
        return transactionSynchronizationRegistry != null;
    }

    @Transactional
    public boolean isTransactionActive() {
        return transactionSynchronizationRegistry.getTransactionStatus() == Status.STATUS_ACTIVE;
    }

    public boolean isTransactionInactive() {
        return transactionSynchronizationRegistry.getTransactionStatus() == Status.STATUS_NO_TRANSACTION;
    }
}
