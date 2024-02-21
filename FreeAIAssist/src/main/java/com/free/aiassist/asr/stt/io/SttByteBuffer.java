package com.free.aiassist.asr.stt.io;

import lombok.extern.slf4j.Slf4j;

import javax.management.ConstructorParameters;

@Slf4j
public class SttByteBuffer implements SttBuffer<byte[]> {
    private byte[] data;
    private final int capacity;

    private int rear;
    private int front;

    @ConstructorParameters({"capacity", "portId"})
    public SttByteBuffer(int capacity) {
        this.capacity = capacity;

        if (capacity > 0) {
            this.data = new byte[capacity];
        }
        this.rear = 0;
        this.front = 0;
    }

    @Override
    public synchronized int write(byte[] in, int len) {
        int remainLen = this.capacity - size();
        if (remainLen < len) return 0;
        int counter = 0;
        if (this.rear + len > this.capacity) {
            int partLen = this.capacity - this.rear;
            for (int i = 0; i < partLen; i++) {
                this.data[this.rear++] = in[i];
                counter++;
            }
            this.rear = 0;
            for (int i = 0; i < len - partLen; i++) {
                this.data[this.rear++] = in[i + partLen];
                counter++;
            }
        } else {
            for (int i = 0; i < len; i++) {
                this.data[this.rear++] = in[i];
                counter++;
            }
        }
        return counter;
    }

    @Override
    public synchronized int read(byte[] out, int len) {
        int dataLen = size();
        if (dataLen == 0) return -1;
        if (dataLen < len) len = dataLen;
        int counter = 0;
        if (this.front + len > this.capacity) {
            int partLen = this.capacity - this.front;
            for (int i = 0; i < partLen; i++) {
                out[i] = this.data[this.front];
                this.data[this.front] = 0;
                this.front++;
                counter++;
            }
            this.front = 0;
            for (int i = 0; i < len - partLen; i++) {
                out[i + partLen] = this.data[this.front];
                this.data[this.front] = 0;
                this.front++;
                counter++;
            }
        } else {
            for (int i = 0; i < len; i++) {
                out[i] = this.data[this.front];
                this.data[this.front] = 0;
                this.front++;
                counter++;
            }
        }
        return counter;
    }

    @Override
    public synchronized int size() {
        int dataLen;
        if (this.rear >= this.front) {
            dataLen = this.rear - this.front;
        } else {
            dataLen = this.capacity - (this.front - this.rear);
        }
        if (dataLen == 0) {
            boolean clean = true;
            for (int i = 0; i < this.capacity; i++) {
                if (this.data[i] != 0) {
                    clean = false;
                    break;
                }
            }
            if (!clean) dataLen = this.capacity;
        }
        return dataLen;
    }

    @Override
    public synchronized int capacity() {
        return this.capacity;
    }

    public void dump() {
        for (int i = 0; i < this.capacity; i++) {
            System.out.print(data[i] + " ");
        }
        System.out.println();
    }
}
