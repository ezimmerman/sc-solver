(ns sc-solver.components.network-solver
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [sc-solver.solver :as solver]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]))

; Expects an unsolved graph on the msg-chan then solves them pipelining
; with the :solver-procs configuration set in the profiles.clj file.

(def solver-procs (read-string (env :solver-procs)))
(def days (read-string (env :days)))

(defn process-graphs [msg-chan response-chan error-chan]
  (async/pipeline solver-procs response-chan (map (try #(solver/flow-for-days %1 days)
                                                       (catch Exception e (async/>!! error-chan e)))) msg-chan))

(defrecord Network-solver [msg-chan response-chan error-chan]
  component/Lifecycle
  (start [component]
    (process-graphs msg-chan response-chan error-chan)
    component)
  (stop [component]
    component))


(defn new-network-solver [msg-request-chan msg-response-chan error-chan]
  (->Network-solver msg-request-chan msg-response-chan error-chan))