package com.wecon.common.test;

import java.util.function.Consumer;

/**
 * Created by fengbing_dian91 on 2015/12/8.
 */
public class TestListForEach<T> implements Consumer<T>
{
    @Override
    public void accept(T t)
    {
        System.out.println("aaa = " + String.valueOf(t));
    }

    @Override
    public Consumer<T> andThen(Consumer<? super T> after)
    {

        return null;
    }
}
