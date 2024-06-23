package com.thinking.machines.webrock;
public class TMRedirect implements java.io.Serializable
{
private String str;
public TMRedirect(String str)
{
this.str=str;
}
public String getRedirectString()
{
return this.str;
}
}