(require 'harness)

(def N 30)

(defn fib [n]
  (if (<= n 1) n
      (+ (fib (- n 1))
         (fib (- n 2)))))

(harness/bench (println (fib N)))
