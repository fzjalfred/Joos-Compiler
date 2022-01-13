#! /bin/bash

WORKDIR=$(pwd)
IMAGE_ID="9tempest/cs444env"
CONTAINER_ID=$(docker ps |grep $IMAGE_ID|awk '{print $1}')


if [ "${1}" = "clean" ]; then
docker rm -f $CONTAINER_ID
exit
fi

echo "checking docker image ..."

if test "$(docker images |grep $IMAGE_ID)" = ""
then
    echo docker pull $IMAGE_ID
else
    echo "docker image found!"
fi

if test "$CONTAINER_ID" = ""
then
    docker run -v $WORKDIR:/root -itd 9tempest/cs444env bash
fi

CONTAINER_ID=$(docker ps|grep $IMAGE_ID|awk '{print $1}')
docker exec -it $CONTAINER_ID bash

