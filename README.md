# mini-AQS-Lock

手写一个简单的ReentrantLock

blog链接：

- 涉及知识点：AQS，ReentrantLock，CAS，JUC，Atomic
- 文件结构：


```powershell
mini-AQS-Lock
├── src
│   ├── main
│      ├── java
│      │   └── com.example.miniaqslock
│      │       ├── MyLock #核心实现
│      │       ├── MiniAqsLockApplication.java #mian函数，跑样例
│      │    
│      └── resources
│          └── application.yml
├── pom.xml
```

- 核心Mylock分析

```powershell
MiniScheduleApplication.java
├──基础参数
│	//原子flag,判断状态; owner,判断加锁对象; 原子引用head和tail,维护链表
│
├──lock()方法
│	├──if判断(注释掉就是公平锁)：不需要排队，空闲，拿到就是你的
│	├──添加到尾节点排队：涉及链表尾插，保证拿到最新尾节点需要CAS操作
│   └──阻塞和阻塞唤醒：顺序优化，先自己if看能不能拿锁，再Park阻塞，防止一直阻塞
│
├──unlock()方法
│	├──if判断：是否是加锁线程
│	├──zx设置flag为false空闲
│   └──进行阻塞唤醒head的next(唤醒后会执行lock里park的剩余部分)
│
│──Node类
	├──Thread
	├──Node pre
    └──Node next
```

- 执行结果：基本符合
  - 跑一个10个线程，1线程给count--10次，最终为0
  - TIP：这里是非公平锁，只是因为给每个线程sleep了(结果直观)，所以看着像公平

```powershell
Thread-1成功添加到队列尾
Thread-0成功拿到锁
Thread-5成功添加到队列尾
Thread-7成功添加到队列尾
Thread-3成功添加到队列尾
Thread-4成功添加到队列尾
Thread-2成功添加到队列尾
Thread-6成功添加到队列尾
Thread-8成功添加到队列尾
Thread-9成功添加到队列尾
Thread-0唤醒了Thread-1
Thread-1被唤醒了，成功拿到锁
Thread-1唤醒了Thread-5
Thread-5被唤醒了，成功拿到锁
Thread-5唤醒了Thread-3
Thread-3被唤醒了，成功拿到锁
Thread-3唤醒了Thread-4
Thread-4被唤醒了，成功拿到锁
Thread-4唤醒了Thread-2
Thread-2被唤醒了，成功拿到锁
Thread-2唤醒了Thread-6
Thread-6被唤醒了，成功拿到锁
Thread-6唤醒了Thread-7
Thread-7被唤醒了，成功拿到锁
Thread-7唤醒了Thread-8
Thread-8被唤醒了，成功拿到锁
Thread-8唤醒了Thread-9
Thread-9被唤醒了，成功拿到锁
0
```



遗憾/代办：未实现可重入