package com.example.c_advanced_core.i_copy.d_tasks.b_prototype;

import lombok.Getter;
import lombok.Setter;

/**
 * Сделать реестр шаблонов объектов и их клонирование (паттерн Prototype).
 * Требования:
 * - Базовый абстрактный класс GameUnit с методом copy().
 * - Минимум 2 типа юнитов (например, Warrior, Mage) с вложенными mutable-полями.
 * - UnitRegistry хранит прототипы по ключу и возвращает копии.
 * - Продемонстрировать, что изменение клонов не меняет прототипы.
 */
@Getter
@Setter
public abstract class GameUnit {
    private int health;
    private int damage;

    protected GameUnit(int health, int damage) {
        this.health = health;
        this.damage = damage;
    }

    abstract UnitType getUnitType();

    abstract GameUnit copy();
}
