package com.thinking.machines.webrock.annotations;
import java.lang.annotation.*;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Forward{
String value();
}