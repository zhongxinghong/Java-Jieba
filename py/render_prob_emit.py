#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# filename: rander_prob_emit.py

import gzip
from utils import pkl_load

emitP_FILE = "../src/main/resources/prob_emit.p"
OUTPUT_TXT_FILE = "../src/main/resources/prob_emit.txt"
OUTPUT_GZ_FILE = "../src/main/resources/prob_emit.gz"

output = ""
for k1, v1 in pkl_load(emitP_FILE).items():
    output += f"\n{k1}\n"
    for k2, v2 in v1.items():
        #k2 = k2.encode("unicode-escape").decode("utf-8") # to unicode string
        output += f"{k2} {v2}D\n"

with open(OUTPUT_TXT_FILE, "w", encoding="utf-8") as fp:
    fp.write(output)

with gzip.open(OUTPUT_GZ_FILE, "wb") as fp:
    fp.write(output.encode("utf-8"))
