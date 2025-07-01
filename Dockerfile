FROM ubuntu:latest
LABEL authors="raonpark"

ENTRYPOINT ["top", "-b"]