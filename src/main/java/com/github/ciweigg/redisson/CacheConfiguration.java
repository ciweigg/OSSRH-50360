package com.github.ciweigg.redisson;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import com.github.ciweigg.properties.RedissonMultipleServerConfig;
import com.github.ciweigg.properties.RedissonProperties;
import com.github.ciweigg.properties.RedissonSingleServerConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.config.*;
import org.redisson.connection.balancer.LoadBalancer;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(RedissonProperties.class)
public class CacheConfiguration {

	@Autowired
	RedissonProperties redissonProperties;

	@Configuration
	@ConditionalOnClass({Redisson.class})
	@ConditionalOnExpression("'${redisson.mode}'=='single' or '${redisson.mode}'=='cluster' or '${redisson.mode}'=='sentinel' or '${redisson.mode}'=='masterslave' or '${redisson.mode}'=='replicated'")
	protected class RedissonSingleClientConfiguration {

		/**
		 * 单机模式 redisson 客户端
		 */
		@Bean
		@ConditionalOnProperty(name = "redisson.mode", havingValue = "single")
		RedissonClient redissonSingle() {
			Config config = initConfigs();
			SingleServerConfig singleServerConfig = config.useSingleServer();
			RedissonSingleServerConfig param = redissonProperties.getSingleServerConfig();
			singleServerConfig.setAddress(prefixAddress(param.getAddress()));
			singleServerConfig.setConnectionMinimumIdleSize(param.getConnectionMinimumIdleSize());
			singleServerConfig.setConnectionPoolSize(param.getConnectionPoolSize());
			singleServerConfig.setDatabase(param.getDatabase());
			singleServerConfig.setDnsMonitoringInterval(param.getDnsMonitoringInterval());
			singleServerConfig.setSubscriptionConnectionMinimumIdleSize(param.getSubscriptionConnectionMinimumIdleSize());
			singleServerConfig.setSubscriptionConnectionPoolSize(param.getSubscriptionConnectionPoolSize());
			singleServerConfig.setPingTimeout(redissonProperties.getPingTimeout());
			singleServerConfig.setClientName(redissonProperties.getClientName());
			singleServerConfig.setConnectTimeout(redissonProperties.getConnectTimeout());
			singleServerConfig.setIdleConnectionTimeout(redissonProperties.getIdleConnectionTimeout());
			singleServerConfig.setKeepAlive(redissonProperties.getKeepAlive());
			singleServerConfig.setPassword(redissonProperties.getPassword());
			singleServerConfig.setPingConnectionInterval(redissonProperties.getPingConnectionInterval());
			singleServerConfig.setRetryAttempts(redissonProperties.getRetryAttempts());
			singleServerConfig.setRetryInterval(redissonProperties.getRetryInterval());
			singleServerConfig.setSslEnableEndpointIdentification(redissonProperties.getSslEnableEndpointIdentification());
			singleServerConfig.setSslKeystore(redissonProperties.getSslKeystore());
			singleServerConfig.setSslKeystorePassword(redissonProperties.getSslKeystorePassword());
			singleServerConfig.setSslProvider(redissonProperties.getSslProvider());
			singleServerConfig.setSslTruststore(redissonProperties.getSslTruststore());
			singleServerConfig.setSslTruststorePassword(redissonProperties.getSslTruststorePassword());
			singleServerConfig.setSubscriptionsPerConnection(redissonProperties.getSubscriptionsPerConnection());
			singleServerConfig.setTcpNoDelay(redissonProperties.getTcpNoDelay());
			singleServerConfig.setTimeout(redissonProperties.getTimeout());
			return Redisson.create(config);
		}

		/**
		 * 集群模式的 redisson 客户端
		 *
		 * @return
		 */
		@Bean
		@ConditionalOnProperty(name = "redisson.mode", havingValue = "cluster")
		RedissonClient redissonCluster() {
			Config config = initConfigs();
			RedissonMultipleServerConfig multipleServerConfig = redissonProperties.getMultipleServerConfig();
			ClusterServersConfig clusterServersConfig = config.useClusterServers();
			clusterServersConfig.setScanInterval(multipleServerConfig.getScanInterval());
			clusterServersConfig.setSlaveConnectionMinimumIdleSize(multipleServerConfig.getSlaveConnectionMinimumIdleSize());
			clusterServersConfig.setSlaveConnectionPoolSize(multipleServerConfig.getSlaveConnectionPoolSize());
			clusterServersConfig.setFailedSlaveReconnectionInterval(multipleServerConfig.getFailedSlaveReconnectionInterval());
			clusterServersConfig.setFailedSlaveCheckInterval(multipleServerConfig.getFailedSlaveCheckInterval());
			clusterServersConfig.setMasterConnectionMinimumIdleSize(multipleServerConfig.getMasterConnectionMinimumIdleSize());
			clusterServersConfig.setMasterConnectionPoolSize(multipleServerConfig.getMasterConnectionPoolSize());
			clusterServersConfig.setReadMode(multipleServerConfig.getReadMode());
			clusterServersConfig.setSubscriptionMode(multipleServerConfig.getSubscriptionMode());
			clusterServersConfig.setSubscriptionConnectionMinimumIdleSize(multipleServerConfig.getSubscriptionConnectionMinimumIdleSize());
			clusterServersConfig.setSubscriptionConnectionPoolSize(multipleServerConfig.getSubscriptionConnectionPoolSize());
			clusterServersConfig.setDnsMonitoringInterval(multipleServerConfig.getDnsMonitoringInterval());
			try {
				clusterServersConfig.setLoadBalancer((LoadBalancer) Class.forName(multipleServerConfig.getLoadBalancer()).newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			for (String nodeAddress : multipleServerConfig.getNodeAddresses()) {
				clusterServersConfig.addNodeAddress(prefixAddress(nodeAddress));
			}
			clusterServersConfig.setPingTimeout(redissonProperties.getPingTimeout());
			clusterServersConfig.setClientName(redissonProperties.getClientName());
			clusterServersConfig.setConnectTimeout(redissonProperties.getConnectTimeout());
			clusterServersConfig.setIdleConnectionTimeout(redissonProperties.getIdleConnectionTimeout());
			clusterServersConfig.setKeepAlive(redissonProperties.getKeepAlive());
			clusterServersConfig.setPassword(redissonProperties.getPassword());
			clusterServersConfig.setPingConnectionInterval(redissonProperties.getPingConnectionInterval());
			clusterServersConfig.setRetryAttempts(redissonProperties.getRetryAttempts());
			clusterServersConfig.setRetryInterval(redissonProperties.getRetryInterval());
			clusterServersConfig.setSslEnableEndpointIdentification(redissonProperties.getSslEnableEndpointIdentification());
			clusterServersConfig.setSslKeystore(redissonProperties.getSslKeystore());
			clusterServersConfig.setSslKeystorePassword(redissonProperties.getSslKeystorePassword());
			clusterServersConfig.setSslProvider(redissonProperties.getSslProvider());
			clusterServersConfig.setSslTruststore(redissonProperties.getSslTruststore());
			clusterServersConfig.setSslTruststorePassword(redissonProperties.getSslTruststorePassword());
			clusterServersConfig.setSubscriptionsPerConnection(redissonProperties.getSubscriptionsPerConnection());
			clusterServersConfig.setTcpNoDelay(redissonProperties.getTcpNoDelay());
			clusterServersConfig.setTimeout(redissonProperties.getTimeout());
			return Redisson.create(config);
		}

		/**
		 * 哨兵模式 redisson 客户端
		 * @return
		 */
		@Bean
		@ConditionalOnProperty(name = "redisson.mode", havingValue = "sentinel")
		RedissonClient redissonSentinel() {
			Config config = initConfigs();
			RedissonMultipleServerConfig multipleServerConfig = redissonProperties.getMultipleServerConfig();
			SentinelServersConfig sentinelServersConfig = config.useSentinelServers();
			sentinelServersConfig.setDatabase(multipleServerConfig.getDatabase());
			sentinelServersConfig.setMasterName(multipleServerConfig.getMasterName());
			sentinelServersConfig.setScanInterval(multipleServerConfig.getScanInterval());
			sentinelServersConfig.setSlaveConnectionMinimumIdleSize(multipleServerConfig.getSlaveConnectionMinimumIdleSize());
			sentinelServersConfig.setSlaveConnectionPoolSize(multipleServerConfig.getSlaveConnectionPoolSize());
			sentinelServersConfig.setFailedSlaveReconnectionInterval(multipleServerConfig.getFailedSlaveReconnectionInterval());
			sentinelServersConfig.setFailedSlaveCheckInterval(multipleServerConfig.getFailedSlaveCheckInterval());
			sentinelServersConfig.setMasterConnectionMinimumIdleSize(multipleServerConfig.getMasterConnectionMinimumIdleSize());
			sentinelServersConfig.setMasterConnectionPoolSize(multipleServerConfig.getMasterConnectionPoolSize());
			sentinelServersConfig.setReadMode(multipleServerConfig.getReadMode());
			sentinelServersConfig.setSubscriptionMode(multipleServerConfig.getSubscriptionMode());
			sentinelServersConfig.setSubscriptionConnectionMinimumIdleSize(multipleServerConfig.getSubscriptionConnectionMinimumIdleSize());
			sentinelServersConfig.setSubscriptionConnectionPoolSize(multipleServerConfig.getSubscriptionConnectionPoolSize());
			sentinelServersConfig.setDnsMonitoringInterval(multipleServerConfig.getDnsMonitoringInterval());
			try {
				sentinelServersConfig.setLoadBalancer((LoadBalancer) Class.forName(multipleServerConfig.getLoadBalancer()).newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			for (String nodeAddress : multipleServerConfig.getNodeAddresses()) {
				sentinelServersConfig.addSentinelAddress(prefixAddress(nodeAddress));
			}
			sentinelServersConfig.setPingTimeout(redissonProperties.getPingTimeout());
			sentinelServersConfig.setClientName(redissonProperties.getClientName());
			sentinelServersConfig.setConnectTimeout(redissonProperties.getConnectTimeout());
			sentinelServersConfig.setIdleConnectionTimeout(redissonProperties.getIdleConnectionTimeout());
			sentinelServersConfig.setKeepAlive(redissonProperties.getKeepAlive());
			sentinelServersConfig.setPassword(redissonProperties.getPassword());
			sentinelServersConfig.setPingConnectionInterval(redissonProperties.getPingConnectionInterval());
			sentinelServersConfig.setRetryAttempts(redissonProperties.getRetryAttempts());
			sentinelServersConfig.setRetryInterval(redissonProperties.getRetryInterval());
			sentinelServersConfig.setSslEnableEndpointIdentification(redissonProperties.getSslEnableEndpointIdentification());
			sentinelServersConfig.setSslKeystore(redissonProperties.getSslKeystore());
			sentinelServersConfig.setSslKeystorePassword(redissonProperties.getSslKeystorePassword());
			sentinelServersConfig.setSslProvider(redissonProperties.getSslProvider());
			sentinelServersConfig.setSslTruststore(redissonProperties.getSslTruststore());
			sentinelServersConfig.setSslTruststorePassword(redissonProperties.getSslTruststorePassword());
			sentinelServersConfig.setSubscriptionsPerConnection(redissonProperties.getSubscriptionsPerConnection());
			sentinelServersConfig.setTcpNoDelay(redissonProperties.getTcpNoDelay());
			sentinelServersConfig.setTimeout(redissonProperties.getTimeout());
			return Redisson.create(config);
		}
	}

	/**
	 * 主从模式 redisson 客户端
	 * @return
	 */
	@Bean
	@ConditionalOnProperty(name = "redisson.mode", havingValue = "masterslave")
	RedissonClient redissonMasterSlave() {
		Config config = initConfigs();
		RedissonMultipleServerConfig multipleServerConfig = redissonProperties.getMultipleServerConfig();
		MasterSlaveServersConfig masterSlaveServersConfig = config.useMasterSlaveServers();
		masterSlaveServersConfig.setDatabase(multipleServerConfig.getDatabase());
		masterSlaveServersConfig.setSlaveConnectionMinimumIdleSize(multipleServerConfig.getSlaveConnectionMinimumIdleSize());
		masterSlaveServersConfig.setSlaveConnectionPoolSize(multipleServerConfig.getSlaveConnectionPoolSize());
		masterSlaveServersConfig.setFailedSlaveReconnectionInterval(multipleServerConfig.getFailedSlaveReconnectionInterval());
		masterSlaveServersConfig.setFailedSlaveCheckInterval(multipleServerConfig.getFailedSlaveCheckInterval());
		masterSlaveServersConfig.setMasterConnectionMinimumIdleSize(multipleServerConfig.getMasterConnectionMinimumIdleSize());
		masterSlaveServersConfig.setMasterConnectionPoolSize(multipleServerConfig.getMasterConnectionPoolSize());
		masterSlaveServersConfig.setReadMode(multipleServerConfig.getReadMode());
		masterSlaveServersConfig.setSubscriptionMode(multipleServerConfig.getSubscriptionMode());
		masterSlaveServersConfig.setSubscriptionConnectionMinimumIdleSize(multipleServerConfig.getSubscriptionConnectionMinimumIdleSize());
		masterSlaveServersConfig.setSubscriptionConnectionPoolSize(multipleServerConfig.getSubscriptionConnectionPoolSize());
		masterSlaveServersConfig.setDnsMonitoringInterval(multipleServerConfig.getDnsMonitoringInterval());
		try {
			masterSlaveServersConfig.setLoadBalancer((LoadBalancer) Class.forName(multipleServerConfig.getLoadBalancer()).newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		int index=0;
		for (String nodeAddress : multipleServerConfig.getNodeAddresses()) {
			if(index++==0){
				masterSlaveServersConfig.setMasterAddress(prefixAddress(nodeAddress));
			}else{
				masterSlaveServersConfig.addSlaveAddress(prefixAddress(nodeAddress));
			}
		}
		masterSlaveServersConfig.setPingTimeout(redissonProperties.getPingTimeout());
		masterSlaveServersConfig.setClientName(redissonProperties.getClientName());
		masterSlaveServersConfig.setConnectTimeout(redissonProperties.getConnectTimeout());
		masterSlaveServersConfig.setIdleConnectionTimeout(redissonProperties.getIdleConnectionTimeout());
		masterSlaveServersConfig.setKeepAlive(redissonProperties.getKeepAlive());
		masterSlaveServersConfig.setPassword(redissonProperties.getPassword());
		masterSlaveServersConfig.setPingConnectionInterval(redissonProperties.getPingConnectionInterval());
		masterSlaveServersConfig.setRetryAttempts(redissonProperties.getRetryAttempts());
		masterSlaveServersConfig.setRetryInterval(redissonProperties.getRetryInterval());
		masterSlaveServersConfig.setSslEnableEndpointIdentification(redissonProperties.getSslEnableEndpointIdentification());
		masterSlaveServersConfig.setSslKeystore(redissonProperties.getSslKeystore());
		masterSlaveServersConfig.setSslKeystorePassword(redissonProperties.getSslKeystorePassword());
		masterSlaveServersConfig.setSslProvider(redissonProperties.getSslProvider());
		masterSlaveServersConfig.setSslTruststore(redissonProperties.getSslTruststore());
		masterSlaveServersConfig.setSslTruststorePassword(redissonProperties.getSslTruststorePassword());
		masterSlaveServersConfig.setSubscriptionsPerConnection(redissonProperties.getSubscriptionsPerConnection());
		masterSlaveServersConfig.setTcpNoDelay(redissonProperties.getTcpNoDelay());
		masterSlaveServersConfig.setTimeout(redissonProperties.getTimeout());
		return Redisson.create(config);
	}

	/**
	 * 云托管模式 redisson 客户端
	 * @return
	 */
	@Bean
	@ConditionalOnProperty(name = "redisson.mode", havingValue = "replicated")
	RedissonClient redissonReplicated() {
		Config config = initConfigs();
		RedissonMultipleServerConfig multipleServerConfig = redissonProperties.getMultipleServerConfig();
		ReplicatedServersConfig replicatedServersConfig = config.useReplicatedServers();
		replicatedServersConfig.setDatabase(multipleServerConfig.getDatabase());
		replicatedServersConfig.setScanInterval(multipleServerConfig.getScanInterval());
		replicatedServersConfig.setSlaveConnectionMinimumIdleSize(multipleServerConfig.getSlaveConnectionMinimumIdleSize());
		replicatedServersConfig.setSlaveConnectionPoolSize(multipleServerConfig.getSlaveConnectionPoolSize());
		replicatedServersConfig.setFailedSlaveReconnectionInterval(multipleServerConfig.getFailedSlaveReconnectionInterval());
		replicatedServersConfig.setFailedSlaveCheckInterval(multipleServerConfig.getFailedSlaveCheckInterval());
		replicatedServersConfig.setMasterConnectionMinimumIdleSize(multipleServerConfig.getMasterConnectionMinimumIdleSize());
		replicatedServersConfig.setMasterConnectionPoolSize(multipleServerConfig.getMasterConnectionPoolSize());
		replicatedServersConfig.setReadMode(multipleServerConfig.getReadMode());
		replicatedServersConfig.setSubscriptionMode(multipleServerConfig.getSubscriptionMode());
		replicatedServersConfig.setSubscriptionConnectionMinimumIdleSize(multipleServerConfig.getSubscriptionConnectionMinimumIdleSize());
		replicatedServersConfig.setSubscriptionConnectionPoolSize(multipleServerConfig.getSubscriptionConnectionPoolSize());
		replicatedServersConfig.setDnsMonitoringInterval(multipleServerConfig.getDnsMonitoringInterval());
		try {
			replicatedServersConfig.setLoadBalancer((LoadBalancer) Class.forName(multipleServerConfig.getLoadBalancer()).newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		for (String nodeAddress : multipleServerConfig.getNodeAddresses()) {
			replicatedServersConfig.addNodeAddress(prefixAddress(nodeAddress));
		}
		replicatedServersConfig.setPingTimeout(redissonProperties.getPingTimeout());
		replicatedServersConfig.setClientName(redissonProperties.getClientName());
		replicatedServersConfig.setConnectTimeout(redissonProperties.getConnectTimeout());
		replicatedServersConfig.setIdleConnectionTimeout(redissonProperties.getIdleConnectionTimeout());
		replicatedServersConfig.setKeepAlive(redissonProperties.getKeepAlive());
		replicatedServersConfig.setPassword(redissonProperties.getPassword());
		replicatedServersConfig.setPingConnectionInterval(redissonProperties.getPingConnectionInterval());
		replicatedServersConfig.setRetryAttempts(redissonProperties.getRetryAttempts());
		replicatedServersConfig.setRetryInterval(redissonProperties.getRetryInterval());
		replicatedServersConfig.setSslEnableEndpointIdentification(redissonProperties.getSslEnableEndpointIdentification());
		replicatedServersConfig.setSslKeystore(redissonProperties.getSslKeystore());
		replicatedServersConfig.setSslKeystorePassword(redissonProperties.getSslKeystorePassword());
		replicatedServersConfig.setSslProvider(redissonProperties.getSslProvider());
		replicatedServersConfig.setSslTruststore(redissonProperties.getSslTruststore());
		replicatedServersConfig.setSslTruststorePassword(redissonProperties.getSslTruststorePassword());
		replicatedServersConfig.setSubscriptionsPerConnection(redissonProperties.getSubscriptionsPerConnection());
		replicatedServersConfig.setTcpNoDelay(redissonProperties.getTcpNoDelay());
		replicatedServersConfig.setTimeout(redissonProperties.getTimeout());
		return Redisson.create(config);
	}

	public Config initConfigs(){
		Config config=new Config();
		try {
			config.setCodec((Codec) Class.forName(redissonProperties.getCodec()).newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		config.setTransportMode(redissonProperties.getTransportMode());
		if(redissonProperties.getThreads()!=null){
			config.setThreads(redissonProperties.getThreads());
		}
		if(redissonProperties.getNettyThreads()!=null){
			config.setNettyThreads(redissonProperties.getNettyThreads());
		}
		config.setReferenceEnabled(redissonProperties.getReferenceEnabled());
		config.setLockWatchdogTimeout(redissonProperties.getLockWatchdogTimeout());
		config.setKeepPubSubOrder(redissonProperties.getKeepPubSubOrder());
		config.setDecodeInExecutor(redissonProperties.getDecodeInExecutor());
		config.setUseScriptCache(redissonProperties.getUseScriptCache());
		config.setMinCleanUpDelay(redissonProperties.getMinCleanUpDelay());
		config.setMaxCleanUpDelay(redissonProperties.getMaxCleanUpDelay());
		return config;
	}

	private String prefixAddress(String address){
		if(!StringUtils.isEmpty(address)&&!address.startsWith("redis")){
			return "redis://"+address;
		}
		return address;
	}

	@Bean
	public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redisson) {
		return new RedissonConnectionFactory(redisson);
	}

	@Bean("redisTemplate")
	public RedisTemplate getRedisTemplate(RedisConnectionFactory redissonConnectionFactory) {
		RedisTemplate<Object, Object> redisTemplate = new RedisTemplate();
		redisTemplate.setConnectionFactory(redissonConnectionFactory);
		redisTemplate.setValueSerializer(valueSerializer());
		redisTemplate.setKeySerializer(keySerializer());
		redisTemplate.setHashKeySerializer(keySerializer());
		redisTemplate.setHashValueSerializer(valueSerializer());
		return redisTemplate;
	}

	@Bean
	public RedisSerializer keySerializer() {
		return new StringRedisSerializer();
	}

//	@Bean
//	public RedisSerializer valueSerializer() {
//		Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
//		ObjectMapper objectMapper = new ObjectMapper();
//		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//		jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
//		return jackson2JsonRedisSerializer;
//	}

	@Bean
	public RedisSerializer valueSerializer() {
		return new GenericFastJsonRedisSerializer();
	}

}
