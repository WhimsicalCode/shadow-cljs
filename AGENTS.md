# Whimsical's shadow-cljs fork

This is Whimsical's fork of [thheller/shadow-cljs](https://github.com/thheller/shadow-cljs). The `whimsical` branch (the default) is upstream plus our commits, published to Clojars as `com.whimsical/shadow-cljs` with versions `<upstream>-whim.<n>`. `master` mirrors upstream. Every `deps.edn` in the Whimsical repo pins the published version; see its `docs/ci-and-dependencies.md`.

## What the fork changes

- Source maps map through a classpath JS file's own inline source map (`//# sourceMappingURL=data:...`), so a bundle built from TypeScript maps back to its `.ts` files. `ShadowCompiler.getSourceMapping` keeps the files the inline map names, and the release map doesn't put them on `x_google_ignoreList`.
- Dev `:esm` builds shift the source maps Closure makes for JS sources past the imports prepended to each file.
- The artifact is `com.whimsical/shadow-cljs`, and the version lookups (`--version`, the server banner, the load error) read its coordinates.
- `release-whimsical.sh` and `.github/workflows/whimsical-release.yml` build and deploy it.

`src/test/shadow/build/inline_source_map_test.clj` covers the source map changes with a release and a dev build of `src/test/inline_map/`.

## Moving to a new upstream release

Upstream no longer tags releases; each is a `bump X.Y.Z` commit on its `master`.

1. `git fetch upstream`, then find the release: `git log --oneline --grep='^bump ' upstream/master`.
2. `git checkout whimsical && git rebase <bump commit>`. The `defproject` line in `project.clj` conflicts; resolve it to `(defproject com.whimsical/shadow-cljs "<X.Y.Z>-whim.1"`.
3. Fast-forward `master` to the same commit and push it: `git push origin <bump commit>:master`.
4. Check it: `./release-whimsical.sh --no-deploy` runs `lein test` and installs the jar into `~/.m2`.
5. `git push --force-with-lease origin whimsical`.
6. Deploy: `gh workflow run whimsical-release.yml --repo WhimsicalCode/shadow-cljs --ref whimsical`, then follow it with `gh run watch`.
7. Tag the released commit, so it stays reachable after later rebases: `git tag <version> && git push origin <version>`.
8. In the Whimsical repo, pin the new version in every `deps.edn` (they must all match).

For a fix without a new upstream release, commit it on `whimsical`, bump `-whim.<n>`, and run steps 4–8. Clojars never replaces a published version, so every deploy needs a new `-whim.<n>`.

## Building and releasing

- `lein test` runs the tests. It compiles the Java sources (`lein javac`) first.
- The jar carries built output: the UI, the build report and the babel worker. Upstream builds these against local checkouts of its other libraries, and the fork doesn't change them, so `release-whimsical.sh` takes them from the upstream jar of the same version, checksum-verified, into the gitignored `src/ui-release/`. `build-all.sh` doesn't work here.
- The workflow deploys with the repository secrets `CLOJARS_USERNAME` and `CLOJARS_DEPLOY_TOKEN`. To deploy locally instead, run `LEIN_USERNAME=<user> LEIN_PASSWORD=<deploy token> ./release-whimsical.sh`; the token needs deploy rights for the `com.whimsical` group.
