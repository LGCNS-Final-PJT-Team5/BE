#!/bin/bash

set -e

# 환경 변수
AWS_REGION=ap-northeast-2
ACCOUNT_ID=211125408736
ECR_BASE_URI=${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/modive
IMAGE_TAG=latest

# 컨테이너 이름 및 포트 설정
declare -A SERVICES=(
  ["apigateway-service"]=8000
  ["config-service"]=8888
  ["service-discovery"]=8761
)

# ECR 로그인
echo "Logging into Amazon ECR..."
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin ${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

# 각 서비스별로 배포
for SERVICE in "${!SERVICES[@]}"
do
  PORT=${SERVICES[$SERVICE]}
  IMAGE_URI=${ECR_BASE_URI}/${SERVICE}:${IMAGE_TAG}

  echo "Deploying service: ${SERVICE} (port ${PORT})"

  # 기존 컨테이너 중지 및 삭제
  docker stop ${SERVICE} || true
  docker rm ${SERVICE} || true

  # 기존 이미지 삭제
  docker rmi ${IMAGE_URI} || true

  # 새 이미지 Pull
  docker pull ${IMAGE_URI}

  # 새 컨테이너 실행
  docker run -d --name ${SERVICE} -p ${PORT}:${PORT} ${IMAGE_URI}
done

echo "All services deployed successfully."