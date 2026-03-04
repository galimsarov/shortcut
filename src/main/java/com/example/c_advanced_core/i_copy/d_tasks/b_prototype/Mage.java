package com.example.c_advanced_core.i_copy.d_tasks.b_prototype;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Mage extends GameUnit {
    private int magic;

    public Mage(int health, int damage, int magic) {
        super(health, damage);
        this.magic = magic;
    }

    @Override
    public UnitType getUnitType() {
        return UnitType.MAGE;
    }

    @Override
    public GameUnit copy() {
        return new Mage(super.getHealth(), super.getDamage(), magic);
    }

    @Override
    public String toString() {
        return "Mage(health=" + super.getHealth() + ",damage=" + super.getDamage() + ",magic=" + magic +")";
    }
}
