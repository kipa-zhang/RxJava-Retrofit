package com.kipa.text;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class Test {

	public static void main(String[] args) {
		//没法直接使用，必须开启服务端的 hazelcast (start.bat)或者使用 Test2 类中的 Client
		ClientConfig clientConfig = new ClientConfig();
        HazelcastInstance client = HazelcastClient.newHazelcastClient( clientConfig );
        IMap map = client.getMap( "customers" );
        System.out.println( "Map Size:" + map.size() );
        
        map.set("name", "KIPA");
        System.out.println(map.get("st"));
	}
	
	
}
