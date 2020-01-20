+!token(0) <- .printf("finished").

+!token(T) : next(X) <- .send(X, achieve, token(T-1)) .


+!token(T) : .my_name(M) & .delete("thread", M, NS) & .term2string(N,NS) &
                                     Y = N mod 500 + 1 &
                                 .concat("thread",Y,X) <- +next(X); .send(X, achieve, token(T-1)).

+!token(N) : not next(F)  & .my_name(M) <- .print("WTF").
