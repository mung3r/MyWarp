#!/bin/bash
#
# Publish Javadoc of successful CI builds to https://thee.github.io/MyWarp/javadoc
# See https://web.archive.org/web/20150107174657/http://benlimmer.com/2013/12/26/automatically-publish-javadoc-to-gh-pages-with-travis-ci/

if [ "$TRAVIS_REPO_SLUG" == "TheE/MyWarp" ] && \
   [ "$TRAVIS_JDK_VERSION" == "oraclejdk8" ] && \
   [ "$TRAVIS_PULL_REQUEST" == "false" ] &&  \
   [ "$TRAVIS_BRANCH" == "master" ]; then

  echo -e "Publishing javadoc...\n"

  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git clone --quit --branch=gh-pages https://${GH_TOKEN}@github.com/TheE/MyWarp $HOME/gh-pages > /dev/null
  cd $HOME/gh-pages

  echo -e "Pages repository cloned."

  for module in mywarp-core mywarp-bukkit; do
    mkdir -p ./javadoc/$module
    git rm -rf ./javadoc/$module/*

    cp -Rf $TRAVIS_BUILD_DIR/$module/build/docs/javadoc/ ./javadoc/$module
    git add -f ./javadoc/$module
    echo -e "Javadocs for '$module' added."
  done

  git commit -m "Lastest javadoc on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
  git push -fq origin gh-pages > /dev/null

  echo -e "Published Javadoc to gh-pages.\n"

fi
