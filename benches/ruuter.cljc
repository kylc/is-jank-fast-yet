;; Aggregate benchmark of ruuter routing library, largely copied from the ruuter
;; benchmark suite itself.

(require 'harness)
(require 'ruuter.core)

(def small-routes
  "5 routes — typical small app."
  [{:path "/" :method :get
    :response {:status 200 :body "home"}}
   {:path "/about" :method :get
    :response {:status 200 :body "about"}}
   {:path "/users/:id" :method :get
    :response (fn [req] {:status 200 :body (str "user " (:id (:params req)))})}
   {:path "/users/:id/posts/:post-id" :method :get
    :response (fn [_req] {:status 200 :body "post"})}
   {:path "/files/:path*" :method :get
    :response (fn [_req] {:status 200 :body "file"})}])

(defn- generate-routes
  "Generate `n` routes of the form /prefix-N/:id."
  [n]
  (vec
   (concat
    [{:path "/" :method :get :response {:status 200 :body "home"}}]
    (for [i (range n)]
      {:path (str "/section-" i "/:id")
       :method :get
       :response (fn [_req] {:status 200 :body (str "section " i)})})
    [{:path "/:catch-all*" :method :get
      :response (fn [_req] {:status 200 :body "catch all"})}])))

(def medium-routes (generate-routes 50))
(def large-routes (generate-routes 200))

(defn do-routing []
  (dotimes [_ 1000]
    (ruuter.core/route small-routes {:uri "/" :request-method :get})
    (ruuter.core/route small-routes {:uri "/about" :request-method :get})
    (ruuter.core/route small-routes {:uri "/users/42" :request-method :get})
    (ruuter.core/route small-routes {:uri "/users/42/posts/7" :request-method :get})
    (ruuter.core/route small-routes {:uri "/files/a/b/c/d.txt" :request-method :get})
    (ruuter.core/route small-routes {:uri "/nope" :request-method :get})
    (ruuter.core/route medium-routes {:uri "/" :request-method :get})
    (ruuter.core/route medium-routes {:uri "/section-25/42" :request-method :get})
    (ruuter.core/route medium-routes {:uri "/section-49/42" :request-method :get})
    (ruuter.core/route medium-routes {:uri "/unknown/path" :request-method :get})
    (ruuter.core/route large-routes {:uri "/" :request-method :get})
    (ruuter.core/route large-routes {:uri "/section-100/42" :request-method :get})
    (ruuter.core/route large-routes {:uri "/section-199/42" :request-method :get})
    (ruuter.core/route large-routes {:uri "/nothing" :request-method :post})))

(harness/bench (do-routing))
