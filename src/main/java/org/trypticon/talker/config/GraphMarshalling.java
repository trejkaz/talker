package org.trypticon.talker.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import org.trypticon.talker.TalkerContext;
import org.trypticon.talker.messages.MessageStreamNodeFactory;
import org.trypticon.talker.model.*;
import org.trypticon.talker.rendering.RenderingNodeFactory;
import org.trypticon.talker.speech.SpeakerNodeFactory;
import org.trypticon.talker.text.substitution.SubstituterNodeFactory;
import org.trypticon.talker.text.tokenisation.TokenizerNodeFactory;

/**
 * Utility to load and save graphs in the top-level configuration file.
 */
public class GraphMarshalling {
    private GraphMarshalling() {}

    /**
     * Stores a graph as configuration.
     *
     * @param graph the graph.
     * @return the configuration.
     */
    public static Configuration storeGraph(Graph graph) {
        Map<Node, Integer> idsByNode = new LinkedHashMap<>();

        ImmutableList.Builder<Configuration> nodeConfigurations = ImmutableList.builder();
        int nodeId = 0;
        for (Node node : graph.getNodes()) {
            nodeConfigurations.add(Configuration.builder()
                    .put("providerId", node.getProviderId())
                    .put("config", node.getConfiguration())
                    .build());

            nodeId++;
            idsByNode.put(node, nodeId);
        }

        ImmutableList.Builder<Configuration> connectionConfigurations = ImmutableList.builder();
        for (Connection connection : graph.getConnections()) {
            connectionConfigurations.add(Configuration.builder()
                    .put("fromNode", idsByNode.get(connection.getSource().getParent()))
                    .put("fromConnector", connection.getSource().getId())
                    .put("toNode", idsByNode.get(connection.getDestination().getParent()))
                    .put("toConnector", connection.getDestination().getId())
                    .build());
        }

        return Configuration.builder()
                .put("nodes", nodeConfigurations.build())
                .put("connections", connectionConfigurations.build())
                .build();
    }

    /**
     * Loads a graph from configuration.
     *
     * @param context the application context.
     * @param topConfiguration the top configuration object.
     * @return the graph.
     */
    public static Graph loadGraph(TalkerContext context, Configuration topConfiguration) {
        Graph graph = new Graph();

        Map<Integer, Node> nodesById = new LinkedHashMap<>();

        List<Configuration> nodeConfigurations = topConfiguration.getSubSectionList("nodes");
        for (Configuration nodeConfiguration : nodeConfigurations) {
            Node node = loadNode(graph, context, nodeConfiguration);
            graph.add(node);
            nodesById.put(nodeConfiguration.getRequiredInt("id"), node);
        }

        List<Configuration> connectionConfigurations = topConfiguration.getSubSectionList("connections");
        for (Configuration connectionConfiguration : connectionConfigurations) {
            Connection connection = loadConnection(nodesById, connectionConfiguration);
            graph.add(connection);
        }

        return graph;
    }

    private static Node loadNode(Graph graph, TalkerContext context, Configuration configuration) {
        ImmutableList<NodeFactory> nodeFactories = ImmutableList.of(
                new MessageStreamNodeFactory(),
                new RenderingNodeFactory(),
                new TokenizerNodeFactory(),
                new SubstituterNodeFactory(),
                new SpeakerNodeFactory());
        String providerId = configuration.getRequiredString("providerId");
        Configuration innerConfiguration = configuration.getSubSection("config");
        for (NodeFactory factory : nodeFactories) {
            Node node = factory.create(graph, context, providerId, innerConfiguration);
            if (node != null) {
                return node;
            }
        }
        throw new IllegalStateException("Unknown node provider: " + providerId);
    }

    private static Connection loadConnection(Map<Integer, Node> nodesById, Configuration configuration) {
        Node sourceNode = nodesById.get(configuration.getRequiredInt("fromNode"));
        OutputConnector sourceConnector = sourceNode.getOutputConnectorById(configuration.getRequiredString("fromConnector"));
        Node targetNode = nodesById.get(configuration.getRequiredInt("toNode"));
        InputConnector targetConnector = targetNode.getInputConnectorById(configuration.getRequiredString("toConnector"));
        return new Connection(sourceConnector, targetConnector);
    }
}