#!/bin/sh
# Tests, builds and deploys com.whimsical/shadow-cljs to Clojars.
#
# The jar also carries built output (the UI, the build report and the babel worker) that
# upstream builds against local checkouts of its other libraries. This fork doesn't change
# any of it, so it comes from the upstream jar of the same version, checksum-verified.
#
# Deploying needs LEIN_USERNAME and LEIN_PASSWORD (a Clojars deploy token) in the
# environment. Pass --no-deploy to build and install into ~/.m2 only.

set -eu

cd "$(dirname "$0")"

deploy=true
if [ "${1:-}" = "--no-deploy" ]; then
    deploy=false
fi

version=$(sed -n 's/^(defproject com\.whimsical\/shadow-cljs "\([^"]*\)".*/\1/p' project.clj)
upstream=${version%-whim.*}
if [ -z "$version" ] || [ "$upstream" = "$version" ]; then
    echo "project.clj must define com.whimsical/shadow-cljs \"<upstream>-whim.<n>\", found \"$version\"" >&2
    exit 1
fi

if [ -n "$(git status --porcelain --untracked-files=no)" ]; then
    echo "uncommitted changes; release from a clean checkout" >&2
    exit 1
fi

tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT

url="https://repo.clojars.org/thheller/shadow-cljs/$upstream/shadow-cljs-$upstream.jar"
echo "Fetching built assets from $url"
curl -fsSL -o "$tmp/upstream.jar" "$url"
expected=$(curl -fsSL "$url.sha1" | cut -d' ' -f1)
actual=$(shasum -a 1 "$tmp/upstream.jar" | cut -d' ' -f1)
if [ "$expected" != "$actual" ]; then
    echo "checksum mismatch for $url: expected $expected, got $actual" >&2
    exit 1
fi

rm -rf src/ui-release/shadow/cljs/ui/dist/js src/ui-release/shadow/cljs/build_report/dist/js src/ui-release/shadow/cljs/dist
unzip -o -q "$tmp/upstream.jar" \
    'shadow/cljs/ui/dist/*' \
    'shadow/cljs/build_report/dist/*' \
    'shadow/cljs/dist/babel-worker.js' \
    -d src/ui-release

lein test

if [ "$deploy" = true ]; then
    lein deploy clojars
else
    lein install
fi

echo "com.whimsical/shadow-cljs $version (from upstream $upstream)"
