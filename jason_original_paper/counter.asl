count(1).

//!start.


+finished(T) : count(250) <- .stopMAS.

@pc[atomic]
+finished(T) : count(X) <- -+count(X+1).