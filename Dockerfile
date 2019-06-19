FROM anapsix/alpine-java:latest
RUN touch /var/log/access.log
COPY http-monitoring.sh http-log-monitor.jar /
ENTRYPOINT ["sh", "http-monitoring.sh", "-f", "/var/log/access.log"]