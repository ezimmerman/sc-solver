(ns sc-solver.components.assemble-network
  (:require [com.stuartsierra.component :as component]
            [sc-solver.domain :refer :all]
            [clojure.core.async :as async]
            [ubergraph.core :as uber]
            [ubergraph.alg :as alg]))


(defn create-nodes-for-schedule
  "Given a schedule, break them into a vector of nodes with attrs."
  [schedule]
  (let [{:keys [source destination]} schedule]
    [[(keyword (:name source)) (attributes source)] [(keyword (:name destination)) (attributes destination)]]))

(defn create-edges-for-schedule
  "Given a schedule, create and connect source and destination from schedule."
  [schedule]
  (let [{:keys [source destination]} schedule]
    [(keyword (:name source)) (keyword (:name destination)) (hash-map :lead-time (:lead-time schedule) :flow-amount 0)]))


(defn assemble-network
  [schedules]
  (-> (uber/digraph)
      (uber/add-nodes-with-attrs* (mapcat create-nodes-for-schedule schedules))
      (uber/add-directed-edges* (map create-edges-for-schedule schedules))))

;Todo externalize the number of processes.
(defn process-schedules [status msg-chan response-chan]
  "Expects the msg to be a map of product number to schedules. Sends a created network on the response channel."
  (async/pipeline 4 response-chan (map assemble-network) msg-chan))


(defrecord Assemble-network [status msg-chan response-chan]
  component/Lifecycle
  (start [component]
    (reset! (:status component) :running)
    (process-schedules status msg-chan response-chan)
    component)
  (stop [component]
    (reset! (:status component) :stopped)
    component))

(defn new-assemble-network [msg-request-chan msg-response-chan]
  (->Assemble-network (atom :init) msg-request-chan msg-response-chan))