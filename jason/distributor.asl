t_total(250). // Tokens
t_counter(1).
w_total(500). // Workers
ended(0).
!init_all.

+!init_all : t_total(T) & w_total(WT) <-
                .printf("start");
                while(t_counter(I) & I <= T) {
                    W = I * (WT/T)
                   .concat("thread",W,Thread)
                   .send(Thread,achieve,token(500000));
                   -+t_counter(I+1);
                 }
                 .
