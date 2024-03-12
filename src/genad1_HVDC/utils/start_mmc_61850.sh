#!/bin/sh

HOME=$1

cd $HOME

# nohup ./iec61850_client_server $2 > ./client_jpy.log &
# nohup ./iec61850_client_server $2 1>> /home/HVDC_iec61850/logs/mmc/clientServer/client_mmc.log 2>&1 &
nohup ./iec61850_client_server $2 1> /dev/null 2>&1 &

