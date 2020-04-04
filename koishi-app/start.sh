#!/bin/bash
basepath=$(cd `dirname $1`; pwd)
cd $basepath
npm run dev
