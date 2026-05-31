(require 'harness)

;; hist1d function copied from jank.perf
(defn hist1d
  "Compute bins of an equal-width histogram."
  [xs min max nbins]
  (let [width (/ (- max min) nbins)]
    {:min   min
     :max   max
     :width width
     :bins  (persistent!
             (reduce
              (fn [acc x]
                (if (and (<= min x) (< x max))
                  (let [k (int (/ (- x min) width))]
                    (assoc! acc k (inc (get acc k))))
                  acc))
              (transient (vec (repeat nbins 0)))
              xs))}))

(let [N  100000
      xs (vec (repeatedly N rand))
      lb 0.0
      ub 1.0]
  (harness/bench (hist1d xs lb ub 100)))
