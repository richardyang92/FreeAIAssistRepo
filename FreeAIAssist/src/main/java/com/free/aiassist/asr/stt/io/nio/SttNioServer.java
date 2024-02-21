package com.free.aiassist.asr.stt.io.nio;

import com.free.aiassist.asr.stt.engine.SttConfig;
import com.free.aiassist.asr.stt.io.SttByteBuffer;
import com.free.aiassist.asr.stt.io.SttException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class SttNioServer implements SttNioHandler {
    @Getter
    private final int bindPort;
    protected SttByteBuffer sttByteBuffer;
    protected SttNioInput sttNioInput;
    protected SttNioOutput sttNioOutput;
    protected SttConfig sttConfig;

    private ServerSocketChannel sttChannel;
    private Selector sttSelector;
    protected SttWriter sttWriter;

    @Getter
    private boolean interrupt;
    @Getter
    private boolean open;

    protected Lock sttLock;
    protected Condition writeCond;
    protected Condition readCond;
    private final Semaphore sttBufferSem;

    public SttNioServer(int bindPort,
                        SttNioInput sttNioInput,
                        SttNioOutput sttNioOutput,
                        SttConfig sttConfig) {
        this.bindPort = bindPort;
        this.sttNioInput = sttNioInput;
        this.sttNioOutput = sttNioOutput;
        this.sttConfig = sttConfig;
        this.interrupt = false;
        this.open = false;

        this.sttLock = new ReentrantLock();
        this.writeCond = this.sttLock.newCondition();
        this.readCond = this.sttLock.newCondition();
        this.sttBufferSem = new Semaphore(1);

        int sampleRate = sttConfig.getSampleRate();
        int channels = sttConfig.getChannels();
        int sampleFormat = sttConfig.getSampleFormat();
        int bufferSizeBase = sampleRate * channels * sampleFormat / 8;
        this.sttByteBuffer = new SttByteBuffer(bufferSizeBase * 8);
        this.sttWriter = new SttWriter();
    }

    public void start() throws SttException {
        try {
            this.sttSelector = Selector.open();
            this.sttChannel = ServerSocketChannel.open();
            this.sttChannel.configureBlocking(false);
            this.sttChannel.bind(new InetSocketAddress(this.bindPort));
            this.sttChannel.register(this.sttSelector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            throw new SttException(e);
        }

        this.open = true;

        Thread server = new Thread(() -> {
            while (!this.interrupt) {
                try {
                    int selectKeyCount = this.sttSelector.select();
                    if (selectKeyCount == 0) continue;
                    Iterator<SelectionKey> keyIter = this.sttSelector.selectedKeys().iterator();
                    while (keyIter.hasNext()) {
                        SelectionKey key = keyIter.next();
                        this.sttWriter.setSttKey(key);
                        if (key.isValid()) {
                            if (key.isAcceptable()) {
                                handleAccept(key);
                            } else if (key.isReadable()) {
                                handleRead(key);
                            } else if (key.isWritable()) {
                                handleWrite(key);
                            }
                        }
                        keyIter.remove();
                    }
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage());
                }
            }
        });
        server.start();
    }

    public void stop() throws SttException {
        this.open = false;
        this.interrupt = true;
        try {
            this.sttChannel.close();
            this.sttSelector.close();
        } catch (IOException e) {
            throw new SttException(e);
        }
    }

    protected int getSttBufferSize() throws SttException {
        int size;
        try {
            this.sttBufferSem.acquire();
            size = this.sttByteBuffer.size();
            this.sttBufferSem.release();
        } catch (InterruptedException e) {
            throw new SttException(e);
        }
        return size;
    }

    @Override
    public void handleAccept(SelectionKey key) throws SttException {
        log.info("accept: {}", key);
        try {
            SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(key.selector(), SelectionKey.OP_READ);
        } catch (IOException e) {
            throw new SttException(e);
        }
    }

    @Override
    public void handleRead(SelectionKey key) throws SttException {
//        log.info("read: {}", key);
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
        try {
            int count = socketChannel.read(byteBuffer);
            if (count == -1) {
                socketChannel.close();
                key.cancel();
            }
            byteBuffer.flip();
            this.sttLock.lock();
            try {
                if (count > 0) {
                    int size = getSttBufferSize();
                    byte[] bytes = new byte[count];
                    System.arraycopy(byteBuffer.array(), 0, bytes, 0, count);
//                    byte[] bytes = byteBuffer.array();
                    if (size + count > this.sttByteBuffer.capacity()) {
                        this.writeCond.await();
                    }
//                    log.info("read size={}", count);
                    this.sttNioInput.produce(bytes, this.sttByteBuffer);
                    this.readCond.signalAll();
                } else {
                    log.warn("count = 0");
                }
            } catch (InterruptedException e) {
                log.error(e.getLocalizedMessage());
            } finally {
                this.sttLock.unlock();
            }
            byteBuffer.clear();
        } catch (IOException e) {
            throw new SttException(e);
        }
    }

    @Override
    public void handleWrite(SelectionKey key) throws SttException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer writeBuffer = (ByteBuffer) key.attachment();
        try {
            int writeLength = socketChannel.write(writeBuffer);
//            log.info("send message to client. client:{} message length:{}", socketChannel.getRemoteAddress(), writeLength);
            if (!writeBuffer.hasRemaining()) {
                // 写完数据后，要把写事件取消，否则当写缓冲区有剩余空间时，会一直触发写事件
                key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
                // socketChannel.shutdownOutput(); // channel调用shutdownOutput()后，会停止触发写事件
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract static class AbstractBuilder<T extends SttNioServer> {
        protected int bindPort;
        protected SttNioInput sttNioInput;
        protected SttNioOutput sttNioOutput;
        protected SttConfig sttConfig;

        public AbstractBuilder<T> bindPort(int bindPort) {
            this.bindPort = bindPort;
            return this;
        }

        public AbstractBuilder<T> setNioInput(SttNioInput sttNioInput) {
            this.sttNioInput = sttNioInput;
            return this;
        }

        public AbstractBuilder<T> setNioOutput(SttNioOutput sttNioOutput) {
            this.sttNioOutput = sttNioOutput;
            return this;
        }

        public AbstractBuilder<T> setSttConfig(SttConfig sttConfig) {
            this.sttConfig = sttConfig;
            return this;
        }

        public abstract T build();
    }

    @Setter
    protected static class SttWriter {
        private SelectionKey sttKey;

        public void write(String stt) {
            byte[] sttBytes = stt.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(sttBytes.length);
            writeBuffer.put(sttBytes);
            // 读完数据后，为 SelectionKey 注册可写事件
            if (!this.sttKey.isValid()) {
                log.warn("invalid key");
                return;
            }
            if (!isInterest(this.sttKey, SelectionKey.OP_WRITE)) {
                this.sttKey.interestOps(this.sttKey.interestOps() + SelectionKey.OP_WRITE);
            }
            writeBuffer.flip();
            this.sttKey.attach(writeBuffer);
        }

        private static boolean isInterest(SelectionKey selectionKey, int event) {
            int interestSet = selectionKey.interestOps();
            return (interestSet & event) == event;
        }
    }
}
