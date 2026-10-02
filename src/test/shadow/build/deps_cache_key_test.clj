(ns shadow.build.deps-cache-key-test
  (:require [clojure.test :refer (deftest is)]
            [shadow.build.compiler :as comp]))

(defn rc-id [name]
  [:shadow.build.classpath/resource name])

;; a.cljs -> b.cljs -> c.cljs, unrelated.cljs on its own
(def state
  {:sources
   {(rc-id "a.cljs") {:cache-key ["a-sha"]}
    (rc-id "b.cljs") {:cache-key ["b-sha"]}
    (rc-id "c.cljs") {:cache-key ["c-sha"]}
    (rc-id "unrelated.cljs") {:cache-key ["unrelated-sha"]}}
   :immediate-deps
   {(rc-id "a.cljs") [(rc-id "b.cljs")]
    (rc-id "b.cljs") [(rc-id "c.cljs")]
    (rc-id "c.cljs") []
    (rc-id "unrelated.cljs") []}})

(defn deps-cache-key [state name]
  (comp/make-deps-cache-key state {:resource-id (rc-id name)}))

(defn set-cache-key [state name cache-key]
  (assoc-in state [:sources (rc-id name) :cache-key] cache-key))

(deftest precomputed-digests-match-direct-computation
  (let [with-digests (comp/update-cache-key-digests state (keys (:sources state)))]
    (doseq [name ["a.cljs" "b.cljs" "c.cljs"]]
      (is (= (deps-cache-key state name)
             (deps-cache-key with-digests name))))))

(deftest changes-to-transitive-deps-invalidate
  (let [before (deps-cache-key state "a.cljs")]
    (is (not= before (deps-cache-key (set-cache-key state "c.cljs" ["c-sha-2"]) "a.cljs")))
    (is (= before (deps-cache-key (set-cache-key state "unrelated.cljs" ["changed"]) "a.cljs")))))

(deftest stale-digests-are-recomputed
  (let [ids (keys (:sources state))
        updated (-> (comp/update-cache-key-digests state ids)
                    (set-cache-key "c.cljs" ["c-sha-2"])
                    (comp/update-cache-key-digests ids))]
    (is (= (deps-cache-key (set-cache-key state "c.cljs" ["c-sha-2"]) "a.cljs")
           (deps-cache-key updated "a.cljs")))))
