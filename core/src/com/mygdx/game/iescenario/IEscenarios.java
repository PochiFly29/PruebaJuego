package com.mygdx.game.iescenario;

import com.mygdx.game.Lluvia;
import com.mygdx.game.Tarro;

public interface IEscenarios {
    void init(Lluvia ctx);
    void update(Lluvia ctx, Tarro tarro, float dt);
}