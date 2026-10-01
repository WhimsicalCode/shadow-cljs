(ns shadow.build.inline-source-map-test
  "a classpath JS file can carry its own inline source map, for example a bundle built
   from TypeScript. the build's source maps should map through it to the files it names."
  (:require
    [clojure.test :refer (deftest is)]
    [clojure.java.io :as io]
    [clojure.data.json :as json]
    [clojure.string :as str]
    [shadow.cljs.devtools.api :as api])
  (:import (com.google.debugging.sourcemap SourceMapConsumerV3)))

(def output-root "target/test-inline-source-map")

(defn build-config [build-id]
  {:build-id build-id
   :target :esm
   :runtime :node
   :output-dir (str output-root "/" (name build-id))
   :compiler-options {:source-map true}
   :modules {:main {:exports {:run 'inline-map.main/run}}}})

(defn consumer [map-file]
  (doto (SourceMapConsumerV3.)
    (.parse (slurp map-file))))

;; where in the TypeScript the first occurrence of needle in the generated file maps to
(defn original-position [js-file map-file needle]
  (let [lines (str/split-lines (slurp js-file))
        line (first (keep-indexed #(when (str/includes? %2 needle) %1) lines))
        mapping (.getMappingForLine (consumer map-file) (inc line) (inc (str/index-of (nth lines line) needle)))]
    [(.getOriginalFile mapping) (.getLineNumber mapping)]))

(deftest release-maps-through-inline-source-map
  (let [{:keys [output-dir] :as config} (build-config ::release)]
    (api/with-runtime (api/release* config {}))
    (let [js-file (io/file output-dir "main.js")
          map-file (io/file output-dir "main.js.map")
          {:strs [sections]} (json/read-str (slurp map-file))
          {:strs [sources x_google_ignoreList]} (get (last sections) "map")
          ignored (set (map #(nth sources %) x_google_ignoreList))]
      (is (= ["inline_map/src/geom.ts" 5]
             (original-position js-file map-file "boom from geom.ts")))
      (is (some #{"inline_map/src/shape.ts"} sources))
      ;; the files are the source's own, not library code
      (is (not-any? ignored ["inline_map/src/geom.ts" "inline_map/src/shape.ts" "inline_map/bundle.js"])))))

(deftest dev-maps-through-inline-source-map-past-imports
  (let [{:keys [output-dir] :as config} (build-config ::dev)]
    (api/with-runtime (api/compile* config {}))
    (let [js-file (io/file output-dir "cljs-runtime" "module$inline_map$bundle.js")
          map-file (io/file (str js-file ".map"))]
      ;; an :esm dev build prepends imports, which the map has to account for
      (is (str/starts-with? (slurp js-file) "import "))
      (is (= ["inline_map/src/geom.ts" 5]
             (original-position js-file map-file "boom from geom.ts")))
      (is (= ["inline_map/src/shape.ts" 6]
             (original-position js-file map-file "p.scale(k)"))))))
