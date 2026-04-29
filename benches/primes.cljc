(require 'harness)

(def N 500)

;; From: https://clojuredocs.org/clojure.core/lazy-seq#example-542692d3c026201cdc326ff1
(defn sieve [s]
  (cons (first s)
        (lazy-seq (sieve (filter #(not= 0 (mod % (first s)))
                                 (rest s))))))

(harness/bench (println (nth (sieve (iterate inc 2)) N)))
