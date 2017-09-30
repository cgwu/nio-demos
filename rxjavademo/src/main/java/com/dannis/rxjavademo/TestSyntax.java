package com.dannis.rxjavademo;

import rx.Observable;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TestSyntax {

    public static void main(String[] args) {
        Observable.just(2, 3, 5, 8)
                .map(v -> v * 3)
                .map(v -> (v % 2 == 0) ? "even" : "odd")
                .subscribe(System.out::println);
        System.out.println("---------------------");
        Observable<Integer> flatmapped = Observable
                .just(-1, 0, 1)
                .map(v -> 2 / v)
                .flatMap(
                        v -> Observable.just(v),
                        e -> Observable.just(0),
                        () -> Observable.just(42)
                );
        flatmapped.subscribe(System.out::println);
    }

    public static void subscribePrint(Observable<Long> observable, String name) {
        observable.subscribe(
                (v) -> System.out.println(name + " : " + v),
                (e) -> {
                    System.err.println("Error from " + name + ":");
                    System.err.println(e.getMessage());
                },
                () -> System.out.println(name + " ended!")
        );
    }

    public static <T> Observable<T> fromIterable(final Iterable<T> iterable) {
        return Observable.unsafeCreate(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                try {
                    Iterator<T> iterator = iterable.iterator();
                    while (iterator.hasNext()) {
                        subscriber.onNext(iterator.next());
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static void main4(String[] args) {
        List<String> list = Arrays.asList("hello", "中国");
        Observable<String> obs = fromIterable(list);
        obs.subscribe(System.out::println);
    }


    public static void main3(String[] args) throws Exception {
        subscribePrint(Observable.interval(500L, TimeUnit.MILLISECONDS),
                "Interval Observable");

        subscribePrint(Observable.interval(0L, 1L, TimeUnit.SECONDS),
                "Timed Interval Observable");

        subscribePrint(Observable.timer(1L, TimeUnit.SECONDS),
                "#Timer Observable");

        subscribePrint(Observable.error(new Exception("Test Error!")), "Error Observable");
        subscribePrint(Observable.empty(), "$Empty Observable");
        subscribePrint(Observable.never(), "*Never Observable");
//        subscribePrint(Observable.range(1L, 3L), "Range Observable");


        Thread.sleep(2000L);
    }

    public static void hello(String... names) {
        List<String> summary = new ArrayList<String>();
//        Observable.from(names).subscribe(System.out::println);
        Observable.from(names).subscribe(
                x -> {
                    summary.add(x);
                    System.out.println(x);
                },
                t -> {
                    t.printStackTrace();
                },
                () -> System.out.println("完成")
        );
        System.out.println(summary);

    }

    public static void main2(String[] args) {
        hello("张三", "李四");

        hello("中国人民", "张三", "李四");

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
