(require 'harness)

(defn do-loop-count [n]
  (loop [i 0
         j (int n)]
    (if (zero? j)
      i
      (recur (inc i) (dec j)))))

(harness/bench (do-loop-count 10000000))
