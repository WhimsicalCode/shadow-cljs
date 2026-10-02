(ns shadow.build.closure-js-cache-test
  "classpath JS converted by closure is cached on disk, and a new build state, as
   after restarting watch, should restore it instead of converting it again."
  (:require
    [clojure.test :refer (deftest is)]
    [shadow.cljs.devtools.api :as api]))

(def config
  {:build-id ::closure-js-cache
   :target :esm
   :runtime :node
   :output-dir "target/test-closure-js-cache"
   :modules {:main {:exports {:run 'inline-map.main/run}}}})

(defn bundle-output [state]
  (let [resource-id (->> (vals (:sources state))
                         (filter #(= "inline_map/bundle.js" (:resource-name %)))
                         (first)
                         (:resource-id))]
    (get-in state [:output resource-id])))

(deftest fresh-build-restores-classpath-js-from-cache
  (api/with-runtime
    (api/compile* config {})
    (let [output (bundle-output (api/compile* config {}))]
      (is (some? output))
      (is (:cached output)))))
