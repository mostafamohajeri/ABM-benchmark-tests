package nl.uva.cci.net2apltest;

import java.net.URISyntaxException;


import org.uu.nl.net2apl.core.agent.Agent;
import org.uu.nl.net2apl.core.agent.AgentCreationFailedException;
import org.uu.nl.net2apl.core.defaults.messenger.MessageReceiverNotFoundException;
import org.uu.nl.net2apl.core.fipa.FIPAMessenger;
import org.uu.nl.net2apl.core.logging.NullLogger;
import org.uu.nl.net2apl.core.messaging.Messenger;
import org.uu.nl.net2apl.core.platform.Platform;
import org.uu.nl.net2apl.core.platform.PlatformNotFoundException;

public class Main {

    public static void main(String[] args) throws MessageReceiverNotFoundException, PlatformNotFoundException {


        Messenger< ? > messenger = new FIPAMessenger();

        Platform platform;
        // How many threads???
            platform = Platform.newPlatform(8, messenger);

            // Copy pasted from example
        try {
            Agent yellowPages = platform.newDirectoryFacilitator();
            platform.setLogger(new NullLogger());
            System.out.println("Created DirectoryFacilitator, AgentID:");
            System.out.println(yellowPages.getAID().toString());

        } catch (URISyntaxException | AgentCreationFailedException ex) {
            System.err.println("Failed to create yellow-pages (= directory facilitator) Agent.");
            System.exit(1);
        }

        //Code start

        // Number of tokens
        int tokens = 250;
        // Number of agents
        int workers = 500;
        // Number of times each message should be sent
        int rounds = 5000;

        try {
            // create the agents
            RingAgent[] agents = new RingAgent[workers];
            for (int i = 0; i < agents.length; i++) {
                RingAgent agent = new RingAgent(platform);
                // link each agent to previous one
                if(i > 0)
                    agent.getContext(RingAgent.RingAgentContext.class).setNextAgent(agents[i-1].getAID());
                agents[i] = agent;
            }

            System.out.println( " Start time: " + System.nanoTime());

            // ling first agent to the last one
            agents[0].getContext(RingAgent.RingAgentContext.class).setNextAgent(agents[agents.length-1].getAID());

            // Distribute all the tokens among agents
            for (int i = 0;i<tokens;i++) {
                int w = i * (workers / tokens);

                agents[w].sendMessage(
                       RingAgent.RingPlan.createMessage(agents[w].getAID(),agents[w].getAID(),String.valueOf(rounds))
                );
            }

        } catch (URISyntaxException ex) {
            System.err.println("Failed to create a Hello World Agent.");
            System.exit(1);
        }

    }



}

