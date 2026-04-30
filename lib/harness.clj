(ns harness
  (:require [criterium.core :as crit]))

;; Try not to take too long...
(def benchmark-options
  {:warmup-jit-period 3
   :samples           5})

(defmacro bench [expr]
  `(let [result# (crit/quick-benchmark ~expr benchmark-options)
         mean#   (first (:mean result#))]
     (println "Elapsed time:" (format "%f" mean#) "s")))

