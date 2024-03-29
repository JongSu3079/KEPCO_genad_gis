#!/bin/bash

# ex) /home/HVDC_iec61850/RealTime_SumFile.sh /home/HVDC_iec61850/hvdcData/pd/ 1.241.156.210_102 20211201 00_01_20211201154957.dat
# /home/HVDC_iec61850/hvdcData/pd/upload/1.241.156.210_102/RTTransF/20211201
# /home/HVDC_iec61850/hvdcData/pd/complete/1.241.156.210_102/RTTransF/20211201

ARG1=/home/HVDC_iec61850/hvdcData/pd/
ARG2=1.241.156.210_102
ARG3=20211201
# ARG4=00_01_20211201154957.dat
ARG4=01_01_20211201154952.dat


DATA_DIR=$1
IED_DIR=$2
DATE_DIR=$3
RT_FILE=$4

FILE_PATH="${DATA_DIR}upload/${IED_DIR}/RTTransF/${DATE_DIR}"
COM_FILE_PATH="${DATA_DIR}complete/${IED_DIR}/RTTransF/${DATE_DIR}"

cd "${FILE_PATH}"

FILE_SPLIT_ARR=(`echo ${RT_FILE} | tr "_" "\n"`)
ARR_CNT=${#FILE_SPLIT_ARR[@]}
TAIL_STR=${FILE_SPLIT_ARR[${ARR_CNT} - 1]}

FILE_SPLIT_ARR2=(`echo ${TAIL_STR} | tr "." "\n"`)
FILE_DATE="${FILE_SPLIT_ARR2[0]}"
FILE_DATE_PRE=${FILE_DATE:0:13}
FILE_DATE_TAIL=${FILE_DATE:13}

echo "파일 생성날짜 : ${FILE_DATE}"
# echo "${FILE_DATE_PRE}"
# echo "${FILE_DATE_TAIL}"

# 존재하는 파일의 '초'를 배열로 묶음
# EXIST_FILE_TAIL_ARRAY=()
SUM_TAIL=""
SUM_FILE=""

# 존재하는 파일을 배열로 묶음
EXIST_FILE_ARRAY=()

if [ ${FILE_DATE_TAIL} -gt 4 ]; then
	
	HOUR_DIR=${FILE_DATE_PRE:8:2}
	COM_FILE_PATH="${DATA_DIR}complete/${IED_DIR}/RTTransF/${DATE_DIR}/${HOUR_DIR}"

	for i in 0 1 2 3 4
	do
		FILE="${FILE_PATH}/${FILE_SPLIT_ARR[0]}_${FILE_SPLIT_ARR[1]}_${FILE_DATE_PRE}${i}.dat"
#		echo "${FILE}"
		if [ -e ${FILE} ]; then
#			EXIST_FILE_TAIL_ARRAY+=(${i})
			SUM_TAIL+="${i}"
			EXIST_FILE_ARRAY+=(${FILE})
		fi
	done
	SUM_FILE="${COM_FILE_PATH}/${FILE_SPLIT_ARR[0]}_${FILE_SPLIT_ARR[1]}_${FILE_DATE_PRE}0"
else
	# 지난날/지난시간의 파일을 묶어야할수있기때문에 빼기 계산
	DATE_FORMAT_STR="${FILE_DATE_PRE:0:4}-${FILE_DATE_PRE:4:2}-${FILE_DATE_PRE:6:2} ${FILE_DATE_PRE:8:2}:${FILE_DATE_PRE:10:2}:${FILE_DATE_PRE:12}0"
	FILE_DATE_PRE=$(date -d "${DATE_FORMAT_STR} 10 seconds ago" +"%Y%m%d%H%M%S")
	FILE_DATE_PRE=${FILE_DATE_PRE:0:13}
	# FILE_DATE_PRE=$((${FILE_DATE_PRE} -1))
	DATE_DIR=${FILE_DATE_PRE:0:8}
	HOUR_DIR=${FILE_DATE_PRE:8:2}

	echo "1을 뺀 FILE_DATE_PRE : ${FILE_DATE_PRE}"
	echo "1을 뺀 DATE_DIR : ${DATE_DIR}"
	echo "1을 뺀 HOUR_DIR : ${HOUR_DIR}"

	FILE_PATH="${DATA_DIR}upload/${IED_DIR}/RTTransF/${DATE_DIR}"
	COM_FILE_PATH="${DATA_DIR}complete/${IED_DIR}/RTTransF/${DATE_DIR}/${HOUR_DIR}"

	for i in 5 6 7 8 9
	do
		FILE="${FILE_PATH}/${FILE_SPLIT_ARR[0]}_${FILE_SPLIT_ARR[1]}_${FILE_DATE_PRE}${i}.dat"
#		echo "${FILE}"
                if [ -e ${FILE} ]; then
#                       EXIST_FILE_TAIL_ARRAY+=(${i})
			SUM_TAIL+="${i}"
			EXIST_FILE_ARRAY+=(${FILE})
                fi
	done
	SUM_FILE="${COM_FILE_PATH}/${FILE_SPLIT_ARR[0]}_${FILE_SPLIT_ARR[1]}_${FILE_DATE_PRE}5"
fi

echo "존재하는 파일목록 : ${EXIST_FILE_ARRAY[@]}"
echo "SumFile Tail : ${SUM_TAIL}"

# 파일이 존재하면
if [ -n "${SUM_TAIL}" ]; then
	echo "파일 합치기"

	# 디렉토리 존재여부 확인
	if [ ! -d ${COM_FILE_PATH} ]; then
		mkdir -p ${COM_FILE_PATH}
	fi	

	# 파일 합치기
        cat "${EXIST_FILE_ARRAY[@]}" > "${SUM_FILE}_${SUM_TAIL}.dat"
	# 0.3초 멈춤
#	sleep 0.3

	# 파일 삭제
	if [ -e "${SUM_FILE}_${SUM_TAIL}.dat" ]; then
#		for x in "${EXIST_FILE_ARRAY[@]}"; do
#			rm -rf "${x}"
#		done
		rm -rf "${EXIST_FILE_ARRAY[@]}"
	fi
fi




