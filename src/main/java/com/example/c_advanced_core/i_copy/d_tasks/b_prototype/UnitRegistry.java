package com.example.c_advanced_core.i_copy.d_tasks.b_prototype;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class UnitRegistry {
    private final Map<UnitType, GameUnit> prototypes = new HashMap<>();

    void register(UnitType type, GameUnit prototype) {
        prototypes.put(type, prototype);
    }

    GameUnit create(UnitType type) {
        GameUnit prototype = prototypes.get(type);
        if (prototype == null) {
            throw new IllegalArgumentException("Unknown unit: " + type);
        }
        return prototype.copy();
    }
}
