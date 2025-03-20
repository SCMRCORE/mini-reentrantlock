package com.example.miniaqslock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootApplication
public class MiniAqsLockApplication {

    public static void main(String[] args) throws InterruptedException {
        int[] count = new int[]{100};
        List<Thread> threads = new ArrayList<>();
        MyLock lock = new MyLock();
        for (int i = 0; i < 10; i++) {
            threads.add(new Thread(()->{
                lock.lock();
                {
                    for (int i1 = 0; i1 < 10; i1++) {
                        try {
                            Thread.sleep(2);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        count[0]--;
                    }
                }
                lock.unlock();
            }));
        }

        for(Thread thread:threads) thread.start();
        for(Thread thread:threads) thread.join();
        //这里不论是sleep还是join都是为了防止主线程执行太快，子线程任务还没执行完，主线程先结束，整个应用程序结束
        System.out.println(count[0]);
    }

}
