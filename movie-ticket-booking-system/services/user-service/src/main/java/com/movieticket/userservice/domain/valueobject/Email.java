package userservice.domain.valueobject;

import java.util.Objects;

public class Email {

    private final String value;

    public Email(String value){

        Objects.requireNonNull(value);

        if(!value.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        )){
            throw new IllegalArgumentException(
                    "Invalid email");
        }

        this.value = value;
    }

    public String getValue(){
        return value;
    }
}