## _Trying to run python code concurrently ?_

Python's way of supporting concurrency is ambiguous. Due to GIL (Global Interpretation Lock) even with the use of threading module it is not possible to achieve true concurrency if the code is CPU bound and in case of I/O bound GIL lock is relaxed and concurrency is achieved. For now, lets try to see if there is any difference in our simple benchmarks when we run CPU bound code with and without threading support.

Let us first consider the below fibonacci code and try to run it sequentially and also concurrently.

#### The code snippet
---
```python
def fib(n: int) -> int:
	if n == 1:
		return 0
	if n == 2:
		return 1
	else:
		return fib(n-1) + fib(n-2)
```

#### Running it sequential
---
```python
import time

# Start of sequential execution
start_time = time.time()

# Running two function calls sequential
fib(40)
fib(41)

# Ending sequential execution
end_time = time.time()

print(f'Overall Time: {end_time - start_time} seconds!')
#Overall Time: 37.46045112609863 seconds!
```

#### Running it sequential
---
```python
import time
import threading

thread_1 = threading.Thread(target=fib, args=(40,))
thread_2 = threading.Thread(target=fib, args=(41,))

# Start threads
thread_1.start()
thread_2.start()

start_time = time.time()

thread_1.join()
thread_2.join()

end_time = time.time()

print(f'Overall Time: {end_time - start_time} seconds!')
#Overall Time: 38.87280172918209 seconds!
```

When we tried the sequential code with multi-threaded version we get output with almost the same time, this is because the above code is CPU bound. As stated before CPU bound code gets executed sequentially (even if you are running your code in multi-core system) due to GIL lock.