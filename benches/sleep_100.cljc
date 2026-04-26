(defn do-sleep []
  #?(:clj (Thread/sleep 100)
     :bb (Thread/sleep 100)
     :jank (cpp/std.this_thread.sleep_for
            (cpp/std.chrono.milliseconds #cpp 100))))

(dotimes [_ 10]
  (time (do-sleep)))
