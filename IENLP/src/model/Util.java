package model;

/**
 * Created by peterwessels on 01/03/2017.
 */
public class Util {
    public static int countSubstring(String subStr, String str){
        return (str.length() - str.replace(subStr, "").length()) / subStr.length();
    }
}
