package com.dannis.rxjavademo;

import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Demo {

    static void testObservable() {
        List<String> list = Arrays.asList("One", "Two", "Three", "Four", "Five");
        Observable<String> observable = Observable.from(list);
        observable.subscribe(
                new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println(s);
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        System.err.println(throwable);
                    }
                },
                new Action0() {
                    @Override
                    public void call() {
                        System.out.println("完成!");
                    }
                }
        );
        System.out.println("---------");
        observable.subscribe(System.out::println, System.err::println, () -> System.out.println("完成"));

    }

    static void from() {
//        Path resources = Paths.get("F:\\workspace\\netty-exercise\\rxjavademo\\src","main","java" );
        Path resources = Paths.get("src", "main");
        try (DirectoryStream<Path> dStream
                     = Files.newDirectoryStream(resources)) {
            Observable<Path> dirObservable = Observable.from(dStream);
            dirObservable.subscribe(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void just() {
        Observable
                .just('R', 'x', 'J', 'a', 'v', 'a')
                .subscribe(
                        System.out::print,
                        System.err::println,
                        System.out::println
                );

        Observable
                .just(new User("Dali", "Bali"))
                .map(u -> u.getForename() + " - " + u.getLastname())
                .subscribe(System.out::println);
    }

    static <T> void subscribePrint(Observable<T> observable, String name) {
        observable.subscribe(
                (v) -> System.out.println(name + " : " + v),
                (e) -> {
                    System.err.println("Error from " + name + ":");
                    System.err.println(e.getMessage());
                },
                () -> System.out.println(name + " ended!")
        );
    }

    static void testSubscribePrint() {
        subscribePrint(
                Observable.interval(500L, TimeUnit.MILLISECONDS),
                "Interval Observable"
        );
        subscribePrint(
                Observable.timer(0L, 1L, TimeUnit.SECONDS),
                "Timed Interval Observable"
        );
        subscribePrint(
                Observable.timer(1L, TimeUnit.SECONDS),
                "Timer Observable");
        subscribePrint(
                Observable.error(new Exception("Test Error!")),
                "Error Observable"
        );
        subscribePrint(Observable.empty(), "Empty Observable");
        subscribePrint(Observable.never(), "Never Observable");
        subscribePrint(Observable.range(1, 3), "Range Observable");
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void testObservableCreate() {
        Observable<String> os = Observable.unsafeCreate(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello");
                subscriber.onNext("中国");
                subscriber.onError(new Exception("错误"));
                subscriber.onNext("world");
                subscriber.onCompleted();
            }
        });
        os.subscribe(System.out::println, System.out::println, () -> System.out.println("完成"));
    }

    static void testFlatMap() {
        Observable<Integer> flatMapped = Observable
                .just(5, 432)
                .flatMap(
                        v -> Observable.range(v, 3)
//                        , (x, y) -> x + y     // x代表原始数据(t1,t2,...)，y代表新Observable中的一项
                        , (x, y) -> x * y
                );
        subscribePrint(flatMapped, "flatMap");
    }

    static void testFlatMapIterable() {
        Observable<?> fIterableMapped = Observable
                .just(
                        Arrays.asList(2, 4),
                        Arrays.asList("two", "four")
                )
                .flatMapIterable(l -> l);   // 等价于　.flatMap(v -> Observable.from(v));

        subscribePrint(fIterableMapped, "fIterableMapped");
    }

    static void testFlatMapIterable2() {
        Observable<?> fIterableMapped = Observable
                .just(
                        Arrays.asList(22, 4),
                        Arrays.asList("two2", "four")
                )
                .flatMap(v -> Observable.from(v));
        subscribePrint(fIterableMapped, "fIterableMapped2");
    }

    static void foo() {
//        subscribePrint(Observable.just(11,22), "foo");
        subscribePrint(Observable.range(110, 2), "foo");
    }

    static void testSingleFlatMap() {
        Single<String> s1 = Single.just("Hello2世界");
//        Single<String> s1 = Single.error(new Exception("错误1"));

        s1.flatMap(id -> {
            System.out.println("id:" + id);
            return Single.just(id + " - 中国a");
        })
//        s1.flatMap(id -> Single.error(new Exception("错误2")))
                .subscribe(System.out::println, System.out::println);
    }

    public static void main(String[] args) {
//        testObservable();
//        from();
//        just();
//        testSubscribePrint();
//        testObservableCreate();
//        testFlatMap();
//        foo();
//        testFlatMapIterable();
//        testFlatMapIterable2();
        testSingleFlatMap();
    }
}

class User {
    private final String forename;
    private final String lastname;

    public User(String forename, String lastname) {
        this.forename = forename;
        this.lastname = lastname;
    }

    public String getForename() {
        return this.forename;
    }

    public String getLastname() {
        return this.lastname;
    }
}

