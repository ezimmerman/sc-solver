(ns sc-solver.components.order-plan-writer
  (:require [cheshire.core :refer :all]
            [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [environ.core :refer [env]]))

(def op-directory
   (env :plans))

(defn write-order-plans [ops]
  (let [product (:product (first ops))
        file-name (str  op-directory "order-plan-product-" product ".json")
        op-json (generate-string ops)]
    (spit file-name op-json)))

(defn process-order-plans [status msg-chan]
  (async/go (while (= @status :running)
              (write-order-plans (async/<! msg-chan)))
            (async/close! msg-chan)))

(defrecord Order-plan-writer [status msg-chan]
  component/Lifecycle
  (start [component]
    (reset! (:status component) :running)
    (process-order-plans status msg-chan)
    component)
  (stop [component]
    (reset! (:status component) :stopped)
    component))

(defn new-order-plan-writer [msg-request-chan]
  (->Order-plan-writer (atom :init) msg-request-chan))