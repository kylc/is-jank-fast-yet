(defn fib [n]
  (loop [curr n
         a    0
         b    1]
    (if (zero? curr)
      a
      (recur (dec curr) b (+' a b)))))

(dotimes [i 100]
  (time (fib 50000)))
