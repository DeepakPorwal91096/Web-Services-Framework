package com.thinking.machines.webrock.model;
import java.util.*;
import com.thinking.machines.webrock.pojo.*;
import java.lang.reflect.*;
public class WebRockModel
{
private Map<String,Service> services;
private Map<String,HashMap<String,Field>> autoWiredMap;
public WebRockModel()
{
services=new HashMap<>();
autoWiredMap=new HashMap<>();
}
public void addToAutoWired(String key,HashMap<String,Field> value)
{
this.autoWiredMap.put(key,value);
}
public HashMap<String,Field> getFromAutoWired(String key)
{
return this.autoWiredMap.get(key);
}

public void add(String key,Service object)
{
this.services.put(key,object);
}
public void update(String key,Service object)
{
this.services.replace(key,object);
}
public void delete(String key)
{
this.services.remove(key);
}
public Service get(String key)
{
return this.services.get(key);
}
public int size()
{
return this.services.size();
}
public void clear()
{
this.services.clear();
}
}