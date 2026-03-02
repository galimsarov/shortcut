package com.example.c_advanced_core.i_copy.c_prototype;

class BGameUnit implements APrototype<BGameUnit> {
    private final String type;
    private final int baseHp;

    BGameUnit(String type, int baseHp) {
        this.type = type;
        this.baseHp = baseHp;
    }

    @Override
    public BGameUnit copy() {
        return new BGameUnit(type, baseHp);
    }

    @Override
    public String toString() {
        return "GameUnit{" + "type='" + type + '\'' + ", baseHp=" + baseHp + '}';
    }
}
