package study.querydsl.generic;

import java.util.ArrayList;
import java.util.List;

public class Course<T> {
    private String name;
    private List<T> students = new ArrayList<>();

    public Course(String name, List<T> students) {
        this.name = name;
        this.students = students;
    }
}
