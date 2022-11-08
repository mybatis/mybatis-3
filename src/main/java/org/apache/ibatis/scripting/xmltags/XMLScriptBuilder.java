/*
 *    Copyright 2009-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.scripting.xmltags;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.session.Configuration;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Clinton Begin
 */
public class XMLScriptBuilder extends BaseBuilder {

	private final XNode context;
	private boolean isDynamic;
	private final Class<?> parameterType;
	private final Map<String, NodeHandler> nodeHandlerMap = new HashMap<>();

	public XMLScriptBuilder(Configuration configuration, XNode context) {
		this(configuration, context, null);
	}

	public XMLScriptBuilder(Configuration configuration, XNode context, Class<?> parameterType) {
		super(configuration);
		this.context = context;
		this.parameterType = parameterType;
		initNodeHandlerMap();
	}

	private void initNodeHandlerMap() {
		nodeHandlerMap.put("trim", new TrimHandler());
		nodeHandlerMap.put("where", new WhereHandler());
		nodeHandlerMap.put("set", new SetHandler());
		nodeHandlerMap.put("foreach", new ForEachHandler());
		nodeHandlerMap.put("if", new IfHandler());
		nodeHandlerMap.put("choose", new ChooseHandler());
		nodeHandlerMap.put("when", new IfHandler());
		nodeHandlerMap.put("otherwise", new OtherwiseHandler());
		nodeHandlerMap.put("bind", new BindHandler());
	}

	public SqlSource parseScriptNode() {
		MixedSqlNode rootSqlNode = parseDynamicTags(context);
		SqlSource sqlSource;
		if (isDynamic) {
			sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
		} else {
			sqlSource = new RawSqlSource(configuration, rootSqlNode, parameterType);
		}
		return sqlSource;
	}

	protected MixedSqlNode parseDynamicTags(XNode node) {
		List<SqlNode> contents = new ArrayList<>();
		NodeList children = node.getNode().getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			XNode child = node.newXNode(children.item(i));
			if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE
					|| child.getNode().getNodeType() == Node.TEXT_NODE) {
				String key = getHash(child.getStringBody(""));
				if (configuration.containsSqlNode(key)) {
					SqlNode sqlNode = configuration.getSqlNode(key);
					if (sqlNode instanceof TextSqlNode) {
						isDynamic = true;
					}
					contents.add(sqlNode);
				} else {
					String data = child.getStringBody("");
					TextSqlNode textSqlNode = new TextSqlNode(data);
					if (textSqlNode.isDynamic()) {
						contents.add(textSqlNode);
						isDynamic = true;
						configuration.addSqlNode(key, textSqlNode);
					} else {
						StaticTextSqlNode staticTextSqlNode = new StaticTextSqlNode(data);
						contents.add(staticTextSqlNode);
						configuration.addSqlNode(key, staticTextSqlNode);
					}
				}
			} else if (child.getNode().getNodeType() == Node.ELEMENT_NODE) { // issue #628
				String key = getHash(child.toStringWithContent());
				String nodeName = child.getNode().getNodeName();
				NodeHandler handler = nodeHandlerMap.get(nodeName);
				if (configuration.containsSqlNode(key) && !(handler instanceof ChooseHandler)) {
					contents.add(configuration.getSqlNode(key));
					isDynamic = true;
				} else {
					if (handler == null) {
						throw new BuilderException("Unknown element <" + nodeName + "> in SQL statement.");
					}
					SqlNode sqlNode = handler.handleNode(child);
					configuration.addSqlNode(key, sqlNode);
					contents.add(sqlNode);
					isDynamic = true;
				}
			}
		}
		return new MixedSqlNode(contents);
	}

	private String getHash(String key) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			byte hashBytes[] = messageDigest.digest(key.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hashBytes);
		} catch (NoSuchAlgorithmException e) {
			return key;
		}
	}

	private interface NodeHandler {
		SqlNode handleNode(XNode nodeToHandle);
	}

	private class BindHandler implements NodeHandler {
		public BindHandler() {
			// Prevent Synthetic Access
		}

		@Override
		public SqlNode handleNode(XNode nodeToHandle) {
			final String name = nodeToHandle.getStringAttribute("name");
			final String expression = nodeToHandle.getStringAttribute("value");
			return new VarDeclSqlNode(name, expression);
		}
	}

	private class TrimHandler implements NodeHandler {
		public TrimHandler() {
			// Prevent Synthetic Access
		}

		@Override
		public SqlNode handleNode(XNode nodeToHandle) {
			MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
			String prefix = nodeToHandle.getStringAttribute("prefix");
			String prefixOverrides = nodeToHandle.getStringAttribute("prefixOverrides");
			String suffix = nodeToHandle.getStringAttribute("suffix");
			String suffixOverrides = nodeToHandle.getStringAttribute("suffixOverrides");
			return new TrimSqlNode(configuration, mixedSqlNode, prefix, prefixOverrides, suffix, suffixOverrides);
		}
	}

	private class WhereHandler implements NodeHandler {
		public WhereHandler() {
			// Prevent Synthetic Access
		}

		@Override
		public SqlNode handleNode(XNode nodeToHandle) {
			MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
			return new WhereSqlNode(configuration, mixedSqlNode);
		}
	}

	private class SetHandler implements NodeHandler {
		public SetHandler() {
			// Prevent Synthetic Access
		}

		@Override
		public SqlNode handleNode(XNode nodeToHandle) {
			MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
			return new SetSqlNode(configuration, mixedSqlNode);
		}
	}

	private class ForEachHandler implements NodeHandler {
		public ForEachHandler() {
			// Prevent Synthetic Access
		}

		@Override
		public SqlNode handleNode(XNode nodeToHandle) {
			MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
			String collection = nodeToHandle.getStringAttribute("collection");
			String item = nodeToHandle.getStringAttribute("item");
			String index = nodeToHandle.getStringAttribute("index");
			String open = nodeToHandle.getStringAttribute("open");
			String close = nodeToHandle.getStringAttribute("close");
			String separator = nodeToHandle.getStringAttribute("separator");
			return new ForEachSqlNode(configuration, mixedSqlNode, collection, index, item, open, close, separator);
		}
	}

	private class IfHandler implements NodeHandler {
		public IfHandler() {
			// Prevent Synthetic Access
		}

		@Override
		public SqlNode handleNode(XNode nodeToHandle) {
			MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
			String test = nodeToHandle.getStringAttribute("test");
			return new IfSqlNode(mixedSqlNode, test);
		}
	}

	private class OtherwiseHandler implements NodeHandler {
		public OtherwiseHandler() {
			// Prevent Synthetic Access
		}

		@Override
		public SqlNode handleNode(XNode nodeToHandle) {
			return parseDynamicTags(nodeToHandle);
		}
	}

	private class ChooseHandler implements NodeHandler {
		public ChooseHandler() {
			// Prevent Synthetic Access
		}

		@Override
		public SqlNode handleNode(XNode nodeToHandle) {
			List<SqlNode> whenSqlNodes = new ArrayList<>();
			List<SqlNode> otherwiseSqlNodes = new ArrayList<>();
			handleWhenOtherwiseNodes(nodeToHandle, whenSqlNodes, otherwiseSqlNodes);
			SqlNode defaultSqlNode = getDefaultSqlNode(otherwiseSqlNodes);
			return new ChooseSqlNode(whenSqlNodes, defaultSqlNode);
		}

		private void handleWhenOtherwiseNodes(XNode chooseSqlNode, List<SqlNode> ifSqlNodes,
				List<SqlNode> defaultSqlNodes) {
			List<XNode> children = chooseSqlNode.getChildren();
			for (XNode child : children) {
				String nodeName = child.getNode().getNodeName();
				NodeHandler handler = nodeHandlerMap.get(nodeName);
				if (handler instanceof IfHandler) {
					String key = getHash(child.toStringWithContent());
					if (configuration.containsSqlNode(key)) {
						ifSqlNodes.add(configuration.getSqlNode(key));
						isDynamic = true;
					} else {
						SqlNode sqlNode = handler.handleNode(child);
						ifSqlNodes.add(sqlNode);
						configuration.addSqlNode(key, sqlNode);
					}
				} else if (handler instanceof OtherwiseHandler) {
					String key = getHash(child.toStringWithContent());
					if (configuration.containsSqlNode(key)) {
						defaultSqlNodes.add(configuration.getSqlNode(key));
					} else {
						SqlNode sqlNode = handler.handleNode(child);
						defaultSqlNodes.add(sqlNode);
						configuration.addSqlNode(key, sqlNode);
					}
				}
			}
		}

		private SqlNode getDefaultSqlNode(List<SqlNode> defaultSqlNodes) {
			SqlNode defaultSqlNode = null;
			if (defaultSqlNodes.size() == 1) {
				defaultSqlNode = defaultSqlNodes.get(0);
			} else if (defaultSqlNodes.size() > 1) {
				throw new BuilderException("Too many default (otherwise) elements in choose statement.");
			}
			return defaultSqlNode;
		}
	}

}
