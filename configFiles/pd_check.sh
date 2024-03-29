#!/bin/bash

DATE_TIME=$(date +"%Y-%m-%d %T")

# 192.168.192.50
CLIENT_IP="127.0.0.1"

# 8201, 8202, 8203
CLIENT_PORT=$1

# pd_stat_ied.txt (1.241.156.210,102,SPDCStat_brcb01)
IED_INFO_FILE=$2

# .jar파일 실행중인지 확인
PD_PROCESS_CNT=`ps -ef | grep "genad_stat_HVDC.jar" | grep "${CLIENT_PORT}" | grep -v "grep" | wc -l`

# iec61850_client_server 실행중인지 확인
PD_CLIENT_SERVER_CNT=`ps -ef | grep iec61850_client_server | grep "${CLIENT_PORT}" | grep -v 'grep' | wc -l`

# PD Process가 죽어있음
if [ ${PD_PROCESS_CNT} -eq 0 ]; then

		# client server가 실행중
        if [ ${PD_CLIENT_SERVER_CNT} -gt 0 ]; then
        
        		# client server 죽이기
                PD_CLIENT_SERVER_PID=`ps -ef | grep iec61850_client_server | grep "${CLIENT_PORT}" | grep -v 'grep' | awk '{print $2}'`
                echo "[ ${DATE_TIME} ] - PD Client Server PID : ${PD_CLIENT_SERVER_PID}"
                kill -9 "${PD_CLIENT_SERVER_PID}"
                sleep 5s
        fi

		# PD Process 실행
        nohup /usr/bin/java -jar /home/HVDC_iec61850/genad_stat_HVDC.jar "${CLIENT_IP}" "${CLIENT_PORT}" start_pd_61850.sh "${IED_INFO_FILE}" 1>> /home/HVDC_iec61850/logs/pd/java/pd_${CLIENT_PORT}.log 2>&1 &
        PD_PROCESS_CNT2=`ps -ef | grep "genad_stat_HVDC.jar" | grep "${CLIENT_PORT}" | grep -v "grep" | wc -l`
        echo "[ ${DATE_TIME} ] - PD Process(${CLIENT_PORT}) 다시 시작 (PD_PROCESS_CNT2 : ${PD_PROCESS_CNT2}, PD_CLIENT_SERVER_CNT : ${PD_CLIENT_SERVER_CNT})"

# PD Process는 실행중이고 client server가 죽어있음
elif [ ${PD_PROCESS_CNT} -eq 1 ] && [ ${PD_CLIENT_SERVER_CNT} -eq 0 ]; then

        # PD Process 죽이기
        PD_PROCESS_PID=`ps - ef | grep "genad_stat_HVDC.jar" | grep "${CLIENT_PORT}" | grep -v "grep" | awk '{print $2}'`
        echo "[ ${DATE_TIME} ] - PD Process PID : ${PD_PROCESS_PID}"
        kill -9 "${PD_PROCESS_PID}"
        sleep 3s

        # PD Process 실행
        nohup /usr/bin/java -jar /home/HVDC_iec61850/genad_stat_HVDC.jar "${CLIENT_IP}" "${CLIENT_PORT}" start_pd_61850.sh "${IED_INFO_FILE}" 1>> /home/HVDC_iec61850/logs/pd/java/pd${CLIENT_PORT}.log 2>&1 &
        echo "[ ${DATE_TIME} ] - PD Process(${CLIENT_PORT}) 다시 시작"
fi

