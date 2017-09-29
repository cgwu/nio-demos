package com.dannis.rxjavademo;

import rx.Observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class TestSyntax {

    public static void hello(String... names) {
        List<String> summary = new ArrayList<String>();
//        Observable.from(names).subscribe(System.out::println);
        Observable.from(names).subscribe(
                x -> {
                    summary.add(x);
                    System.out.println(x);
                },
                t ->{
                    t.printStackTrace();
                },
                () -> System.out.println("完成")
        );
        System.out.println(summary);

    }

    public static void main(String[] args) {
        hello("张三", "李四");

        Consumer<String> print = System.out::println;
        print.accept("yes");
        print.accept("no");

    }

    public static void main1(String[] args) {
        Observable<String> howdy = Observable.just("Howdy中国");
        howdy.subscribe(System.out::println);

        Observable.just("Hello", "世界").subscribe(System.out::println);

        List<String> words = Arrays.asList(
                "the",
                "quick",
                "brown",
                "fox",
                "jumped",
                "over",
                "the",
                "lazy",
                "dog"
        );
        Observable.just(words).subscribe(word -> System.out.println(word));
        System.out.println(words);
        Observable.from(words).subscribe(System.out::println);

        Observable.from(words)
                .zipWith(Observable.range(1, Integer.MAX_VALUE),
                        (str, idx) -> String.format("%2d. %s", idx, str))
                .subscribe(System.out::println);

        Observable.from(words)
                .flatMap(word -> Observable.from(word.split("")))
                .distinct()
                .sorted()
                .zipWith(Observable.range(1, Integer.MAX_VALUE),
                        (str, idx) -> String.format("%2d. %s", idx, str))
//                .count()
                .subscribe(System.out::println);

        System.out.println("完成");
    }
}
