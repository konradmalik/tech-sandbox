package io.github.konradmalik.rest.library.server;

public class Author implements java.io.Serializable
{
    private String name;

    public Author() {}

    public Author(String name) { setName(name); }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
}