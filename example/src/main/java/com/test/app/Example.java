package com.test.app;

import com.petertackage.jonty.Fieldable;

@Fieldable
public class Example {

    private String a;
    private String b;
    private String c;

    public static void main(String[] args) {
        new Example();
        new KotlinDataClass("xyz");
        // System.out.println(Jonty.field(Example.class));
    }

    @Fieldable
    public static class InnerExample {

        private String d;
        private String e;
        private String f;
    }

    @Fieldable
    private static class PrivateInnerExample {
        private String g;
    }

    @Fieldable
    private class PrivateInnerNonStaticExample {
        private String h;
        public String k;
    }

    @Fieldable
    class SubClass extends PrivateInnerNonStaticExample {
        private String i;
    }

    class PlainParent {
        private String pp;
    }

    @Fieldable
    class SubClass2 extends PlainParent {
        private String i;
    }

    @Fieldable
    class Empty {

    }
}
