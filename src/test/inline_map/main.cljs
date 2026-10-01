(ns inline-map.main
  (:require ["/inline_map/bundle.js" :as lib]))

(defn run [k]
  (lib/grow (lib/Point. 1 2) k))
