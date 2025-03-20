package com.example.miniaqslock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class MyLock {
    AtomicBoolean flag = new AtomicBoolean(false);
    Thread owner = null;
    //多线程环境下普通Node没法原子操作，会出现很多问题，AtomicReference<T>原子引用
    AtomicReference<Node> head = new AtomicReference<>(new Node());
    AtomicReference<Node> tail = new AtomicReference<>(head.get());
    void lock(){
        //把这段代码注释掉就是公平锁，只要lock就要排队
        if(flag.compareAndSet(false, true)){
            System.out.println(Thread.currentThread().getName()+"成功拿到锁");
            owner=Thread.currentThread();
            return;//成功拿到锁，返回
        }

        //没达到锁，把自己添加到尾节点(线程安全)
        Node curNode = new Node();
        curNode.thread=Thread.currentThread();
        while(true){//while保证拿到的是最新的尾节点
            Node curTail = tail.get();//类似于temp，保存之前的尾节点
            if(tail.compareAndSet(curTail, curNode)){//CAS操作将尾节点变成自己
                System.out.println(Thread.currentThread().getName()+"成功添加到队列尾");
                //修改前驱后驱逻辑
                curNode.pre=curTail;
                curTail.next=curNode;
                break;
            }
        }

        //阻塞自己防止一直自旋消耗资源
        while (true){
            //LockSupport.park();优化见下
            //如果阻塞被唤醒会接着park()后面的逻辑
            //并且被唤醒了就说明拿到锁了，为了防止虚假唤醒，所以需要while和一些条件才能返回
                //head->A->B->C，唤醒后head肯定就变成了A，下一次唤醒同理
                //curNode.pre==head.get说明是真唤醒了轮到curNode了
            if(curNode.pre == head.get() && flag.compareAndSet(false, true)){
                //成功获取锁后的逻辑，修改owner，头节点
                owner = Thread.currentThread();
                head.set(curNode);//执行这个操作一定是持有锁时，线程安全的
                //head已经成A了，断开原有的head->A和head<-A
                curNode.pre.next = null;
                curNode.pre = null;
                System.out.println(Thread.currentThread().getName()+"被唤醒了，成功拿到锁");
                return;
            }
            //优化，先自己判断下能不能拿到锁，如果不能，就说明真的需要别人来唤醒了
            LockSupport.park();
        }
    }

    void unlock(){
        if(owner!=Thread.currentThread()){
            throw new IllegalStateException("不是当前线程，不能解锁");
        }
        //走到这一步说明已经拿到锁了,不需要CAS
        //lock里拿到锁就两种情况，来就拿到没进队列，队列排队拿到
        //这两种情况，我们都只需要唤醒head节点的->即可
        Node headNode = head.get();
        Node next = headNode.next;
        flag.set(false);
        if(next!=null){
            System.out.println(Thread.currentThread().getName()+"唤醒了"+next.thread.getName());
            LockSupport.unpark(next.thread);
            //如果我们把next唤醒了，去执行原本先park再if的逻辑，如果if失败，就一直阻塞了，没人唤醒
            //所以改下顺序，先自己if再park(见上)
        }
    }

    class Node{
        Thread thread;
        Node pre;
        Node next;

    }
}
