#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# filename: rander_model.py

from utils import pkl_load

OUTPUT_FILE = "../src/main/java/top/rabbitzxh/jieba/viterbi/Model.java"
startP_FILE = "../src/main/resources/prob_start.p"
transP_FILE = "../src/main/resources/prob_trans.p"
emitP_FILE = "../src/main/resources/prob_emit.p"
TAB_WIDTH = 4

import sys
sys.stdout = out = open(OUTPUT_FILE, "w", encoding="utf-8")

def tab(n):
    return " "*TAB_WIDTH*n

def pkl_load(file):
    with open(file, "rb") as fp:
        return pickle.load(fp)


### header ###
header = """\
package top.rabbitzxh.jieba.viterbi;

import java.util.HashMap;
import java.util.Map;

class Model {""" + "\n"
print(header)
### END header ###

### startP ###
map_startP = ""
map_startP += tab(1) + r"protected static final Map<Character, Double> startP;" + "\n"
map_startP += "\n"
map_startP += tab(1) + r"static {" + "\n"
map_startP += tab(2) + r"startP = new HashMap<Character, Double>() {{" + "\n"
for k1, v1 in pkl_load(startP_FILE).items():
    map_startP += tab(3) + f"put('{k1}', {v1}D);" +"\n"
map_startP += tab(2) + r"}};" + "\n"
map_startP += tab(1) + r"}" + "\n"
print(map_startP)
### END startP ###

### transP ###
map_transP = ""
map_transP += tab(1) + r"protected static final Map<Character, Map<Character, Double>> transP;" + "\n"
map_transP += "\n"
map_transP += tab(1) + r"static {" + "\n"
map_transP += tab(2) + r"transP = new HashMap<Character, Map<Character, Double>>() {{" + "\n"
for k1, v1 in pkl_load(transP_FILE).items():
    map_transP += tab(3) + f"put('{k1}', new HashMap<Character, Double>() " + r"{{" + "\n"
    for k2, v2 in v1.items():
        map_transP += tab(4) + f"put('{k2}', {v2}D);" + "\n"
    map_transP += tab(3) + r"}});" + "\n"
map_transP += tab(2) + r"}};" + "\n"
map_transP += tab(1) + r"}" + "\n"
print(map_transP)
### END transP ###

### emitP ###
map_emitP = ""
map_emitP += tab(1) + r"protected static final Map<Character, Map<Character, Double>> emitP;" + "\n"
map_emitP += "\n"
map_emitP += tab(1) + r"static {" + "\n"
map_emitP += tab(2) + r"emitP = new HashMap<Character, Map<Character, Double>>() {{" + "\n"
for k1, v1 in pkl_load(emitP_FILE).items():
    map_emitP += tab(3) + f"put('{k1}', new HashMap<Character, Double>() " + r"{{" + "\n"
    for k2, v2 in v1.items():
        k2 = k2.encode("unicode-escape").decode("utf-8") # to unicode string
        map_emitP += tab(4) + f"put('{k2}', {v2}D);" + "\n"
    map_emitP += tab(3) + r"}});" + "\n"
map_emitP += tab(2) + r"}};" + "\n"
map_emitP += tab(1) + r"}" + "\n"
#print(map_emitP)
### END emitP ###

### ending ###
ending = r"}" + "\n"
print(ending)
### END ending ###


out.close()