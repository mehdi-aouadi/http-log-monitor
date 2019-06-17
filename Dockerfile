FROM anapsix/alpine-java:latest
COPY stats.sh http-log-monitor.jar /
ENTRYPOINT ["sh", "stats.sh"]