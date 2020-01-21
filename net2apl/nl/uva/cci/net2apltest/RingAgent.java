package nl.uva.cci.net2apltest;

import org.uu.nl.net2apl.core.agent.*;
import org.uu.nl.net2apl.core.defaults.messenger.MessageReceiverNotFoundException;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.Performative;
import org.uu.nl.net2apl.core.fipa.mts.Envelope;
import org.uu.nl.net2apl.core.plan.builtin.FunctionalPlanSchemeInterface;
import org.uu.nl.net2apl.core.plan.builtin.SubPlanInterface;
import org.uu.nl.net2apl.core.platform.Platform;
import org.uu.nl.net2apl.core.platform.PlatformNotFoundException;

import java.net.URISyntaxException;


public class RingAgent extends Agent {

    public RingAgent(Platform p) throws URISyntaxException {
        super(p, new RingAgentArguments());
    }

    static class RingAgentContext implements Context {

        /**
         * Next agent to send the token to
         */
        private AgentID nextAgent;

        public AgentID getNextAgent() {
            return nextAgent;
        }

        public void setNextAgent(AgentID nextAgent) {
            this.nextAgent = nextAgent;
        }
    }

    static class RingPlan implements FunctionalPlanSchemeInterface {

        public SubPlanInterface getPlan(final Trigger trigger, final AgentContextInterface contextInterface) {

            if (trigger instanceof ACLMessage) {
                ACLMessage received = (ACLMessage) trigger;

                switch (received.getPerformative()) {
                    default:
                        return SubPlanInterface.UNINSTANTIATED;

                    case AGREE:
                        return (planInterface) -> {

                            // Get next agent
                            AgentID next = planInterface.getContext(RingAgentContext.class).getNextAgent();

                            // Sub the token number by one
                            int token = Integer.parseInt(received.getContent()) - 1;

                            // If it is still a valid token
                            if (token > 0) {
                                try {
                                    System.out.println("I am " +planInterface.getAgentID() + ", got " + received.getContent() + " now sending to sending " + token + " to " + next);

                                    //send the token to the next agent
                                    planInterface.getAgent().sendMessage(createMessage(planInterface.getAgentID(), next, String.valueOf(token)));
                                } catch (MessageReceiverNotFoundException | PlatformNotFoundException e) {
                                    System.err.println("Can't send message.");
                                    System.exit(1);
                                }
                            } else {
                                // If token is zero just print the time
                                System.out.println(System.nanoTime());
                            }
                        };
                }
            }

            return SubPlanInterface.UNINSTANTIATED;
        }


        /**
         * Helper function to create messages
         *
         * @param aidMe
         * @param aidThem
         * @param reply
         * @return
         */
        public static ACLMessage createMessage(AgentID aidMe, AgentID aidThem, String reply) {
            Envelope envelope = new Envelope();
            envelope.setFrom(aidMe);
            envelope.addTo(aidThem);
            envelope.addIntendedReceiver(aidThem);

            ACLMessage message = new ACLMessage(Performative.AGREE);
            message.addReceiver(aidThem);
            message.addReplyTo(aidMe);
            message.setSender(aidMe);
            message.setContent(reply);
            message.setEnvelope(envelope);

            return message;
        }
    }


    static class RingAgentArguments extends AgentArguments {

        public RingAgentArguments() {
            super.addContext(new RingAgentContext());
            super.addMessagePlanScheme(new RingPlan());
        }
    }
}