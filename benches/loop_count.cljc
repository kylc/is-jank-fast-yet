(require 'harness)

(defn do-loop-count [n stop?]
  (loop [i 0
         j (int n)]
    (if (stop? j)
      i
      (recur (inc i) (dec j)))))

(harness/bench (do-loop-count 10000000 zero?))
