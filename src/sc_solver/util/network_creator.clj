(ns sc-solver.util.network-creator
  (:require [sc-solver.domain :refer :all]
            [environ.core :refer [env]]))

(def stores
  (read-string (env :stores)))

(def dcs
  (read-string (env :dcs)))

(def days
  (read-string (env :days)))

(def products
  (read-string (env :products)))

(defn make-vendor
  [number]
  (->Vendor "vendor" "vendor" number))

(defn make-store
  [number inventory target]
  (->Store (str "store" "-" number) "store" inventory target))

(defn make-dc
  [number]
  (->Dc (str "dc" "-" number) "dc"))

(defn- make-schedule
  "creates in individual d/Schedule"
  [product source destination]
  (->Schedule product source destination "DAILY" (rand-int 5)))

(defn make-schedules
  "Returns a vector of d/Schedule. Evenly distribute the number of stores across the dcs."
  [product vendor dcs stores]
  (let [vendor-dcs (map #(make-schedule product vendor %) dcs)
        partitioned-stores (partition (count dcs) stores)
        dcs-stores (mapcat (fn [dc stores]
                             (map (fn [store] (make-schedule product dc store)) stores)) dcs partitioned-stores)]
    (lazy-cat vendor-dcs dcs-stores)))


(defn network
  "Make a network based on the product, and how many DCs you want and stores per DC."
  [product number-dcs number-stores]
  (let [vendor (make-vendor product)
        dcs (map #(make-dc %) (range 0 number-dcs))
        stores (map #(make-store % (rand-int 100) (rand-int 100)) (range 0 number-stores))]
    (make-schedules product vendor dcs stores))
  )

;Todo support a range of days accross products.
(defn make-network
  "Reads in env and creates a seq of schedules"
  []
  (mapcat #(network % dcs stores) (range 0 products)))