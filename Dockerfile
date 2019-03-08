FROM neo4j:3.5.3
LABEL author dinh-cuong.duong@epita.fr

COPY ./target/decision-tree-core-1.0.1-SNAPSHOT.jar /var/lib/neo4j/plugins/

RUN apk update && apk add vim wget && \
    wget http://central.maven.org/maven2/org/codehaus/janino/commons-compiler/3.0.8/commons-compiler-3.0.8.jar -P /var/lib/neo4j/plugins/ && \
    wget http://central.maven.org/maven2/org/codehaus/janino/janino/3.0.8/janino-3.0.8.jar -P /var/lib/neo4j/plugins/ && \
    echo "dbms.security.procedures.unrestricted=fr.zaki.*" > /var/lib/neo4j/conf/neo4j.conf

ENTRYPOINT ["/sbin/tini", "-g", "--", "/docker-entrypoint.sh"]
CMD ["neo4j"]