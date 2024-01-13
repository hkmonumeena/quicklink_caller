package com.ruchitech.quicklinkcaller.persistence.foreground_notification;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ForegroundServiceInfo {
    int res() default 0;

    String value();
}