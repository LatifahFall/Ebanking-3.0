package com.bank.graphql_gateway.model;

public class CreateUserInput {

    private String login;
    private String email;
    private String password;
    private String fname;
    private String lname;
    private String role;

    // getters & setters
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFname() { return fname; }
    public void setFname(String fname) { this.fname = fname; }

    public String getLname() { return lname; }
    public void setLname(String lname) { this.lname = lname; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
