package com.library.util;

import com.library.exception.ValidationException;
import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Pattern EMAIL=Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private ValidationUtil(){ }
    public static String required(String value,String label){String v=value==null?"":value.trim();if(v.isEmpty())throw new ValidationException(label+" is required.");return v;}
    public static String required(String value,String label,int maxLength){String v=required(value,label);if(v.length()>maxLength)throw new ValidationException(label+" must be at most "+maxLength+" characters.");return v;}
    public static String email(String value){String v=required(value,"Email").toLowerCase();if(!EMAIL.matcher(v).matches())throw new ValidationException("Enter a valid email address.");return v;}
    public static int nonNegative(int value,String label){if(value<0)throw new ValidationException(label+" cannot be negative.");return value;}
    public static long id(String value,String label){try{long id=Long.parseLong(value);if(id<=0)throw new NumberFormatException();return id;}catch(Exception e){throw new ValidationException("Choose a valid "+label.toLowerCase()+".");}}
}
