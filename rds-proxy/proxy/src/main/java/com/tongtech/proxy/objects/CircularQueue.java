package com.tongtech.proxy.objects;

import java.util.Arrays;

public class CircularQueue<T> {
    private final Object[] elements;
    private final int capacity;
    private int head; // 头指针
    private int tail; // 尾指针

    public CircularQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.capacity = capacity;
        this.elements = new Object[capacity];
    }

    public void enqueue(T item) {
        if (elements[tail] != null) { // 队列已满，需要覆盖最早的元素
            elements[head] = item;
            incrementHead(); // 移动头指针
        } else {
            elements[tail] = item;
            incrementTail(); // 移动尾指针
        }
    }

    private void incrementHead() {
        head = (head + 1) % capacity;
    }

    private void incrementTail() {
        tail = (tail + 1) % capacity;
    }

    public T dequeue() {
        if (isEmpty()) {
            return null;
        }
        T result = (T) elements[head];
        elements[head] = null; // 清空已出队元素
        incrementHead(); // 移动头指针
        return result;
    }

    public boolean isEmpty() {
        return elements[head] == null && head == tail;
    }

    public int size() {
        if (isEmpty()) {
            return 0;
        }
        return (tail - head + capacity) % capacity; // 考虑环形队列的情况计算元素数量
    }

    // 提供一个便捷的方法查看当前队列的第一个元素（如果不为空）
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return (T) elements[head];
    }

    public void clear(){
        Arrays.fill(elements, null);
        head = 0;
        tail = 0;
    }


    public static void main(String[] args) {
        CircularQueue<Integer> circularQueue = new CircularQueue<>(10);

        for (int i = 0; i < 20; i++) {
            circularQueue.enqueue(i);
        }

        while (!circularQueue.isEmpty()) {
            System.out.println(circularQueue.dequeue());
        }
    }

}

// 测试代码保持不变

