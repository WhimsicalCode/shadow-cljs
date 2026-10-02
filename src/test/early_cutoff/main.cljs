(ns early-cutoff.main
  (:require [early-cutoff.lib :as lib]))

(defn run []
  (lib/greet {:name "world"}))
