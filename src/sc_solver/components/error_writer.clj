(ns sc-solver.components.error-writer
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]))

; Gets error messages from the error-chan and simply logs them.

(defn process-errors [status  error-chan]
  (async/go (while (= @status :running)
              (log/error (async/<! error-chan)))
            (async/close! error-chan)))

(defrecord Error-writer [status error-chan]
  component/Lifecycle
  (start [component]
    (reset! (:status component) :running)
    (process-errors status error-chan)
    component)
  (stop [component]
    (reset! (:status component) :stopped)
    component))

(defn new-error-writer [error-chan]
  (->Error-writer (atom :init) error-chan))