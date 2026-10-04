package com.library.model;

public final class Librarian extends User {
    public Librarian(){setRole(Role.LIBRARIAN);setStatus(Status.ACTIVE);}
    public Librarian(long id,String name,String email,String hash,String phone,String membership,Role role,Status status){super(id,name,email,hash,phone,membership,role,status);}
    @Override public String getDashboardType(){return getRole()==Role.ADMIN ? "admin" : "librarian";}
    @Override public String getDisplayLabel(){return getName()+" · "+(getRole()==Role.ADMIN?"Administrator":"Librarian");}
}
