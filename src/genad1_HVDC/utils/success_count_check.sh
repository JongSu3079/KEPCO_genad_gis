#!/bin/bash

# 파일 저장위치
PATH=/home/HVDC_iec61850/hvdcData/pd/complete

IP=$1
PORT=$2

START_DATETIME=$3
END_DATETIME=$4

START_DATE=${START_DATETIME:0:8}
END_DATE=${END_DATETIME:0:8}

G_START_DATETIME=${START_DATETIME:0:10}
G_END_DATETIME=${END_DATETIME:0:10}

echo "=============================================================="
echo "IP : ${IP}"
echo "PORT : ${PORT}"
echo "시간범위 : ${START_DATETIME} ~ ${END_DATETIME}"
echo "=============================================================="

# 파일 종류별 갯수
EVT_CNT_1=0
EVT_CNT_2=0

HIGH_CNT_1=0
HIGH_CNT_2=0

RT_CNT_1=0
RT_CNT_2=0

TREND_CNT_1=0
TREND_CNT_2=0

cd "${PATH}/${IP}_${PORT}"

echo "=======[ 이벤트 파일 ]================="
cd "EvtTransF"

G_DIR=`/bin/ls -d *`

for dir in ${G_DIR}; do

	# 디렉토리 체크
	if [ ${dir} -ge ${START_DATE} ] && [ ${dir} -le ${END_DATE} ]; then
		echo "${dir} - 체크"
		cd "${PATH}/${IP}_${PORT}/EvtTransF/${dir}"
		
		FILES=`/bin/ls *`
		for file in ${FILES}; do
			FILE_SPLIT_ARR=(`echo ${file} | /bin/tr "_" "\n"`)
			FILE_TAIL_ARR=(`echo ${FILE_SPLIT_ARR[2]} | /bin/tr "." "\n"`)
			
			# 파일 체크
			if [ ${FILE_TAIL_ARR[0]} -ge ${START_DATETIME} ] && [ ${FILE_TAIL_ARR[0]} -le ${END_DATETIME} ]; then
				if [ "${FILE_SPLIT_ARR[0]}" == "00" ]; then
					EVT_CNT_1=$(($EVT_CNT_1 + 1))
				else
					EVT_CNT_2=$(($EVT_CNT_2 + 1))
				fi
			fi
		done
	fi
done

echo "=======[ 250M 파일 ]================="
cd "${PATH}/${IP}_${PORT}/HighResTransF"

G_DIR=`/bin/ls -d *`

for dir in ${G_DIR}; do

        # 디렉토리 체크
        if [ ${dir} -ge ${START_DATE} ] && [ ${dir} -le ${END_DATE} ]; then
		echo "${dir} - 체크"
                cd "${PATH}/${IP}_${PORT}/HighResTransF/${dir}"

                FILES=`/bin/ls *`
                for file in ${FILES}; do
                        FILE_SPLIT_ARR=(`echo ${file} | /bin/tr "_" "\n"`)
                        FILE_TAIL_ARR=(`echo ${FILE_SPLIT_ARR[2]} | /bin/tr "." "\n"`)

                        # 파일 체크
                        if [ ${FILE_TAIL_ARR[0]} -ge ${START_DATETIME} ] && [ ${FILE_TAIL_ARR[0]} -le ${END_DATETIME} ]; then
                                if [ "${FILE_SPLIT_ARR[0]}" == "00" ]; then
                                        HIGH_CNT_1=$(($HIGH_CNT_1 + 1))
                                else
                                        HIGH_CNT_2=$(($HIGH_CNT_2 + 1))
                                fi
                        fi
                done
        fi
done

echo "=======[ 트렌드 파일 ]================="
cd "${PATH}/${IP}_${PORT}/TrendTransF"

G_DIR=`/bin/ls -d *`

for dir in ${G_DIR}; do

        # 디렉토리 체크
        if [ ${dir} -ge ${START_DATE} ] && [ ${dir} -le ${END_DATE} ]; then
		echo "${dir} - 체크"
                cd "${PATH}/${IP}_${PORT}/TrendTransF/${dir}"

                FILES=`/bin/ls *`
                for file in ${FILES}; do
                        FILE_SPLIT_ARR=(`echo ${file} | /bin/tr "_" "\n"`)
                        FILE_TAIL_ARR=(`echo ${FILE_SPLIT_ARR[2]} | /bin/tr "." "\n"`)

                        # 파일 체크
                        if [ ${FILE_TAIL_ARR[0]} -ge ${START_DATETIME} ] && [ ${FILE_TAIL_ARR[0]} -le ${END_DATETIME} ]; then
                                if [ "${FILE_SPLIT_ARR[0]}" == "00" ]; then
                                        TREND_CNT_1=$(($TREND_CNT_1 + 1))
                                else
                                        TREND_CNT_2=$(($TREND_CNT_2 + 1))
                                fi
                        fi
                done
        fi
done

echo "=======[ 실시간 파일 ]================="
cd "${PATH}/${IP}_${PORT}/RTTransF"

G_DIR=`/bin/ls -d *`

for dir in ${G_DIR}; do

        # 디렉토리 체크
        if [ ${dir} -ge ${START_DATE} ] && [ ${dir} -le ${END_DATE} ]; then
		echo "${dir} - 체크"
                cd "${PATH}/${IP}_${PORT}/RTTransF/${dir}"

		TIMES=`/bin/ls -d *`
		for time in ${TIMES}; do
			L_DATETIME="${dir}${time}"

			# 시간 디렉토리 체크
			if [ ${L_DATETIME} -ge ${G_START_DATETIME} ] && [ ${L_DATETIME} -le ${G_END_DATETIME} ]; then 
				cd "${PATH}/${IP}_${PORT}/RTTransF/${dir}/${time}"

                		FILES=`/bin/ls *`
                		for file in ${FILES}; do

					# echo "${file}"

                        		FILE_SPLIT_ARR=(`echo ${file} | /bin/tr "_" "\n"`)
					FILE_DATETIME=${FILE_SPLIT_ARR[2]}
                        		FILE_TAIL_ARR=(`echo ${FILE_SPLIT_ARR[3]} | /bin/tr "." "\n"`)

					# echo "FILE_DATETIME : ${FILE_DATETIME} , START_DATETIME : ${START_DATETIME}"
			
                        		# 파일 체크
                        		if [ ${FILE_DATETIME} -ge ${START_DATETIME} ] && [ ${FILE_DATETIME} -le ${END_DATETIME} ]; then
						
						# 합쳐진 파일갯수 체크
						FILE_TAIL="${FILE_TAIL_ARR[0]}"
						FILE_CNT=${#FILE_TAIL}

						# echo "${FILE_CNT}"
						
                                		if [ "${FILE_SPLIT_ARR[0]}" == "00" ]; then
                                        		RT_CNT_1=$(($RT_CNT_1 + $FILE_CNT))
                                		else
                                        		RT_CNT_2=$(($RT_CNT_2 + $FILE_CNT))
                                		fi
                        		fi
		                done
			fi
		done
        fi
done


echo "------------------------------------------------------------------"
echo "         realtime	event		250M		trend   "
echo "------------------------------------------------------------------"
echo "[ch1]    ${RT_CNT_1}		${EVT_CNT_1}		${HIGH_CNT_1}		${TREND_CNT_1}"
echo "[ch2]    ${RT_CNT_2}		${EVT_CNT_2}		${HIGH_CNT_2}		${TREND_CNT_2}"
echo "------------------------------------------------------------------"



