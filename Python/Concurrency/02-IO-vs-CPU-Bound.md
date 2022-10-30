## _CPU Bound_
When your code is highly dependent on CPU or is CPU intensive you can call it as CPU Bounded. This means that other components of the system like network adapters, disk etc are rarely used/accessed by your code.

Consider the recursive fibonacci code that we used before in [Trying to run concurrent code](./01-Trying-To-Run-Concurrent-Code.md), it is said to be CPU bound and from the benchmarks we did by employing multuithreading we realized that the code was not executed concurrently but rather sequentially due to the underlying Global Interpretation Lock (GIL). We also mentioned that the GIL is relaxed when the code is I/O bound. To dig more deeper let us consider the example below:

### _Sequential Code_
```python
import requests
import time

def get_status_code():
    response = requests.get(url='https://pokeapi.co/api/v2/pokemon/ditto')
    status_code = response.status_code
    return

start_time = time.time()

get_status_code()
get_status_code()
get_status_code()

end_time = time.time()
print(f'Overall time: {end_time - start_time} seconds!')
#Overall time: 2.001796007156372 seconds!
```

### _Concurrect Code_
```python
import requests
import threading
import time

def get_status_code():
    response = requests.get(url='https://pokeapi.co/api/v2/pokemon/ditto')
    status_code = response.status_code
    return

th1 = threading.Thread(target=get_status_code)
th2 = threading.Thread(target=get_status_code)
th3 = threading.Thread(target=get_status_code)

start_time = time.time()

th1.start()
th2.start()
th3.start()

th1.join()
th2.join()
th3.join()

end_time = time.time()
print(f'Overall time: {end_time - start_time} seconds!')
#Overall time: 0.4923217296600342 seconds!
```

With our benchmarks we see that concurrent code took only 1/4th of the time what sequential code took. Let us understand how we did it and what made the cuncurrency to be working in this case. The flow:

1. When we created a new thread for each call and started them, each thread creates its own instruction set with respect to **_get_status_code_** method and waits for the OS to be executed.
2. Since the code is I/O bound i.e a network call which will be performed by operating system, a handoff is given to OS for performing this task. In mean time the threads created can be used to do other work.
3. Once OS is done with the network call, it notifies the caller via its event notificiation system to execute further by taking the response.

**Note**<br/>
Every operating system have its own event notification system: Example include:
1. **_kqueue_** - For FreeBSD and MacOS
2. **_epoll_** - Linux
3. **_I/O Completion Port (IOCP)_** - Windows

Libraries such as **_asyncio_** exploits the fact that I/O operations release the GIL lock to give us concurrency even with only single thread and gives the developer the ability to write single threaded concurrency code.