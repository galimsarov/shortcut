package com.example.b_advanced_core.i_copy.c_prototype;

import java.util.HashMap;
import java.util.Map;

class CUnitRegistry {
    private final Map<String, BGameUnit> prototypes = new HashMap<>();

    void register(String key, BGameUnit prototype) {
        prototypes.put(key, prototype);
    }

    BGameUnit create(String key) {
        BGameUnit prototype = prototypes.get(key);
        if (prototype == null) {
            throw new IllegalArgumentException("Unknown prototype: " + key);
        }
        return prototype.copy();
    }
}
