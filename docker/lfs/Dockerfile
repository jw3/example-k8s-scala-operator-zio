FROM centos/python-36-centos7

ARG GIT_LFS_VERSION_ARG="v2.10.0"

USER root

RUN mkdir -p /tmp/lfs/src \
 && curl -sL -o /tmp/lfs/lfs.tar.gz "https://github.com/git-lfs/git-lfs/releases/download/${GIT_LFS_VERSION_ARG}/git-lfs-linux-amd64-${GIT_LFS_VERSION_ARG}.tar.gz" \
 && tar xvzf /tmp/lfs/lfs.tar.gz -C /tmp/lfs/src \
 && /tmp/lfs/src/install.sh \
 && git lfs install --skip-smudge \
 && rm -rf /tmp/lfs

USER default
