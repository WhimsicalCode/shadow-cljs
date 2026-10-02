(ns shadow.build.early-cutoff-test
  "watch only recompiles the dependents of a modified namespace when the analyzer
   data they compile against changed. the others keep their output but still count
   as compiled, so they are hot-reloaded as before."
  (:require
    [clojure.test :refer (deftest is)]
    [shadow.build :as build]
    [shadow.build.api :as build-api]
    [shadow.build.data :as data]
    [shadow.cljs.devtools.api :as api]
    [shadow.cljs.devtools.server.util :as util]))

(def config
  {:build-id ::early-cutoff
   :target :esm
   :runtime :node
   :output-dir "target/test-early-cutoff"
   :build-options {:cache-level :off}
   :modules {:main {:exports {:run 'early-cutoff.main/run}}}})

(defn initial-build []
  (-> (util/new-build config :dev {})
      (build/configure :dev config {})
      (build/compile)))

(defn source-id [state ns]
  (get-in state [:sym->id ns]))

;; what watch does when it sees a modified file, with the new contents in the build state
(defn edit-and-compile [state ns source]
  (let [rc (get-in state [:sources (source-id state ns)])]
    (-> state
        (build-api/reset-namespaces-with-cutoff #{ns})
        (data/add-source (assoc rc :source source :cache-key [(data/sha1-string source)]))
        (build/compile))))

(defn main-output [state]
  (get-in state [:output (source-id state 'early-cutoff.main)]))

(defn compiled-recently? [state ns]
  (contains? (set (build/resources-compiled-recently state)) (source-id state ns)))

(deftest body-edits-keep-dependent-output
  (api/with-runtime
    (let [before (initial-build)]
      (doseq [source ["(ns early-cutoff.lib)\n\n(defn greet [{:keys [name]}]\n  (str \"hi \" name))\n"
                      ";; shifts every position\n(ns early-cutoff.lib)\n\n\n(defn greet [{:keys [name]}]\n  (str \"hello \" name))\n"]]
        (let [after (edit-and-compile before 'early-cutoff.lib source)]
          (is (identical? (:js (main-output before)) (:js (main-output after))))
          (is (compiled-recently? after 'early-cutoff.lib))
          (is (compiled-recently? after 'early-cutoff.main)))))))

(deftest interface-changes-recompile-dependents
  (api/with-runtime
    (let [before (initial-build)
          after (edit-and-compile before 'early-cutoff.lib
                  "(ns early-cutoff.lib)\n\n(defn greet [{:keys [name]} punctuation]\n  (str \"hello \" name punctuation))\n")]
      (is (not (identical? (:js (main-output before)) (:js (main-output after)))))
      (is (compiled-recently? after 'early-cutoff.lib))
      (is (compiled-recently? after 'early-cutoff.main))
      (is (= [:fn-arity] (map :warning (:warnings (main-output after))))))))

(deftest cutoff-can-be-disabled
  (api/with-runtime
    (let [before (-> (initial-build) (assoc-in [:build-options :early-cutoff] false))
          after (edit-and-compile before 'early-cutoff.lib
                  "(ns early-cutoff.lib)\n\n(defn greet [{:keys [name]}]\n  (str \"hi \" name))\n")]
      (is (not (identical? (:js (main-output before)) (:js (main-output after))))))))
