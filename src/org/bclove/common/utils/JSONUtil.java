package org.bclove.common.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * JSON工具类
 * @author soryokurin
 *
 */
public class JSONUtil {
	
	public static Map <String ,String>jsonToMap(String json) throws ParseException{
		if(json==null){
			return null;
		}
		JSONObject jo = new JSONObject(json);
		Map <String ,String>map = new HashMap<String,String>();
		for ( Iterator keys = jo.keys(); keys.hasNext(); ) {
    		String key = (String)keys.next();
    		map.put(key, jo.get(key).toString());
    	}
		return map;
	}
	
	/**
	 * @param json
	 * 将json数组的字符串转化成以map为元素的list,根据map的equals()去重
	 * @return
	 */
	public static List jsonArrayToMapList(String json){
		if(json==null){
			return null;
		}
		List list =null;
		try {
			JSONArray ja =null;
			if(json.startsWith("[")&&json.endsWith("]")){
				ja = new JSONArray(json);
			}
			else{
				ja = new JSONArray("["+json+"]");
			}
			list =  new ArrayList();
			for (int i = 0; i < ja.length(); i++) {
				JSONObject o = ja.getJSONObject(i);
				Map map = jsonToMap(o.toString());
				if(!list.contains(map)){
					list.add(map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static String MapListToJSON(List<Map> mapList){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mapList.size(); i++) {
			Map map = mapList.get(i);
			sb.append(mapToJSON(map));
		}
		return sb.toString();
	}
	
	public static String mapToJSON(Map map){
		return new JSONObject(map).toString();
	}
	public static void main(String[] arg) throws Exception{
		String json=null;
		Map map = jsonToMap(json);
	}
}
