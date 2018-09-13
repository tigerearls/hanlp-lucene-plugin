package com.tongdatech.sys.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
@Profile("hasElasticSearch")
@Component
public class ElasticsearchSettings implements InitializingBean{
	/**
	 * Elasticsearch cluster name.
	 */
	@Value("${spring.data.elasticsearch.cluster-name:elasticsearch}")
	private String clusterName;

	/**
	 * Comma-separated list of cluster node addresses. If not specified, starts a client
	 * node.
	 */
	@Value("${spring.data.elasticsearch.cluster-nodes:}")
	private String clusterNodes;

	/**
	 * Additional properties used to configure the client.
	 */
	@Value("${spring.data.elasticsearch.properties:}")	
	private String propertiesInlineMapsSpel ;
	
	private Map<String, String> properties = new HashMap<String, String>();

	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		if(!StringUtils.isEmpty(propertiesInlineMapsSpel)){
		ExpressionParser parser = new SpelExpressionParser();
		properties = (Map<String, String>) parser.parseExpression(propertiesInlineMapsSpel).getValue();
		}
	}
	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getClusterNodes() {
		return clusterNodes;
	}

	public void setClusterNodes(String clusterNodes) {
		this.clusterNodes = clusterNodes;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}


	
	
}
