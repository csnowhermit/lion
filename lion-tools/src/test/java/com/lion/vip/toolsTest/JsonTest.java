/**
 * FileName: JsonTest
 * Author:   Ren Xiaotian
 * Date:     2018/11/22 14:57
 */

package com.lion.vip.toolsTest;

import com.alibaba.fastjson.JSON;

import java.util.List;

public class JsonTest {
    public static void main(String[] args) {
        Person p = new Person("张三", 20);
        String jsonStr = JSON.toJSONString(p);
        System.out.println(jsonStr);

        jsonStr.replaceAll("\\{", "[");
        jsonStr.replaceAll("}", "]");

        List<Person> list = JSON.parseArray(jsonStr, Person.class);

        for (Person person:list){
            System.out.println(person);
        }

    }
}

class Person {
    private String name;
    private int age;

    public Person() {
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}