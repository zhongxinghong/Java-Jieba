#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# filename: rander_model.py

import os
from utils import txt_load, gzip_dump

OUTPUT_DIR = "../src/main/resources/"

fileList = [

    "dict.big",
    "dict.std",
    "dict.small",
    "idf.std",

    ]

for file in fileList:
    txtFile = os.path.join(OUTPUT_DIR, file+".txt")
    gzFile = os.path.join(OUTPUT_DIR, file+".gz")
    gzip_dump(txt_load(txtFile), gzFile)
