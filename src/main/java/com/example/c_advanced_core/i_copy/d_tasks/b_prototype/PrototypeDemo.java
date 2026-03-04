package com.example.c_advanced_core.i_copy.d_tasks.b_prototype;

public class PrototypeDemo {
    public static void main(String[] args) {
        UnitRegistry registry = new UnitRegistry();
        registry.register(UnitType.WARRIOR, new Warrior(100, 30, 30));
        registry.register(UnitType.MAGE, new Mage(50, 50, 50));

        Warrior captain = (Warrior) registry.create(UnitType.WARRIOR);
        captain.setHealth(150);
        captain.setDamage(50);
        captain.setArmor(50);
        System.out.println("Captain: " + captain);

        Mage archMage = (Mage) registry.create(UnitType.MAGE);
        archMage.setHealth(75);
        archMage.setDamage(75);
        archMage.setMagic(75);
        System.out.println("ArchMage: " + archMage);

        System.out.println("Prototypes:");
        for (GameUnit gameUnit : registry.getPrototypes().values()) {
            switch (gameUnit.getUnitType()) {
                case WARRIOR -> System.out.println("Warrior: " + gameUnit);
                case MAGE -> System.out.println("Mage: " + gameUnit);
            }
        }
    }
}
