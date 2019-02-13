#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# filename: test_dt.py

import os
import time
import math
from functools import partial
import multiprocessing
import jieba

from utils import log, Stack, json, tqdm


INPUT_DIR = "../input/"
OUTPUT_DIR = "../output/"

TEST_FILE_1 = "围城.txt"

JAVA_EXT = ".java.json"
PY36_EXT = ".py36.json"
CUT_SUFFIX = ".cut"
CUTALL_SUFFIX = ".cutall"
CUT_FOR_SEARCH_SUFFIX = ".cutforsearch"
HMM_SUFFIX = ".HMM"
NO_HMM_SUFFIX = ".notHMM"


### I/O Methods ###

def read_input(filename):
    path = os.path.join(INPUT_DIR, filename)
    with open(path, "r", encoding="utf-8") as fp:
        return fp.read()

def write_json(obj, filename, suffix, ext=PY36_EXT, **kwargs):
    file = filename + suffix + ext
    with open(os.path.join(OUTPUT_DIR, file), "w", encoding="utf-8") as fp:
        json.dump(obj, fp, separators=(',',':'), ensure_ascii=False, **kwargs)

def read_json(filename, suffix, ext=PY36_EXT, **kwargs):
    file = filename + suffix + ext
    with open(os.path.join(OUTPUT_DIR, file), "r", encoding="utf-8") as fp:
        return json.load(fp, **kwargs)

### END I/O Methods ###


### General Methods ###

@log
def test(fn, filename, suffix, *args, **kwargs):
    print("function: %r" % fn.__name__)
    print("args = %s, kwargs = %s" % (args, kwargs))
    text = read_input(filename)
    segs = fn(text, *args, **kwargs)
    write_json(segs, filename, suffix)

@log
def multi_test(fn, filename, suffix, n, *args, **kwargs):
    print("function: %r" % fn.__name__)
    print("args = %s, kwargs = %s" % (args, kwargs))
    print("n = %s" % n)
    text = read_input(filename)
    for _ in tqdm(range(n)):
        segs = fn(text, *args, **kwargs)

def filter_space(filename, suffix): # 这回修改原始的 java 的输出数据
    for ext in [PY36_EXT, JAVA_EXT]:
        segs = read_json(filename, suffix, ext)
        segs = list(filter( lambda x: (x != "") and (not x.isspace()), segs ))
        write_json(segs, filename, suffix, ext)

@log
def analyse_results(filename, suffix):
    jRes = read_json(filename, suffix, JAVA_EXT)
    pRes = read_json(filename, suffix, PY36_EXT)
    jSet, pSet = set(jRes), set(pRes)
    s1 = jSet & pSet
    s2 = jSet | pSet
    print("jRes count:", len(jRes))
    print("pRes count:", len(pRes))
    print("jSet & pSet =", len(s1))
    print("jSet | pSet =", len(s2))
    print("score:", len(s1)/len(s2))

def task_test(fn, filename, suffix, n, *args, **kwargs):
    test(fn, filename, suffix, *args, **kwargs)
    filter_space(filename, suffix)
    analyse_results(filename, suffix)
    #multi_test(fn, filename, suffix, n, *args, **kwargs)

### END General Methods ###


### Test Functions ###

@log
def task_test_cut():
    #//////////////////////////////////////////#
    # --- TEST_FILE_1 ---
    # jRes count: 142607
    # pRes count: 142608
    # jSet & pSet = 18489
    # jSet | pSet = 18489
    # score: 1.0
    #//////////////////////////////////////////#
    # TEST_FILE_1, n = 100, cost: 143.859403 s #
    # TEST_FILE_1, n = 100, cost: 145.613091 s #
    # TEST_FILE_1, n = 100, cost: 159.842772 s #
    #//////////////////////////////////////////#
    filename = TEST_FILE_1
    suffix   = CUT_SUFFIX + HMM_SUFFIX
    fn       = jieba.lcut
    args     = ()
    kwargs   = {}
    n        = 100
    task_test(fn, filename, suffix, n, *args, **kwargs)

@log
def task_test_cutall():
    #//////////////////////////////////////////#
    # --- TEST_FILE_1 ---
    # jRes count: 131273
    # pRes count: 131273
    # jSet & pSet = 18604
    # jSet | pSet = 18604
    # score: 1.0
    #//////////////////////////////////////////#
    # TEST_FILE_1, n = 100, cost: 51.740311 s  #
    # TEST_FILE_1, n = 100, cost: 49.591669 s  #
    #//////////////////////////////////////////#
    filename = TEST_FILE_1
    suffix   = CUTALL_SUFFIX + HMM_SUFFIX
    fn       = jieba.lcut
    args     = ()
    kwargs   = {"cut_all": True}
    n        = 100
    task_test(fn, filename, suffix, n, *args, **kwargs)

@log
def task_test_cut_for_search():
    #//////////////////////////////////////////#
    # --- TEST_FILE_1 ---
    # jRes count: 152519
    # pRes count: 152520
    # jSet & pSet = 20202
    # jSet | pSet = 20202
    # score: 1.0
    #//////////////////////////////////////////#
    # TEST_FILE_1, n = 100, cost: 147.739258 s #
    # TEST_FILE_1, n = 100, cost: 161.897684 s #
    #//////////////////////////////////////////#
    filename = TEST_FILE_1
    suffix   = CUT_FOR_SEARCH_SUFFIX + HMM_SUFFIX
    fn       = jieba.lcut_for_search
    args     = ()
    kwargs   = {}
    n        = 100
    task_test(fn, filename, suffix, n, *args, **kwargs)

@log
def task_test_cut_noHMM():
    #//////////////////////////////////////////#
    # --- TEST_FILE_1 ---
    # jRes count: 151816
    # pRes count: 151819
    # jSet & pSet = 14620
    # jSet | pSet = 14623
    # score: 0.9997948437393148
    #//////////////////////////////////////////#
    # TEST_FILE_1, n = 100, cost: 93.955209 s  #
    #//////////////////////////////////////////#
    filename = TEST_FILE_1
    suffix   = CUT_SUFFIX + NO_HMM_SUFFIX
    fn       = jieba.lcut
    args     = ()
    kwargs   = {"HMM": False}
    n        = 100
    task_test(fn, filename, suffix, n, *args, **kwargs)

@log
def task_test_cut_for_search_noHMM():
    #//////////////////////////////////////////#
    # --- TEST_FILE_1 ---
    # jRes count: 161574
    # pRes count: 161577
    # jSet & pSet = 16320
    # jSet | pSet = 16323
    # score: 0.9998162102554677
    #//////////////////////////////////////////#
    # TEST_FILE_1, n = 100, cost: 99.746842 s  #
    #//////////////////////////////////////////#
    filename = TEST_FILE_1
    suffix   = CUT_FOR_SEARCH_SUFFIX + NO_HMM_SUFFIX
    fn       = jieba.lcut_for_search
    args     = ()
    kwargs   = {"HMM": False}
    n        = 100
    task_test(fn, filename, suffix, n, *args, **kwargs)

### END Test Functions ###


### Test MultiProcess ###

PROCESS_NUM = multiprocessing.cpu_count() # == 4
#PROCESS_NUM = 2
INTERVAL_TIME = 0.25

class Worker(multiprocessing.Process):

    def __init__(self, dt, inQ, outQ):
        self.dt = dt # Tokenizer 难以在进程间通信，因此每个进程配一个
        self.inQ  = inQ   # [(idx,text), (...), ...]
        self.outQ = outQ  # [(idx,segs), (...), ...]
        super(Worker, self).__init__()

    def run(self):
        while not self.inQ.empty():
            idx, text = self.inQ.get() # block
            res = self.dt.lcut_for_search(text, HMM=False)
            print(idx) # log
            self.outQ.put_nowait( (idx, res) )

@log
def test_multiprocess(n = 100):
    #//////////////////////////////////////////#
    # PROCESS_NUM = 4, TEST_FILE_1, n = 100    #
    #//////////////////////////////////////////#
    # CPU% = 100, max RSS = 3.0 GB             #
    #//////////////////////////////////////////#
    # cut                   cost: 74.106783 s  #
    # cutall                cost: 23.799704 s  #
    # cut_for_search        cost: 80.361630 s  #
    # cut_noHMM             cost: 48.841630 s  #
    # cut_for_search_noHMM  cost: 50.618668 s  #
    #//////////////////////////////////////////#
    #//////////////////////////////////////////#
    # PROCESS_NUM = 2, TEST_FILE_1, n = 100    #
    #//////////////////////////////////////////#
    # CPU% =  50, max RSS = 1.8 GB             #
    #//////////////////////////////////////////#
    # cut                   cost: 78.596184 s  #
    # cutall                cost: 26.787759 s  #
    # cut_for_search        cost: 86.355992 s  #
    # cut_noHMM             cost: 50.811727 s  #
    # cut_for_search_noHMM  cost: 57.578415 s  #
    #//////////////////////////////////////////#
    #
    # 内存过高的主要是因为所有结果都被暂时缓存，实际
    # 使用中可以改成“边切边取”
    #
    #//////////////////////////////////////////#
    pool = []
    text = read_input(TEST_FILE_1)

    inQ = multiprocessing.Queue(n)
    outQ = multiprocessing.Queue(n)

    for idx in range(n):
        inQ.put( (idx+1, text) )

    for _ in range(PROCESS_NUM):
        dt = jieba.Tokenizer()
        dt.initialize()
        p = Worker(dt, inQ, outQ)
        pool.append(p)

    t1 = time.time()

    for p in pool:
        p.daemon = True
        p.start()

    # for p in pool:
    #     p.join()   ## 永不结束？

    while not outQ.full():
        time.sleep(INTERVAL_TIME)

    t2 = time.time()
    cost, unit = t2-t1, "s"
    if cost < 1: # -> ms
        cost, unit = cost*1000, "ms"
    print("cost: %f %s" % (cost, unit))

    while not outQ.empty():
        idx, res = outQ.get()
        # print(idx, len(res))


def test_enable_parallel(n = 100):
    #//////////////////////////////////////////#
    # PROCESS_NUM = 4, TEST_FILE_1, n = 100    #
    #//////////////////////////////////////////#
    # CPU% = 25, total RSS = 500 MB            #
    #//////////////////////////////////////////#
    # cut                   cost: 153.050811 s #
    # cutall                cost:  49.898048 s #
    # cut_for_search        cost: 152.372105 s #
    # cut_noHMM             cost:  92.142967 s #
    # cut_for_search_noHMM  cost: 113.988569 s #
    #//////////////////////////////////////////#
    #
    # 本机测试中会发现，原 jieba 提供的并行计算方法
    # 并不成功，主进程 CPU 占用 25%，而 4 个子进程
    # 的 CPU 占用率为 0
    #
    #//////////////////////////////////////////#
    jieba.initialize()
    jieba.enable_parallel(PROCESS_NUM)
    text = read_input(TEST_FILE_1)

    t1 = time.time()

    for _ in tqdm(range(n)):
        segs = jieba.lcut(text, HMM=False)

    t2 = time.time()
    cost, unit = t2-t1, "s"
    if cost < 1: # -> ms
        cost, unit = cost*1000, "ms"
    print("cost: %f %s" % (cost, unit))


### END Test MultiProcess ###



def test_docs():

    jieba.initialize()
    segs = jieba.lcut("我来到北京清华大学", cut_all=True)
    #segs = jieba.lcut("我来到北京清华大学", cut_all=False)
    #segs = jieba.lcut("他来到了网易杭研大厦")  # 默认是精确模式
    #segs = jieba.lcut_for_search("小明硕士毕业于中国科学院计算所，后在日本京都大学深造")  # 搜索引擎模式
    print(segs)




if __name__ == '__main__':

    #jieba.initialize()

    task_test_cut()
    task_test_cutall()
    task_test_cut_for_search()
    task_test_cut_noHMM()
    task_test_cut_for_search_noHMM()


    #test_multiprocess()
    #test_enable_parallel()

    #test_docs()

    pass