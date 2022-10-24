package pbassigment;

import io.micronaut.runtime.Micronaut;

public class PbaApplication {

    public static void main(String[] args) {
        Micronaut.build(args)
                .banner(false)
                .mainClass(PbaApplication.class)
                .start();
    }

}
