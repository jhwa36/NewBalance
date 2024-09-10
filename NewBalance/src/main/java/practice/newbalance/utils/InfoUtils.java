package practice.newbalance.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InfoUtils {
    public Map<String, String> getPrefixPhoneNumber(){

        Map<String, String> prefixNumber = new LinkedHashMap<>();
        prefixNumber.put("010", "010");
        prefixNumber.put("011", "011");
        prefixNumber.put("017", "017");
        prefixNumber.put("016", "016");
        prefixNumber.put("019", "019");

        return prefixNumber;
    }

    public List<String> getEmail(){
        List<String> emailList = new ArrayList<>();
        emailList.add("직접선택");
        emailList.add("naver.com");
        emailList.add("gmail.com");
        emailList.add("hanmail.net");
        emailList.add("nate.com");
        return emailList;
    }
}
