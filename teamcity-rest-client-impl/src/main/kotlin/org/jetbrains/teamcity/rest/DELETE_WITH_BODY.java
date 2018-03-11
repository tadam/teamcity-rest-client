package org.jetbrains.teamcity.rest;

import retrofit2.http.HTTP;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author nik
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface DELETE_WITH_BODY {
  @HTTP(method = "DELETE", hasBody = true)
  String value();
}
