(def N 30)

(defn fib ^long [^long n]
  (if (<= n 1) n
      (+ (fib (- n 1))
         (fib (- n 2)))))

(dotimes [_ 50]
  (time (println (fib N))))
