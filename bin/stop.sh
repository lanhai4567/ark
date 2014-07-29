#!/bin/sh
ps -ef |grep ARK_Kernel |awk '{print $2}'|xargs kill -9
