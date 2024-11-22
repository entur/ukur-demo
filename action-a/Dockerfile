FROM debian:9.5-slim

LABEL "com.github.actions.name"="GH Actions Demo"
LABEL "com.github.actions.description"="This is a demo of gh actions"
LABEL "com.github.actions.icon"="mic"
LABEL "com.github.actions.color"="purple"

ADD entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]