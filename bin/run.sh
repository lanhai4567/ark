#!/bin/sh

cd ..

CLSNAME=com.etone.ark.kernel.Runner

#---------------------------------#
# dynamically build the classpath #
#---------------------------------#
THE_CLASSPATH=
for i in `ls lib/*.jar`
do
  THE_CLASSPATH=${THE_CLASSPATH}:${i}
done

#-----------------#
# run the program #
#-----------------#
java -server -Xms512m -Xmx2048m -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -cp ".:${THE_CLASSPATH}" ${CLSNAME} ARK_Kernel
#java -server -Xms4096m -Xmx8000m -cp ".:${THE_CLASSPATH}" ${CLSNAME} ARK_Kernel
