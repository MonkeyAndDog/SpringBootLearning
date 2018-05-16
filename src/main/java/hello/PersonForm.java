package hello;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PersonForm {

    @Min(18)
    private int age;

    @NotNull
    @Size(max = 20, min = 6)
    private String name;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "PersonForm{" +
                "age=" + age +
                ", name='" + name + '\'' +
                '}';
    }
}
