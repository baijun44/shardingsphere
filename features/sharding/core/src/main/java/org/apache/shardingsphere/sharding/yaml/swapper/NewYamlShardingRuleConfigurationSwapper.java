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

package org.apache.shardingsphere.sharding.yaml.swapper;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.metadata.converter.ShardingNodeConverter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

/**
 * TODO Rename to YamlShardingRuleConfigurationSwapper when metadata structure adjustment completed. #25485
 * New YAML sharding rule configuration swapper.
 */
public final class NewYamlShardingRuleConfigurationSwapper implements NewYamlRuleConfigurationSwapper<ShardingRuleConfiguration> {
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final ShardingRuleConfiguration data) {
        Collection<YamlDataNode> result = new LinkedHashSet<>();
        // TODO swap rule to YAML configuration before YamlEngine.marshal
        swapTableRules(data, result);
        swapStrategies(data, result);
        swapAlgorithms(data, result);
        if (null != data.getDefaultShardingColumn()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getDefaultShardingColumnPath(), YamlEngine.marshal(data.getDefaultShardingColumn())));
        }
        if (null != data.getShardingCache()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getShardingCachePath(), YamlEngine.marshal(data.getShardingCache())));
        }
        return result;
    }
    
    private void swapTableRules(final ShardingRuleConfiguration data, final Collection<YamlDataNode> result) {
        for (ShardingTableRuleConfiguration each : data.getTables()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getTableNamePath(each.getLogicTable()), YamlEngine.marshal(each)));
        }
        for (ShardingAutoTableRuleConfiguration each : data.getAutoTables()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getAutoTableNamePath(each.getLogicTable()), YamlEngine.marshal(each)));
        }
        for (ShardingTableReferenceRuleConfiguration each : data.getBindingTableGroups()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getBindingTableNamePath(each.getName()), YamlEngine.marshal(each)));
        }
        if (null != data.getBroadcastTables() && !data.getBroadcastTables().isEmpty()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getBroadcastTablesPath(), YamlEngine.marshal(data.getBroadcastTables())));
        }
    }
    
    private void swapStrategies(final ShardingRuleConfiguration data, final Collection<YamlDataNode> result) {
        if (null != data.getDefaultDatabaseShardingStrategy()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getDefaultDatabaseStrategyPath(), YamlEngine.marshal(data.getDefaultDatabaseShardingStrategy())));
        }
        if (null != data.getDefaultTableShardingStrategy()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getDefaultTableStrategyPath(), YamlEngine.marshal(data.getDefaultTableShardingStrategy())));
        }
        if (null != data.getDefaultKeyGenerateStrategy()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getDefaultKeyGenerateStrategyPath(), YamlEngine.marshal(data.getDefaultKeyGenerateStrategy())));
        }
        if (null != data.getDefaultAuditStrategy()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getDefaultAuditStrategyPath(), YamlEngine.marshal(data.getDefaultAuditStrategy())));
        }
    }
    
    private void swapAlgorithms(final ShardingRuleConfiguration data, final Collection<YamlDataNode> result) {
        for (Entry<String, AlgorithmConfiguration> each : data.getShardingAlgorithms().entrySet()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getShardingAlgorithmPath(each.getKey()), YamlEngine.marshal(each.getValue())));
        }
        for (Entry<String, AlgorithmConfiguration> each : data.getKeyGenerators().entrySet()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getKeyGeneratorPath(each.getKey()), YamlEngine.marshal(each.getValue())));
        }
        for (Entry<String, AlgorithmConfiguration> each : data.getAuditors().entrySet()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getAuditorPath(each.getKey()), YamlEngine.marshal(each.getValue())));
        }
    }
    
    @Override
    public ShardingRuleConfiguration swapToObject(final Collection<YamlDataNode> dataNodes) {
        return new ShardingRuleConfiguration();
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getTypeClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "SHARDING";
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
}