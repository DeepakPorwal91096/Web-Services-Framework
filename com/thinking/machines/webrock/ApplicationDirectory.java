package com.thinking.machines.webrock;
import java.io.*;
public class ApplicationDirectory implements java.io.Serializable
{
private File directory;
public ApplicationDirectory()
{
this.directory=null;
}
public ApplicationDirectory(File directory)
{
this.directory=directory;
}
public void setAttribute(File directory)
{
this.directory=directory;
}
public File getAttribute()
{
return this.directory;
}
}