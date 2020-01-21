import pykka
import time
from pykka import ActorRef


class RingActor(pykka.ThreadingActor):
    def __init__(self, name: str, next_actor: ActorRef):
        super().__init__()
        self.next_actor: ActorRef = next_actor
        self.name: str = name

    def on_receive(self, message):
        if isinstance(message, ActorRef):
            print("setting next ", self.name)
            self.next_actor = message
        elif message > 0:
            if (message % 1000) == 0 and self.name == "0":
                print("%.20f" % time.time(),message)
            self.next_actor.tell(message - 1)
        else:
            print(self.name,message, " says finished at %.20f" % time.time())


if __name__ == '__main__':

    tokens = 250
    workers = 1000
    rounds: int = 500000

    agents = []
    agent_refs = []

    for i in range(0, workers):
        if i > 0:
            actor = RingActor(str(i),agent_refs[i-1])
            agent_refs.append(actor.start(str(i),agent_refs[i-1]))
        else:
            actor = RingActor(str(i),None)
            agent_refs.append(actor.start(str(i), None))

    agent_refs[0].tell(agent_refs[workers-1])

    print("%.20f" % time.time())

    for i in range(0, tokens):
        w = int(i * (workers / tokens))
        agent_refs[w].tell(rounds)
