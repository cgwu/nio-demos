package com.dannis.rxjavademo.generic;

import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        // Object <- Fruit <- Apple <- RedApple
        System.out.println(Apple.class.isAssignableFrom(Fruit.class));
        System.out.println(Fruit.class.isAssignableFrom(Fruit.class));
        System.out.println(Fruit.class.isAssignableFrom(Apple.class));  // apple 是 Fruit子类

        // 协变
        List<Apple> apples = new ArrayList<Apple>();
        List<? extends Fruit> listfruits = apples;
//        listfruits.add(new Apple());
//        listfruits.add(new RedApple());
//        listfruits.add(new Fruit());  // ERROR: 不能够往一个使用了? extends的数据结构里写入任何的值。
        Fruit f = listfruits.get(0);

        // 逆变
        List<Fruit> fruits = new ArrayList<Fruit>();
        List<? super Apple> list2 = fruits;
//        list2.add(new Fruit());   //ERROR
        list2.add(new Apple());
        list2.add(new RedApple());
//        Fruit f = list2.get(0);   //ERROR

        /*
        存取原则和PECS法则
        总结 ? extends 和 the ? super 通配符的特征，我们可以得出以下结论：

        如果你想从一个数据类型里获取数据，使用 ? extends 通配符
        如果你想把对象写入一个数据结构里，使用 ? super 通配符
        如果你既想存，又想取，那就别用通配符。
        这就是Maurice Naftalin在他的《Java Generics and Collections》这本书中所说的存取原则，
        以及Joshua Bloch在他的《Effective Java》这本书中所说的PECS法则。
        Bloch提醒说，这PECS是指”Producer Extends, Consumer Super”，这个更容易记忆和运用。
        * */
    }
}
