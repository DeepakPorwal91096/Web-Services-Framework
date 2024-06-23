package com.thinking.machines.webrock;
public class TMForward implements java.io.Serializable
{
private String str;
public TMForward(String str)
{
this.str=str;
}
public String getForwardString()
{
return this.str;
}
}