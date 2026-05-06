(require 'harness)

(defn print+space [data]
  (print data) (print " "))

(defn rand-real [min max]
  (+ min (* (- max min) (rand))))
(defn clamp [n min max]
  (if (< n min)
    min
    (if (< max n)
      max
      n)))
(def pi 3.1415926535897932385)
(defn degrees->radians [deg]
  (/ (* deg pi) 180.0))

(defmacro sqrt [n]
  #?(:clj `(Math/sqrt ~n)
     :jank `(clojure.core-native/sqrt ~n)))

(defmacro tan [n]
  #?(:clj `(Math/tan ~n)
     :jank `(clojure.core-native/tan ~n)))

(defmacro pow [l r]
  #?(:clj `(Math/pow ~l ~r)
     :jank `(clojure.core-native/pow ~l ~r)))

(defn vec3-create [r g b]
  {:r r
   :g g
   :b b})
(defn vec3-scale [l n]
  {:r (* (:r l) n)
   :g (* (:g l) n)
   :b (* (:b l) n)})
(defn vec3-add [l r]
  {:r (+ (:r l) (:r r))
   :g (+ (:g l) (:g r))
   :b (+ (:b l) (:b r))})
(defn vec3-sub [l r]
  {:r (- (:r l) (:r r))
   :g (- (:g l) (:g r))
   :b (- (:b l) (:b r))})
(defn vec3-mul [l r]
  {:r (* (:r l) (:r r))
   :g (* (:g l) (:g r))
   :b (* (:b l) (:b r))})
(defn vec3-div [l n]
  {:r (/ (:r l) n)
   :g (/ (:g l) n)
   :b (/ (:b l) n)})
(defn vec3-length-squared [v]
  (+ (+ (* (:r v) (:r v))
        (* (:g v) (:g v)))
     (* (:b v) (:b v))))
(defn vec3-length [v]
  (sqrt (vec3-length-squared v)))
(defn vec3-dot [l r]
  (+ (+ (* (:r l) (:r r))
        (* (:g l) (:g r)))
     (* (:b l) (:b r))))
(defn vec3-cross [l r]
  (vec3-create (- (* (:g l) (:b r))
                  (* (:b l) (:g r)))
               (- (* (:b l) (:r r))
                  (* (:r l) (:b r)))
               (- (* (:r l) (:g r))
                  (* (:g l) (:r r)))))
(defn vec3-normalize [v]
  (vec3-div v (vec3-length v)))
(defn vec3-rand []
  (vec3-create (rand) (rand) (rand)))
(defn vec3-rand+clamp [min max]
  (vec3-create (rand-real min max) (rand-real min max) (rand-real min max)))
(defn vec3-rand-in-sphere []
  (let [v (vec3-rand+clamp -1 1)]
    (if (< 1.0 (vec3-length-squared v))
      v
      (vec3-rand-in-sphere))))
(defn vec3-rand-unit-in-sphere []
  (vec3-normalize (vec3-rand-in-sphere)))
(defn vec3-rand-in-unit-disk []
  (let [p (vec3-create (rand-real -1 1) (rand-real -1 1) 0)]
    (if (< 1 (vec3-length-squared p))
      (vec3-rand-in-unit-disk)
      p)))
(defn vec3-near-zero? [v]
  (let [epsilon 0.0000008]
    (and (and (< (abs (:r v)) epsilon)
              (< (abs (:g v)) epsilon))
         (< (abs (:b v)) epsilon))))

(defn vec3-reflect [v n]
  (vec3-sub v (vec3-scale n (* 2 (vec3-dot v n)))))
(defn vec3-refract [uv n etai-over-etat]
  (let [cos-theta (min (vec3-dot (vec3-sub (vec3-create 0 0 0)
                                           uv)
                                 n)
                       1.0)
        r-out-perp (vec3-scale (vec3-add uv (vec3-scale n cos-theta))
                               etai-over-etat)
        r-out-parallel (vec3-scale n (- 0.0 (sqrt (abs (- 1.0 (vec3-length-squared r-out-perp))))))]
    (vec3-add r-out-perp r-out-parallel)))
(defn vec3-print [v samples-per-pixel]
  (let [scale (/ 1.0 samples-per-pixel)
        r (sqrt (* scale (:r v)))
        g (sqrt (* scale (:g v)))
        b (sqrt (* scale (:b v)))]
    (print+space (int (* 256.0 (clamp r 0.0 0.999))))
    (print+space (int (* 256.0 (clamp g 0.0 0.999))))
    (print+space (int (* 256.0 (clamp b 0.0 0.999))))))

(defn ray-create [origin direction]
  {:origin origin
   :direction direction})
(defn ray-at [r t]
  (vec3-add (:origin r) (vec3-scale (:direction r) t)))

(defn reflectance [cosine ref-idx]
  (let [r (/ (- 1.0 ref-idx)
             (+ 1.0 ref-idx))
        r2 (* r r)]
    (* (+ r2 (- 1.0 r2))
       (pow (- 1.0 cosine) 5.0))))

(defn hit-info-create [point normal t material front-face?]
  {:point point
   :normal normal
   :t t
   :material material
   :front-face? front-face?})

(defn hit-sphere [hittable t-min t-max ray]
  (let [center (:center hittable)
        radius (:radius hittable)
        oc (vec3-sub (:origin ray) center)
        a (vec3-length-squared (:direction ray))
        half-b (vec3-dot oc (:direction ray))
        c (- (vec3-length-squared oc) (* radius radius))
        discriminant (- (* half-b half-b) (* a c))]
    (if (< discriminant 0)
      nil
      (let [sqrt-d (sqrt discriminant)
            root (let [root (/ (- (- 0 half-b) sqrt-d) a)]
                   (if (or (< root t-min) (< t-max root))
                     (/ (+ (- 0 half-b) sqrt-d) a)
                     root))]
        (if (or (< root t-min) (< t-max root))
          nil
          (let [point (ray-at ray root)
                outward-normal (vec3-div (vec3-sub point center) radius)
                front-face? (< (vec3-dot (:direction ray) outward-normal) 0.0)]
            (hit-info-create point
                             (if front-face?
                               outward-normal
                               (vec3-sub (vec3-create 0 0 0) outward-normal))
                             root
                             (:material hittable)
                             front-face?)))))))

(defn hit-all [t-min t-max ray hittables]
  (:hit-info
       (reduce (fn [acc hittable]
                 (let [hit-info (hit-sphere hittable
                                            t-min
                                            (:closest-so-far acc)
                                            ray)]
                   (if (some? hit-info)
                     (assoc (assoc acc :hit-info hit-info)
                            :closest-so-far (:t hit-info))
                     acc)))
               {:closest-so-far t-max
                :hit-info nil}
               hittables)))

(defn scatter-lambertian [ray hit-info]
  (let [scatter-direction (let [dir (vec3-add (:normal hit-info)
                                              (vec3-rand-unit-in-sphere))]
                            (if (vec3-near-zero? dir)
                              (:normal hit-info)
                              dir))
        scattered (ray-create (:point hit-info) scatter-direction)
        attenuation (:albedo (:material hit-info))]
    {:ray scattered
     :attenuation attenuation}))

(defn scatter-metal [ray hit-info]
  (let [material (:material hit-info)
        reflected (vec3-reflect (vec3-normalize (:direction ray))
                                (:normal hit-info))
        scattered (ray-create (:point hit-info)
                              (vec3-add reflected
                                        (vec3-scale (vec3-rand-unit-in-sphere)
                                                    (:fuzz material))))
        attenuation (:albedo material)
        res {:ray scattered
             :attenuation attenuation}]
    (if (< 0 (vec3-dot (:direction scattered) (:normal hit-info)))
      res
      nil)))

(defn scatter-dialetric [ray hit-info]
  (let [material (:material hit-info)
        attenuation (vec3-create 1 1 1)
        index-of-refraction (:index-of-refraction material)
        refraction-ratio (if (:front-face? hit-info)
                           (/ 1.0 index-of-refraction)
                           index-of-refraction)
        unit-direction (vec3-normalize (:direction ray))

        normal (:normal hit-info)
        cos-theta (min (vec3-dot (vec3-sub (vec3-create 0 0 0)
                                           unit-direction)
                                 normal)
                       1.0)
        sin-theta (sqrt (- 1.0 (* cos-theta cos-theta)))
        cannot-refract? (< 1.0 (* refraction-ratio sin-theta))
        direction (if (or cannot-refract?
                              (< (rand) (reflectance cos-theta refraction-ratio)))
                    (vec3-reflect unit-direction normal)
                    (vec3-refract unit-direction normal refraction-ratio))]
    {:ray (ray-create (:point hit-info) direction)
     :attenuation attenuation}))

(defn ray-cast [r max-ray-bounces hittables]
  (if (< max-ray-bounces 0)
    (vec3-create 0 0 0)
    (let [normalize-direction (vec3-normalize (:direction r))
          t (* 0.5 (+ (:g normalize-direction) 1.0))
          hit-info (hit-all 0.001 99999999 r hittables)]
      (if (some? hit-info)
        (let [material (:material hit-info)
              scatter-fn (:scatter material)
              scattered (scatter-fn r hit-info)]
          (if (some? scattered)
            (vec3-mul (ray-cast (:ray scattered)
                                (dec max-ray-bounces)
                                hittables)
                      (:attenuation scattered))
            (vec3-create 0 0 0)))
        (vec3-add (vec3-scale (vec3-create 1.0 1.0 1.0) (- 1.0 t))
                  (vec3-scale (vec3-create 0.5 0.7 1.0) t))))))

(defn rand-scene! []
  (reduce (fn [acc i]
            (let [x (- (mod i 21) 10)
                  z (- (/ i 21) 6)
                  choose-mat (rand)
                  center (vec3-create (+ x (* 0.9 (rand)))
                                      0.2
                                      (+ z (* 0.9 (rand))))]
              (if (< 0.9 (vec3-length (vec3-sub center (vec3-create 4 0.2 0))))
                (conj acc (if (< choose-mat 0.8)
                            {:center center
                             :radius 0.2
                             :material {:albedo (vec3-mul (vec3-rand) (vec3-rand))
                                        :scatter scatter-lambertian}}
                            (if (< choose-mat 0.95)
                              {:center center
                               :radius 0.2
                               :material {:albedo (vec3-rand+clamp 0.5 1)
                                          :fuzz (rand-real 0 0.5)
                                          :scatter scatter-metal}}
                              {:center center
                               :radius 0.2
                               :material {:index-of-refraction 1.5
                                          :scatter scatter-dialetric}})))
                acc)))
          [{:center (vec3-create 0 -1000 0)
            :radius 1000
            :material {:albedo (vec3-create 0.5 0.5 0.5)
                       :scatter scatter-lambertian}}
           {:center (vec3-create -4 1 0)
            :radius 1
            :material {:albedo (vec3-create 0.4 0.2 0.1)
                       :scatter scatter-lambertian}}
           {:center (vec3-create 0 1 0)
            :radius 1
            :material {:index-of-refraction 1.5
                       :scatter scatter-dialetric}}
           {:center (vec3-create 4 1 0)
            :radius 1
            :material {:albedo (vec3-create 0.7 0.6 0.5)
                       :fuzz 0
                       :scatter scatter-metal}}]
          (range 0 200)))

(defn ray []
  (let [aspect-ratio (/ 3.0 2.0)
        image-width 10
        image-height (int (/ image-width aspect-ratio))
        samples-per-pixel 2
        max-ray-bounces 10

        look-from (vec3-create 13 2 3)
        look-at (vec3-create 0 0 0)
        aperture 0.1
        lens-radius (/ aperture 2)
        focus-distance 10
        camera-up (vec3-create 0 1 0)
        field-of-view 20
        field-of-view-theta (degrees->radians field-of-view)
        viewport-height (* 2 (tan (/ field-of-view-theta 2.0)))
        viewport-width (* aspect-ratio viewport-height)
        _ (vec3-sub look-from look-at)
        camera-w (vec3-normalize (vec3-sub look-from look-at))
        camera-u (vec3-normalize (vec3-cross camera-up camera-w))
        camera-v (vec3-cross camera-w camera-u)

        origin look-from
        horizontal (vec3-scale camera-u (* viewport-width focus-distance))
        vertical (vec3-scale camera-v (* viewport-height focus-distance))
        lower-left-corner (vec3-sub (vec3-sub (vec3-sub origin (vec3-div horizontal 2))
                                              (vec3-div vertical 2))
                                    (vec3-scale camera-w focus-distance))

        hittables (rand-scene!)
        y-counter (reverse (range 0 image-height))
        x-counter (range 0 image-width)
        sample-counter (range 0 samples-per-pixel)]

    (println "P3")
    (print+space image-width) (println image-height)
    (println 255)
    (doseq [y y-counter]
      (doseq [x x-counter]
        (let [sample (reduce (fn [acc _sample-count]
                               (let [u (/ (+ x (rand)) (- image-width 1))
                                     v (/ (+ y (rand)) (- image-height 1))
                                     rd (vec3-scale (vec3-rand-in-unit-disk) lens-radius)
                                     offset (vec3-create 0 0 0)
                                     ray (ray-create (vec3-add origin offset)
                                                     (vec3-sub (vec3-add (vec3-add lower-left-corner
                                                                                   (vec3-scale horizontal u))
                                                                         (vec3-scale vertical v))
                                                               (vec3-sub origin offset)))]
                                 (vec3-add acc (ray-cast ray max-ray-bounces hittables))))
                             (vec3-create 0 0 0)
                             sample-counter)]
          (vec3-print sample samples-per-pixel))))))

(harness/bench (ray))
