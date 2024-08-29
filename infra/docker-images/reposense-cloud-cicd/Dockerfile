# Multi-stage Dockerfile to build deploy and build images
# Eg. docker build --pull --no-cache --rm -t {OUTPUT_IMAGE:TAG} -f {SRC_DOCKER_FILE} {SRC_WORK_DIR}
# Eg. docker build --pull --no-cache --rm -t reposense-cloud-cicd:test -f $HOME/workspace/Dockerfile $HOME/workspace/

# Base stage with default versions defined, add --platform=linux/amd64 when building locally w M series mac
FROM ubuntu:20.04 as base
LABEL maintainer="Yee Bin Rong <yeebinrong@gmail.com>"
# NodeJs 15.x > requires WORKDIR to be set as per https://stackoverflow.com/questions/57534295/
WORKDIR /home

# Defining default versions as per https://github.com/moby/moby/issues/37345
ARG AWS_CLI_VERSION="2.4.15"
ARG SLS_CLI_VERSION="1.83.0"
ARG NODE_VERSION="18.18.2"
ARG JAVA_VERSION="8.322.06.2-amzn"
ARG MAVEN_VERSION="3.8.4"
ARG YQ_VERSION="4.18.1"
ARG LOCALE="C.UTF-8"
ENV AWS_CLI_VERSION=${AWS_CLI_VERSION}
ENV SLS_CLI_VERSION=${SLS_CLI_VERSION}
ENV NODE_VERSION=${NODE_VERSION}
ENV JAVA_VERSION=${JAVA_VERSION}
ENV MAVEN_VERSION=${MAVEN_VERSION}
ENV YQ_VERSION=${YQ_VERSION}
ENV LC_ALL=${LOCALE}

# Install Tini(https://github.com/krallin/tini)
ARG TINI_VERSION="v0.19.0"
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini-amd64 /tini
RUN chmod +x /tini

ENTRYPOINT ["/tini", "--"]
VOLUME /var/lib/docker

# Stage to build deploy image
FROM base AS deploy

RUN set -eux && apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    bash curl jq unzip zip gnupg software-properties-common \
    python3 python3-pip python3-setuptools && ln -s /usr/bin/python3 /usr/bin/python \
    && curl -sL -o awscliv2.zip https://awscli.amazonaws.com/awscli-exe-linux-x86_64-${AWS_CLI_VERSION}.zip \
    && unzip awscliv2.zip && ./aws/install && rm -rf awscliv2.zip aws \
    && curl -sL -o node.tar.gz https://nodejs.org/dist/v${NODE_VERSION}/node-v${NODE_VERSION}-linux-x64.tar.gz \
    && tar -xzf node.tar.gz -C /usr/local --strip-components=1 --no-same-owner && ln -s /usr/local/bin/node /usr/local/bin/nodejs && rm -rf node.tar.gz \
    && curl -sL -o yq.tar.gz https://github.com/mikefarah/yq/releases/download/v${YQ_VERSION}/yq_linux_amd64.tar.gz \
    && tar -xzf yq.tar.gz && chmod +x yq_linux_amd64 && mv yq_linux_amd64 /usr/bin/yq && rm -rf yq.tar.gz \
    && apt-get install -y less groff \
	&& apt-get clean && rm -rf /var/lib/apt/lists/* /var/cache/* /tmp/* \
    && node -v && npm -v && python --version && pip --version && python3 --version && pip3 --version && aws --version && jq --version && yq --version

# Install sls and plugins, npm modules
RUN set -eux \
    && npm config set legacy-peer-deps true -g \
	&& npm install -g serverless@~${SLS_CLI_VERSION} \
        # serverless-aws-alias@^1.0.21 \
		# serverless-prune-plugin@^1.5.0 \
		# serverless-plugin-dynamodb-autoscaling@^3.1.0 \
		# serverless-api-gateway-caching@^1.7.2 \
		# serverless-api-stage@^2.1.0 \
		# serverless-step-functions@^2.30.0 \
		# serverless-pseudo-parameters@^2.4.0 \
		# serverless-plugin-resource-tagging@^1.1.0 \
		# serverless-sts-caller-id@^1.0.1 \
		# serverless-plugin-ifelse@^1.0.7 \
    && sls --version

# Fix sls v1 for node v18 due to archiver https://github.com/serverless/serverless/issues/11118
RUN set -eux \
    && cd /usr/local/lib/node_modules/serverless \
    && npm install archiver@5.3.2

# Stage to build build image
FROM deploy AS build

RUN set -eux && apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    bash bzip2 curl git gnupg iputils-ping jq libfontconfig openssh-client software-properties-common tzdata unzip wget xmlstarlet zip \
    && curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add - \
    && echo "deb http://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list \
    && apt-get update && apt-get install -y --no-install-recommends yarn \
    && apt-get clean && rm -rf /var/lib/apt/lists/* /var/cache/* /tmp/* \
    && node -v && npm -v && yarn -v && python --version && python3 --version && jq --version && yq --version

# Downloading SDKMAN!
RUN set -eux && curl -s "https://get.sdkman.io" | bash
# Installing Java and Maven, removing some unnecessary SDKMAN files
RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && \
    yes | sdk install java ${JAVA_VERSION} && \
    yes | sdk install maven ${MAVEN_VERSION} && \
    rm -rf $HOME/.sdkman/archives/* && \
    rm -rf $HOME/.sdkman/tmp/* && \
    source $HOME/.sdkman/bin/sdkman-init.sh && \
    java -version && mvn -version"
ENV MAVEN_HOME="/root/.sdkman/candidates/maven/current"
ENV JAVA_HOME="/root/.sdkman/candidates/java/current"
ENV PATH="$MAVEN_HOME/bin:$JAVA_HOME/bin:$PATH"
COPY ./resources/docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh 

# Unblock http repositories(https://issues.apache.org/jira/browse/MNG-7118)
RUN set -eux \
	&& sed -i 's/external:http:\*/dummy/g' $MAVEN_HOME/conf/settings.xml

# Install extra utilities
RUN set -eux \
	&& pip3 install networkx==2.6.3

ENTRYPOINT ["/tini", "--"]
