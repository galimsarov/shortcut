package com.example.a_alg_and_structs.c_streams.d_orders_chain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString
@AllArgsConstructor
public class Order {
    private int id;
    private int price;
    private Status status;
    private LocalDate date;
}
