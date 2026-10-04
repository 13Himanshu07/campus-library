package com.library.model;

public final class Student extends User {
    public Student(){setRole(Role.STUDENT);setStatus(Status.ACTIVE);}
    public Student(long id,String name,String email,String hash,String phone,String membership,Status status){super(id,name,email,hash,phone,membership,Role.STUDENT,status);}
    @Override public String getDashboardType(){return "student";}
    @Override public String getDisplayLabel(){return getName()+" · Student";}
}
