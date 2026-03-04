package com.example.c_advanced_core.i_copy.d_tasks.b_prototype;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Warrior extends GameUnit {
    private int armor;

    public Warrior(int health, int damage, int armor) {
        super(health, damage);
        this.armor = armor;
    }

    @Override
    public UnitType getUnitType() {
        return UnitType.WARRIOR;
    }

    @Override
    public GameUnit copy() {
        return new Warrior(super.getHealth(), super.getDamage(), armor);
    }

    @Override
    public String toString() {
        return "Warrior(health=" + super.getHealth() + ",damage=" + super.getDamage() + ",armor=" + armor +")";
    }
}
