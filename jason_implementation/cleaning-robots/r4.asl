// mars robot 4

/* Initial beliefs */

at(P) :- pos(P,X,Y) & pos(r4,X,Y).

/* Initial goal */

!check(slots).

/* Plans */

+!check(slots) : not garbage(r4)
   <- next(slot);
      !check(slots).

+!check(slots) : garbage(r4).

+garbage(r4) : not .desire(carry_to(r2))
   <- !carry_to(r2).

+!carry_to(R)
   <- .drop_desire(check(slots)); // stop checking
      
      // remember where to go back
      ?pos(r4,X,Y);
      -+pos(last,X,Y);

      // carry garbage to r2
      !take(garb,R);

      // goes back and continue to check
      !at(last);
      !!check(slots).


+!take(S,L)
   <- !ensure_pick(S);
      !at(L);
      drop(S).

+!ensure_pick(S) : garbage(r4)
   <- pick(garb);
      !ensure_pick(S).
+!ensure_pick(_).

+!at(L) : at(L).
+!at(L) <- ?pos(L,X,Y);
           move_towards(X,Y);
           !at(L).
