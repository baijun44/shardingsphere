/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sharding.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.RuleConfigurationSubscribeCoordinator;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.event.table.auto.AddShardingAutoTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.auto.AlterShardingAutoTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.auto.DeleteShardingAutoTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.binding.AddShardingTableReferenceConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.binding.AlterShardingTableReferenceConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.binding.DeleteShardingTableReferenceConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.broadcast.AddBroadcastTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.broadcast.AlterBroadcastTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.broadcast.DeleteBroadcastTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.sharding.AddShardingTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.sharding.AlterShardingTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.sharding.DeleteShardingTableConfigurationEvent;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Map;
import java.util.Optional;

/**
 * Sharding table configuration subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
public final class ShardingTableConfigurationSubscriber implements RuleConfigurationSubscribeCoordinator {
    
    private Map<String, ShardingSphereDatabase> databases;
    
    private InstanceContext instanceContext;
    
    @Override
    public void registerRuleConfigurationSubscriber(final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext) {
        this.databases = databases;
        this.instanceContext = instanceContext;
        instanceContext.getEventBusContext().register(this);
    }
    
    /**
     * Renew with add sharding table configuration.
     *
     * @param event add sharding table configuration event
     */
    @Subscribe
    public synchronized void renew(final AddShardingTableConfigurationEvent<ShardingTableRuleConfiguration> event) {
        if (event.getVersion() < instanceContext.getModeContextManager().getActiveVersionByKey(event.getVersionKey())) {
            return;
        }
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingTableRuleConfiguration needToAddedConfig = event.getConfig();
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        ShardingRuleConfiguration config;
        if (rule.isPresent()) {
            config = (ShardingRuleConfiguration) rule.get().getConfiguration();
            config.getTables().removeIf(each -> each.getLogicTable().equals(needToAddedConfig.getLogicTable()));
            config.getTables().add(needToAddedConfig);
        } else {
            config = new ShardingRuleConfiguration();
            config.getTables().add(needToAddedConfig);
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with add sharding auto table configuration.
     *
     * @param event add sharding auto table configuration event
     */
    @Subscribe
    public synchronized void renew(final AddShardingAutoTableConfigurationEvent<ShardingAutoTableRuleConfiguration> event) {
        if (event.getVersion() < instanceContext.getModeContextManager().getActiveVersionByKey(event.getVersionKey())) {
            return;
        }
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingAutoTableRuleConfiguration needToAddedConfig = event.getConfig();
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        ShardingRuleConfiguration config;
        if (rule.isPresent()) {
            config = (ShardingRuleConfiguration) rule.get().getConfiguration();
            config.getAutoTables().removeIf(each -> each.getLogicTable().equals(needToAddedConfig.getLogicTable()));
            config.getAutoTables().add(needToAddedConfig);
        } else {
            config = new ShardingRuleConfiguration();
            config.getAutoTables().add(needToAddedConfig);
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with add sharding auto table configuration.
     *
     * @param event add sharding auto table configuration event
     */
    @Subscribe
    public synchronized void renew(final AddShardingTableReferenceConfigurationEvent<ShardingTableReferenceRuleConfiguration> event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingTableReferenceRuleConfiguration needToAddedConfig = event.getConfig();
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        ShardingRuleConfiguration config;
        if (rule.isPresent()) {
            config = (ShardingRuleConfiguration) rule.get().getConfiguration();
            config.getBindingTableGroups().removeIf(each -> each.getName().equals(needToAddedConfig.getName()));
            config.getBindingTableGroups().add(needToAddedConfig);
        } else {
            config = new ShardingRuleConfiguration();
            config.getBindingTableGroups().add(needToAddedConfig);
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with add broadcast table configuration.
     *
     * @param event add broadcast table configuration event
     */
    @Subscribe
    public synchronized void renew(final AddBroadcastTableConfigurationEvent event) {
        if (event.getVersion() < instanceContext.getModeContextManager().getActiveVersionByKey(event.getVersionKey())) {
            return;
        }
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        ShardingRuleConfiguration config;
        if (rule.isPresent()) {
            config = (ShardingRuleConfiguration) rule.get().getConfiguration();
        } else {
            config = new ShardingRuleConfiguration();
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter sharding table configuration.
     *
     * @param event alter sharding table configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterShardingTableConfigurationEvent<ShardingTableRuleConfiguration> event) {
        if (event.getVersion() < instanceContext.getModeContextManager().getActiveVersionByKey(event.getVersionKey())) {
            return;
        }
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingTableRuleConfiguration needToAlteredConfig = event.getConfig();
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getTables().removeIf(each -> each.getLogicTable().equals(event.getTableName()));
        config.getTables().add(needToAlteredConfig);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter sharding auto table configuration.
     *
     * @param event alter sharding auto table configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterShardingAutoTableConfigurationEvent<ShardingAutoTableRuleConfiguration> event) {
        if (event.getVersion() < instanceContext.getModeContextManager().getActiveVersionByKey(event.getVersionKey())) {
            return;
        }
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingAutoTableRuleConfiguration needToAlteredConfig = event.getConfig();
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getAutoTables().removeIf(each -> each.getLogicTable().equals(event.getTableName()));
        config.getAutoTables().add(needToAlteredConfig);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter sharding table reference configuration.
     *
     * @param event alter sharding table reference configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterShardingTableReferenceConfigurationEvent<ShardingTableReferenceRuleConfiguration> event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingTableReferenceRuleConfiguration needToAlteredConfig = event.getConfig();
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getBindingTableGroups().removeIf(each -> each.getName().equals(event.getTableName()));
        config.getBindingTableGroups().add(needToAlteredConfig);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter broadcast table configuration.
     *
     * @param event alter broadcast table configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterBroadcastTableConfigurationEvent event) {
        if (event.getVersion() < instanceContext.getModeContextManager().getActiveVersionByKey(event.getVersionKey())) {
            return;
        }
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete sharding table configuration.
     *
     * @param event delete sharding table configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteShardingTableConfigurationEvent event) {
        if (event.getVersion() < instanceContext.getModeContextManager().getActiveVersionByKey(event.getVersionKey())) {
            return;
        }
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getTables().removeIf(each -> each.getLogicTable().equals(event.getTableName()));
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete sharding auto table configuration.
     *
     * @param event delete sharding auto table configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteShardingAutoTableConfigurationEvent event) {
        if (event.getVersion() < instanceContext.getModeContextManager().getActiveVersionByKey(event.getVersionKey())) {
            return;
        }
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getAutoTables().removeIf(each -> each.getLogicTable().equals(event.getTableName()));
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete sharding table reference configuration.
     *
     * @param event delete sharding table reference configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteShardingTableReferenceConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getBindingTableGroups().removeIf(each -> each.getName().equals(event.getTableName()));
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete broadcast table configuration.
     *
     * @param event delete broadcast table configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteBroadcastTableConfigurationEvent event) {
        if (event.getVersion() < instanceContext.getModeContextManager().getActiveVersionByKey(event.getVersionKey())) {
            return;
        }
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
}