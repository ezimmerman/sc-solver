(ns sc-solver.components.order-plan-writer
  (:require [cheshire.core :refer :all]
            [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [environ.core :refer [env]]))
; Write the order plans to the file system
; Gets the location to write to from the :plans configureation in
; profiles.clj

(def op-directory
  (env :plans))

(defn write-order-plans [ops]
  (let [product (:product (first (first ops)))
        file-name (str op-directory "order-plan-product-" product ".json")
        op-json (generate-string ops {:pretty true})]
    (spit file-name op-json)))

(defn process-order-plans [status msg-chan error-chan]
  (async/go (while (= @status :running)
              (try (write-order-plans (async/<! msg-chan))
                   (catch Exception e (async/>! error-chan e))))
            (async/close! msg-chan)))

(defrecord Order-plan-writer [status msg-chan error-chan]
  component/Lifecycle
  (start [component]
    (reset! (:status component) :running)
    (process-order-plans status msg-chan error-chan)
    component)
  (stop [component]
    (reset! (:status component) :stopped)
    component))

(defn new-order-plan-writer [msg-request-chan error-chan]
  (->Order-plan-writer (atom :init) msg-request-chan error-chan))