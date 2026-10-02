(ns shadow.build.resource-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer (deftest is)]
            [shadow.build.resource :as rc]))

(deftest valid-resource-id-matches-spec
  (doseq [id [[:shadow.build.classpath/resource "a.cljs"]
              [:shadow.build.npm/resource "react/index.js" :extra]
              [:shadow.build.classpath/resource]
              [:unqualified "a.cljs"]
              ["a.cljs" :shadow.build.classpath/resource]
              '(:shadow.build.classpath/resource "a.cljs")
              []
              nil
              :shadow.build.classpath/resource]]
    (is (= (s/valid? ::rc/resource-id id) (rc/valid-resource-id? id)) (pr-str id))))
