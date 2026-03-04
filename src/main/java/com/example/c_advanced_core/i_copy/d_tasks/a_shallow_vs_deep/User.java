package com.example.c_advanced_core.i_copy.d_tasks.a_shallow_vs_deep;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class User {
    private String name;
    private Address address;

    public User(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    // Shallow copy - name и address
    public User shallowCopy() {
        return new User(name, address);
    }

    public User deepCopy() {
        Address copiedAddress = new Address(
                address.getStreet(),
                address.getCity(),
                address.getState()
        );
        return new User(name, copiedAddress);
    }
}
