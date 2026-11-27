package com.library.listener;

import jakarta.servlet.SessionCookieConfig;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class SessionCookieSecurityTest {
    @Test void productionCookieIsSecureHttpOnlyAndLax() {
        Map<String,Object> values=config(true);
        assertEquals(true,values.get("httpOnly"));assertEquals(true,values.get("secure"));assertEquals("Lax",values.get("SameSite"));assertEquals("JSESSIONID",values.get("name"));
    }
    @Test void localHttpCookieRemainsUsable() {
        Map<String,Object> values=config(false);assertEquals(false,values.get("secure"));assertEquals(true,values.get("httpOnly"));assertEquals("Lax",values.get("SameSite"));
    }
    private Map<String,Object> config(boolean production){
        Map<String,Object> values=new HashMap<>();
        SessionCookieConfig cookie=(SessionCookieConfig)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{SessionCookieConfig.class},(proxy,method,args)->{
            if(method.getName().equals("setAttribute"))values.put((String)args[0],args[1]);
            else if(method.getName().startsWith("set"))values.put(Character.toLowerCase(method.getName().charAt(3))+method.getName().substring(4),args[0]);
            return null;
        });
        LibraryContextListener.configureSessionCookie(cookie,production);return values;
    }
}
