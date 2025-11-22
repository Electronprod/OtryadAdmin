package ru.electronprod.OtryadAdmin.utils;

import lombok.Getter;

public class IntPair {
	@Getter
	int first;
	@Getter
	int second;

	public IntPair(int first, int second) {
		this.first = first;
		this.second = second;
	}

	public void add(IntPair other) {
		this.first += other.first;
		this.second += other.second;
	}

	public void addFirst(int first) {
		this.first += first;
	}

	public void addSecond(int second) {
		this.second += second;
	}
}