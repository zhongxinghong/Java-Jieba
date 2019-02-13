#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# filename: render_pos_model.py

import os
from utils import txt_dump, gzip_dump

NULL_VALUE = "NULL"
OUTPUT_DIR = "../src/main/resources/"

PROB_START_TXT     = os.path.join(OUTPUT_DIR, "prob_start.pos.txt")
PROB_START_GZ      = os.path.join(OUTPUT_DIR, "prob_start.pos.gz")
PROB_TRANS_TXT     = os.path.join(OUTPUT_DIR, "prob_trans.pos.txt")
PROB_TRANS_GZ      = os.path.join(OUTPUT_DIR, "prob_trans.pos.gz")
PROB_EMIT_TXT      = os.path.join(OUTPUT_DIR, "prob_emit.pos.txt")
PROB_EMIT_GZ       = os.path.join(OUTPUT_DIR, "prob_emit.pos.gz")
CHAR_STATE_TAB_TXT = os.path.join(OUTPUT_DIR, "char_state_tab.pos.txt")
CHAR_STATE_TAB_GZ  = os.path.join(OUTPUT_DIR, "char_state_tab.pos.gz")

from jieba.posseg.char_state_tab import P as char_state_tab_P
from jieba.posseg.prob_start import P as start_P
from jieba.posseg.prob_trans import P as trans_P
from jieba.posseg.prob_emit import P as emit_P


### prob_start ###
output = ""
for (k1, k2), v1 in start_P.items():
    output += f"{k1} {k2} {v1}D\n"
txt_dump(output, PROB_START_TXT)
gzip_dump(output, PROB_START_GZ)
### END prob_start ###

### prob_trans ###
output = ""
for (k1, k2), d1 in trans_P.items():
    output += f"\n{k1} {k2}\n"
    if len(d1) == 0:
        output += NULL_VALUE + "\n"
    for (k3, k4), v2 in d1.items():
        output += f"{k3} {k4} {v2}D\n"
txt_dump(output, PROB_TRANS_TXT)
gzip_dump(output, PROB_TRANS_GZ)
### END prob_trans ###

### prob_emit ###
output = ""
for (k1, k2), d1 in emit_P.items():
    output += f"\n{k1} {k2}\n"
    for k3, v1 in d1.items():
        output += f"{k3} {v1}D\n"
txt_dump(output, PROB_EMIT_TXT)
gzip_dump(output, PROB_EMIT_GZ)
### END prob_emit ###

### char_state_tab ###
output = ""
for k1, l1 in char_state_tab_P.items():
    output += f"\n{k1}\n"
    for k2, k3 in l1:
        output += f"{k2} {k3}\n"
txt_dump(output, CHAR_STATE_TAB_TXT)
gzip_dump(output, CHAR_STATE_TAB_GZ)
### END char_state_tab ###
