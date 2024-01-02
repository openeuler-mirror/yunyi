package com.tongtech.proxy.core.utils;


public class StableVector {

	private final int Datas[];

	private final int Capacity;

	private int Size = 0;

	public StableVector(int capacity) {
		Datas = new int[capacity];
		Capacity = capacity;
	}

	public synchronized void add(int i) throws TooManyRowsException {
		if (Size < Capacity) {
			Datas[Size++] = i;
		} else {
			throw new TooManyRowsException("data is too large");
		}
	}

	public synchronized int get(int p) throws IllegalArgumentException {
		if (p >= 0 && p < Size) {
			return Datas[p];
		} else {
			throw new IllegalArgumentException("input position is error");
		}
	}

	public synchronized void clear() {
		Size = 0;
	}

	public synchronized int size() {
		return Size;
	}

	public int capacity() {
		return Datas.length;
	}
}
