package com.library;

import com.library.model.*;
import com.library.util.PasswordUtil;
import com.library.util.ValidationUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CoreJavaTest {
    @Test void roleFactoryModelDemonstratesOverriddenPolymorphicBehavior(){
        User user=new Student(7,"Asha","asha@campus.edu","hash",null,"S-7",User.Status.ACTIVE);
        assertEquals("student",user.getDashboardType());
        assertEquals("Asha · Student",user.getDisplayLabel());
        user=new Librarian(8,"Dev","dev@campus.edu","hash",null,"L-8",User.Role.LIBRARIAN,User.Status.ACTIVE);
        assertEquals("librarian",user.getDashboardType());
    }
    @Test void passwordHashIsNotStoredAsPlainTextAndCanBeVerified(){
        String hash=PasswordUtil.hash("LibraryDemo9!");
        assertNotEquals("LibraryDemo9!",hash);
        assertTrue(PasswordUtil.matches("LibraryDemo9!",hash));
        assertFalse(PasswordUtil.matches("wrong-password",hash));
        assertTrue(PasswordUtil.matches("LibraryDemo9!","$2a$10$SkewBWZgz3WteBLOZOu/GOIcifztzq9eN04n5nWkOgRyOZH3zhzZ."));
    }
    @Test void overloadedValidationChecksRequiredValueAndLength(){
        assertEquals("title",ValidationUtil.required(" title ","Title",10));
        assertThrows(RuntimeException.class,()->ValidationUtil.required("much too long","Title",4));
    }
}
