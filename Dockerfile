FROM anapsix/alpine-java:latest
RUN touch /var/log/access.log
COPY stats.sh http-log-monitor.jar /
ENTRYPOINT ["sh", "stats.sh", "-f", "/var/log/access.log"]