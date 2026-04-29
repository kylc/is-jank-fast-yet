(require 'harness)

(defn do-sleep [ms]
  #?(:clj (Thread/sleep ms)
     :bb (Thread/sleep ms)
     :jank (cpp/std.this_thread.sleep_for
            (cpp/std.chrono.milliseconds (cpp/int ms)))))

(harness/bench (do-sleep 100))
