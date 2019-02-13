#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# filename: test_rank.py

import jieba.analyse
from pprint import pprint
from utils import log, json, tqdm
from test_dt import read_json, write_json, read_input, TEST_FILE_1


def test_tfidf():
    s = read_input(TEST_FILE_1)
    res = jieba.analyse.extract_tags(s, topK=50, withWeight=True, withFlag=True)
    pprint(res)

def test_textrank():
    s = read_input(TEST_FILE_1)
    res = jieba.analyse.textrank(s, topK=50, withWeight=True, withFlag=True)
    pprint(res)


if __name__ == '__main__':
    test_tfidf()
    #test_textrank()