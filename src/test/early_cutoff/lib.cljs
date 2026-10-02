(ns early-cutoff.lib)

;; destructured params compile to gensyms that differ on every compile
(defn greet [{:keys [name]}]
  (str "hello " name))
