package com.thinking.machines.webrock.pojo;
import java.lang.reflect.*;
import java.util.*;
public class ReturnPojo implements java.io.Serializable
{
private boolean success;
private boolean isException;
private boolean isReturnSomething;
private Object result;
private String exception;
public ReturnPojo()
{
this.exception="";
this.success=false;
this.isException=false;
this.isReturnSomething=false;
this.result=null;
}
public void setException(String exception)
{
this.exception=exception;
}
public String getException()
{
return this.exception;
}
public void setSuccess(boolean success)
{
this.success=success;
}
public boolean getSuccess()
{
return this.success;
}
public void setIsException(boolean isException)
{
this.isException=isException;
}
public boolean getIsException()
{
return this.isException;
}
public void setIsReturnSomething(boolean isReturnSomething)
{
this.isReturnSomething=isReturnSomething;
}
public boolean getIsReturnSomething()
{
return this.isReturnSomething;
}
public void setResult(java.lang.Object result)
{
this.result=result;
}
public java.lang.Object getResult()
{
return this.result;
}

}