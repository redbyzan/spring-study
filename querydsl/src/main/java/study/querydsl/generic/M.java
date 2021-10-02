package study.querydsl.generic;

import java.util.ArrayList;
import java.util.List;

public class M {
    private Integer num;
    private String name;


    @Override
    public String toString() {
        return super.toString();
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void main(String[] args) {
        M m = new M();
        System.out.println(m.getNum().toString());

    }
}
