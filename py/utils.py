#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# filename: utils.py

import time
from functools import wraps
import pickle
import gzip
import simplejson as json
from tqdm import tqdm


def b(s):
    if isinstance(s, bytes):
        return s
    elif isinstance(s, (str, int, float)):
        return str(s).encode("utf-8")
    else:
        raise TypeError(s.__class__)

def u(s):
    if isinstance(s, (str, int, float)):
        return str(s)
    elif isinstance(s, bytes):
        return s.decode("utf-8")
    else:
        raise TypeError(s.__class__)

def pkl_load(file):
    with open(file, "rb") as fp:
        return pickle.load(fp)

def txt_load(file):
    with open(file, "r", encoding="utf-8") as fp:
        return fp.read()

def txt_dump(s, file):
    with open(file, "w", encoding="utf-8") as fp:
        fp.write(s)

def gzip_dump(s, file):
    with gzip.open(file, "wb") as fp:
        fp.write(b(s))


def log(fn):
    @wraps(fn)
    def wrapper(*args, **kwargs):
        print("=== BEGIN %r ===" % fn.__name__)
        t1 = time.time()
        res = fn(*args, **kwargs)
        t2 = time.time()
        cost, unit = t2-t1, "s"
        if cost < 1: # -> ms
            cost, unit = cost*1000, "ms"
        print("< cost: %f %s >" % (cost, unit))
        print("=== END %r ===\n" % fn.__name__)
        return res
    return wrapper

def list_split(lst, span):
    """ 以 span 为步长分割 list """
    if not isinstance(lst, (list, tuple)):
        raise TypeError(lst.__class__)
    return [lst[i:i+span] for i in range(0,len(lst),span)]


class Stack(object):

    def __init__(self, items=[]):
        self._items = items

    def __len__(self):
        return len(self._items)

    def __repr__(self):
        return str(self._items)

    def __str__(self):
        return str(self._items)

    def empty(self):
        return len(self._items) == 0

    def size(self):
        return len(self._items)

    def push(self, ele):
        self._items.append(ele)

    def pop(self):
        return self._items.pop() if len(self._items) > 0 else None

    def peek(self):
        return self._items[-1] if len(self._items) > 0 else None

    def clear(self):
        self._items.clear()
