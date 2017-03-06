(ns sc-solver.components.order-plan-creator
  (:require [sc-solver.domain :refer :all]
            [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [sc-solver.solver :as solver]
            [ubergraph.core :as uber]
            [ubergraph.alg :as alg]
            [clojure.tools.logging :as log]
            [clj-time.coerce :as c])
  (:import (java.util Date)))


(defn create-plan [graph graph-edge]
  (let [src-node (uber/src graph-edge)
        dest-node (uber/dest graph-edge)
        attrs-map (uber/attrs graph graph-edge)]
    (->Orderplan (uber/attr graph src-node :product)
                 (c/to-date(solver/get-day graph))
                 (uber/attr graph src-node :name)
                 (uber/attr graph dest-node :name)
                 (:flow-amount attrs-map))))

(defn create-plans-for-graph
  "Returns a seq of order plans from vendor to DC(s). Expects a graph."
  [graph]
  (let [edges (uber/out-edges graph (solver/get-vendor graph))]
    (map #(create-plan graph %) edges)))

(defn create-plans
  "Returns a vector of order plans.  Takes a seq of graphs."
  [graphs]
  (map create-plans-for-graph graphs))

(defn process-graphs [status msg-chan response-chan error-chan]
  (async/go (while (= @status :running)
              (try (let [graph (async/<! msg-chan)
                         order-plans (create-plans graph)]
                     (async/>! response-chan order-plans))
                   (catch Exception e (async/>! Exception e))))
            (async/close! msg-chan)))

(defrecord Order-plan-creator [status msg-chan msg-response-chan error-chan]
  component/Lifecycle
  (start [component]
    (reset! (:status component) :running)
    (process-graphs status msg-chan msg-response-chan error-chan)
    component)
  (stop [component]
    (reset! (:status component) :stopped)
    component))

(defn new-order-plan-creator [msg-request-chan msg-response-chan error-chan]
  (->Order-plan-creator (atom :init) msg-request-chan msg-response-chan error-chan))
