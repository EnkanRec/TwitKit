#!/bin/bash
basepath=$(cd `dirname $0`; pwd)
xvfb-run python3 $basepath/start_oven.py
