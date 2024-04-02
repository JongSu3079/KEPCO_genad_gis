#!/bin/bash

DATE_TIME=$(date +"%Y-%m-%d %T")

# 
CLIENT_IP="127.0.0.1"

# 8101, 8102... 8201, 8201...
CLIENT_PORT=$1

# glu_ied_rcb_SCBR_Diagnostic.txt (192.168.10.101,102,rcb_SCBR_Diagnostic01,GNDMUGLU01)
IED_INFO_FILE=$2

# glu, mlu
TYPE=$3

# .jar파일 실행중인지 확인
PROCESS_CNT=`ps -ef | grep "KEPCO_tnm_genad.jar" | grep "${CLIENT_PORT}" | grep -v "grep" | wc -l`

# iec61850_client_server 실행중인지 확인
CLIENT_SERVER_CNT=`ps -ef | grep iec61850_client_server | grep "${CLIENT_PORT}" | grep -v 'grep' | wc -l`

# Process가 죽어있음
if [ ${PROCESS_CNT} -eq 0 ]; then

		# client server가 실행중
        if [ ${CLIENT_SERVER_CNT} -gt 0 ]; then
        
        		# client server 죽이기
                CLIENT_SERVER_PID=`ps -ef | grep iec61850_client_server | grep "${CLIENT_PORT}" | grep -v 'grep' | awk '{print $2}'`
                echo "[ ${DATE_TIME} ] - ${TYPE} (${CLIENT_PORT}) Client Server PID : ${CLIENT_SERVER_PID}"
                kill -9 "${CLIENT_SERVER_PID}"
                sleep 5s
        fi

		# Process 실행
        nohup /usr/bin/java -jar /home/KEPCO_iec61850/KEPCO_tnm_genad.jar "${CLIENT_IP}" "${CLIENT_PORT}" start_client_61851.sh "${IED_INFO_FILE}" 1>> /home/KEPCO_iec61850/logs/${TYPE}/java/${TYPE}_${CLIENT_PORT}.log 2>&1 &
        PROCESS_CNT2=`ps -ef | grep "KEPCO_tnm_genad.jar" | grep "${CLIENT_PORT}" | grep -v "grep" | wc -l`
        echo "[ ${DATE_TIME} ] - ${TYPE} Process(${CLIENT_PORT}) 다시 시작 (PROCESS_CNT2 : ${PROCESS_CNT2}, CLIENT_SERVER_CNT : ${CLIENT_SERVER_CNT})"

# Process는 실행중이고 client server가 죽어있음
elif [ ${PROCESS_CNT} -eq 1 ] && [ ${CLIENT_SERVER_CNT} -eq 0 ]; then

        # Process 죽이기
        PROCESS_PID=`ps - ef | grep "KEPCO_tnm_genad.jar" | grep "${CLIENT_PORT}" | grep -v "grep" | awk '{print $2}'`
        echo "[ ${DATE_TIME} ] - ${TYPE} Process(${CLIENT_PORT}) PID : ${PROCESS_PID}"
        kill -9 "${PROCESS_PID}"
        sleep 3s

        # Process 실행
        nohup /usr/bin/java -jar /home/KEPCO_iec61850/KEPCO_tnm_genad.jar "${CLIENT_IP}" "${CLIENT_PORT}" start_client_61851.sh "${IED_INFO_FILE}" 1>> /home/KEPCO_iec61850/logs/${TYPE}/java/${TYPE}_${CLIENT_PORT}.log 2>&1 &
        echo "[ ${DATE_TIME} ] - ${TYPE} Process(${CLIENT_PORT}) 다시 시작"
fi

