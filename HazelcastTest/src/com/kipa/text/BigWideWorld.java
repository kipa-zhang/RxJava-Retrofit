package com.kipa.text;

import java.util.Random;

/**
 * @author Roger
 * 
 */
public class BigWideWorld {

	private static Random rand = new Random(System.currentTimeMillis());

	private final Users users = new Users();

	private final int totalNumUsers = users.size();

	public String nextUser() {

		User user = users.get(rand.nextInt(totalNumUsers));
		String name = user.getUsername();

		return name;

	}

}
