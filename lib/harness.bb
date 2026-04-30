(ns harness)

(defmacro bench [expr]
  ;; TODO: implement a sophisticated benchmark loop like criterium or nanobench.
  `(let [N          50
         start-time (System/nanoTime)]
     (dotimes [i N]
      ;; TODO: how to blackhole the result on bb?
       (println ~expr))
     (let [end-time (System/nanoTime)
           duration (-> (- end-time start-time)
                        (* 1e-9) ;; ns -> s
                        (/ N))]
       (println "Elapsed time:" (format "%f" duration) "s"))))

