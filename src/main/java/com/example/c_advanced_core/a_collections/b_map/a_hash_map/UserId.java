package com.example.c_advanced_core.a_collections.b_map.a_hash_map;

public class UserId {
    private final long value;

    UserId(long value) { this.value = value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserId userId)) return false;
        return value == userId.value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }
}
