package com.obel.miniurl.dao;

import org.springframework.stereotype.Component;

@Component
public class MiniUrlAutoGenerator {
	private int counter = 0;
	
	public synchronized String getNextMiniUrl() {
		return String.valueOf(++counter);
	}
	
	public synchronized void reset() {
		counter = 0;
	}
}
