package com.test.app;

import com.petertackage.jonty.Fieldable;

@Fieldable
public class Example {

    private String a;
    private String b;
    private String c;

    public static void main(String[] args) {
        new Example();
        // System.out.println(Jonty.field(Example.class));
    }
}
