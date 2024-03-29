#!/bin/sh

HOME=$1

cd $HOME

# nohup ./iec61850_client_server $2 1> /dev/null 2>&1 &
$ nohup ./iec61850_client_server $2 1> /home/HVDC_iec61850/logs/pd/clientServer/client_pd.log 2>&1 &
nohup ./iec61850_client_server $2 1> /dev/null 2>&1 &

