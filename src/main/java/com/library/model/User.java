package com.library.model;

import java.io.Serializable;

public abstract class User implements Serializable {
    private long id;
    private String name;
    private String email;
    private String passwordHash;
    private String phone;
    private String membershipId;
    private Role role;
    private Status status;

    protected User() { }
    protected User(long id, String name, String email, String passwordHash, String phone,
                   String membershipId, Role role, Status status) {
        this.id=id; this.name=name; this.email=email; this.passwordHash=passwordHash;
        this.phone=phone; this.membershipId=membershipId; this.role=role; this.status=status;
    }
    public abstract String getDashboardType();
    public String getDisplayLabel() { return name + " (" + role + ")"; }
    public long getId(){return id;} public void setId(long v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String v){passwordHash=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public String getMembershipId(){return membershipId;} public void setMembershipId(String v){membershipId=v;}
    public Role getRole(){return role;} public void setRole(Role v){role=v;}
    public Status getStatus(){return status;} public void setStatus(Status v){status=v;}
    public enum Role { STUDENT, LIBRARIAN, ADMIN }
    public enum Status { ACTIVE, PENDING, DISABLED }
}
