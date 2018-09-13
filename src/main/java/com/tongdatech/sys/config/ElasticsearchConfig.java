package com.tongdatech.sys.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.lease.Releasable;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.TransportClientFactoryBean;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;


@Configuration
@Profile("hasElasticSearch")
@EnableElasticsearchRepositories("com.tongdatech")
public class ElasticsearchConfig implements DisposableBean {

	private static final Map<String, String> DEFAULTS;

	static {
		Map<String, String> defaults = new LinkedHashMap<String, String>();
		defaults.put("http.enabled", String.valueOf(false));
		defaults.put("node.local", String.valueOf(true));
		DEFAULTS = Collections.unmodifiableMap(defaults);
	}

	private static final Log logger = LogFactory
			.getLog(ElasticsearchConfig.class);

	@Autowired
	private ElasticsearchSettings properties;

	private Releasable releasable;

	@Bean
	public Client elasticsearchClient() {
		try {
			return createClient();
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private Client createClient() throws Exception {
		if (StringUtils.hasLength(this.properties.getClusterNodes())) {
			return createTransportClient();
		}
		return createNodeClient();
	}

	private Client createNodeClient() throws Exception {
		ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();
		for (Map.Entry<String, String> entry : DEFAULTS.entrySet()) {
			if (!this.properties.getProperties().containsKey(entry.getKey())) {
				settings.put(entry.getKey(), entry.getValue());
			}
		}
		settings.put(this.properties.getProperties());
		Node node = new NodeBuilder().settings(settings)
				.clusterName(this.properties.getClusterName()).node();
		this.releasable = node;
		return node.client();
	}

	private Client createTransportClient() throws Exception {
		TransportClientFactoryBean factory = new TransportClientFactoryBean();
		factory.setClusterNodes(this.properties.getClusterNodes());
		factory.setProperties(createProperties());
		factory.afterPropertiesSet();
		TransportClient client = factory.getObject();
		this.releasable = client;
		return client;
	}

	private Properties createProperties() {
		Properties properties = new Properties();
		properties.put("cluster.name", this.properties.getClusterName());
		properties.putAll(this.properties.getProperties());
		return properties;
	}

	@Override
	public void destroy() throws Exception {
		if (this.releasable != null) {
			try {
				if (logger.isInfoEnabled()) {
					logger.info("Closing Elasticsearch client");
				}
				try {
					this.releasable.close();
				}
				catch (NoSuchMethodError ex) {
					// Earlier versions of Elasticsearch had a different method name
					ReflectionUtils.invokeMethod(
							ReflectionUtils.findMethod(Releasable.class, "release"),
							this.releasable);
				}
			}
			catch (final Exception ex) {
				if (logger.isErrorEnabled()) {
					logger.error("Error closing Elasticsearch client: ", ex);
				}
			}
		}
	}
	
	
	
	

	@Bean
	public ElasticsearchConverter elasticsearchConverter(
			SimpleElasticsearchMappingContext mappingContext) {
		return new MappingElasticsearchConverter(mappingContext);
	}

	@Bean
	public SimpleElasticsearchMappingContext mappingContext() {
		return new SimpleElasticsearchMappingContext();
	}
	@Bean
	public ElasticsearchTemplate elasticsearchTemplate(Client client,
			ElasticsearchConverter converter) {
		try {
			return new ElasticsearchTemplate(client, converter);
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

}
