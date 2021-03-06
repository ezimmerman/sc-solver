(ns sc-solver.test-solver
  (:require [clojure.test :refer :all]
            [ubergraph.core :as uber]
            [ubergraph.alg :as alg]
            [sc-solver.solver :as s]
            [clj-time.core :as t]))


(def graph-1 (-> (uber/digraph
                   [:vendor {:type "vendor" :name "vendor" :product 0 :day (t/today-at-midnight)}]
                   [:dc-0 {:name "dc-0" :type "dc"}]
                   [:store-0 {:inventory 20 :target 50 :type "store"}]
                   [:store-1 {:inventory 50 :target 50 :type "store"}]
                   [:store-2 {:inventory 30 :target 50 :type "store"}])
                 (uber/add-directed-edges [:vendor :dc-0 {:flow-amount 0 :lead-time 1 :max nil}]
                                          [:dc-0 :store-0 {:flow-amount 0 :lead-time 2 :max nil}]
                                          [:dc-0 :store-1 {:flow-amount 0 :lead-time 2 :max nil}]
                                          [:dc-0 :store-2 {:flow-amount 0 :lead-time 3 :max nil}])
                 ))

(def graph-deep (-> (uber/digraph
                   [:vendor {:type "vendor" :name "vendor" :product 0 :day (t/today-at-midnight)}]
                   [:dc-0 {:name "ndc-0" :type "dc"}]
                   [:dc-1 {:name "dc-1" :type "dc"}]
                   [:store-0 {:inventory 20 :target 50 :type "store"}]
                   [:store-1 {:inventory 50 :target 50 :type "store"}]
                   [:store-2 {:inventory 30 :target 50 :type "store"}])
                 (uber/add-directed-edges [:vendor :dc-0 {:flow-amount 0 :lead-time 1 :max nil}]
                                          [:dc-0 :dc-1 {:flow-amount 0 :lead-time 1 :max nil}]
                                          [:dc-1 :store-0 {:flow-amount 0 :lead-time 2 :max nil}]
                                          [:dc-1 :store-1 {:flow-amount 0 :lead-time 2 :max nil}]
                                          [:dc-1 :store-2 {:flow-amount 0 :lead-time 3 :max nil}])
                 ))

(def graph-dc-store-constrained (-> (uber/digraph
                                      [:vendor {:type "vendor" :name "vendor" :product 0}]
                                      [:dc-0 {:name "dc-0" :type "dc"}]
                                      [:store-0 {:inventory 2 :target 5 :type "store"}]
                                      [:store-1 {:inventory 5 :target 5 :type "store"}]
                                      [:store-2 {:inventory 3 :target 5 :type "store"}])
                                    (uber/add-directed-edges [:vendor :dc-0 {:flow-amount 0 :lead-time 1 :max nil}]
                                                             [:dc-0 :store-0 {:flow-amount 0 :lead-time 2 :max 1}]
                                                             [:dc-0 :store-1 {:flow-amount 0 :lead-time 2 :max nil}]
                                                             [:dc-0 :store-2 {:flow-amount 0 :lead-time 3 :max nil}])
                                    ))

(def graph-vendor-dc-constrained (-> (uber/digraph
                                       [:vendor {:type "vendor" :name "vendor" :product 0}]
                                       [:dc-0 {:name "dc-0" :type "dc"}]
                                       [:store-0 {:inventory 2 :target 5 :type "store"}]
                                       [:store-1 {:inventory 5 :target 5 :type "store"}]
                                       [:store-2 {:inventory 3 :target 5 :type "store"}])
                                     (uber/add-directed-edges [:vendor :dc-0 {:flow-amount 0 :lead-time 1 :max 1}]
                                                              [:dc-0 :store-0 {:flow-amount 0 :lead-time 2 :max nil}]
                                                              [:dc-0 :store-1 {:flow-amount 0 :lead-time 2 :max nil}]
                                                              [:dc-0 :store-2 {:flow-amount 0 :lead-time 3 :max nil}])
                                     ))
(def graph-large (-> (uber/digraph
                       [:vendor {:type "vendor" :name "vendor" :product 0}]
                       [:dc-0 {:name "dc-0" :type "dc"}]
                       [:dc-1 {:name "dc-1" :type "dc"}]
                       [:store-0 {:inventory 2 :target 5 :type "store"}]
                       [:store-1 {:inventory 5 :target 5 :type "store"}]
                       [:store-2 {:inventory 3 :target 5 :type "store"}]
                       [:store-3 {:inventory 3 :target 5 :type "store"}]
                       [:store-4 {:inventory 3 :target 10 :type "store"}]
                       [:store-5 {:inventory 3 :target 5 :type "store"}]
                       [:store-6 {:inventory 3 :target 5 :type "store"}]
                       [:store-7 {:inventory 3 :target 5 :type "store"}]
                       [:store-8 {:inventory 3 :target 25 :type "store"}]
                       [:store-9 {:inventory 3 :target 5 :type "store"}]
                       [:store-10 {:inventory 3 :target 5 :type "store"}]
                       [:store-11 {:inventory 2 :target 5 :type "store"}]
                       [:store-12 {:inventory 5 :target 5 :type "store"}]
                       [:store-13 {:inventory 3 :target 5 :type "store"}]
                       [:store-14 {:inventory 3 :target 5 :type "store"}]
                       [:store-15 {:inventory 3 :target 35 :type "store"}]
                       [:store-16 {:inventory 3 :target 8 :type "store"}]
                       [:store-17 {:inventory 3 :target 5 :type "store"}]
                       [:store-18 {:inventory 3 :target 12 :type "store"}]
                       [:store-19 {:inventory 3 :target 5 :type "store"}]
                       [:store-20 {:inventory 3 :target 5 :type "store"}]
                       [:store-21 {:inventory 3 :target 50 :type "store"}])
                     (uber/add-directed-edges [:vendor :dc-0 {:flow-amount 0 :lead-time 1 :max nil}]
                                              [:vendor :dc-1 {:flow-amount 0 :lead-time 1 :max nil}]
                                              [:dc-0 :store-0 {:flow-amount 0 :lead-time 2 :max nil}]
                                              [:dc-0 :store-1 {:flow-amount 0 :lead-time 2 :max nil}]
                                              [:dc-0 :store-2 {:flow-amount 0 :lead-time 3 :max nil}]
                                              [:dc-0 :store-3 {:flow-amount 0 :lead-time 3 :max nil}]
                                              [:dc-0 :store-4 {:flow-amount 0 :lead-time 3 :max nil}]
                                              [:dc-0 :store-5 {:flow-amount 0 :lead-time 3 :max nil}]
                                              [:dc-0 :store-6 {:flow-amount 0 :lead-time 3 :max nil}]
                                              [:dc-0 :store-7 {:flow-amount 0 :lead-time 10 :max nil}]
                                              [:dc-0 :store-8 {:flow-amount 0 :lead-time 3 :max nil}]
                                              [:dc-0 :store-9 {:flow-amount 0 :lead-time 3 :max nil}]
                                              [:dc-0 :store-10 {:flow-amount 0 :lead-time 3 :max nil}]
                                              [:dc-1 :store-11 {:flow-amount 0 :lead-time 2 :max nil}]
                                              [:dc-1 :store-12 {:flow-amount 0 :lead-time 2 :max nil}]
                                              [:dc-1 :store-13 {:flow-amount 0 :lead-time 3 :max nil}]
                                              [:dc-1 :store-14 {:flow-amount 0 :lead-time 8 :max nil}]
                                              [:dc-1 :store-15 {:flow-amount 0 :lead-time 3 :max nil}]
                                              [:dc-1 :store-16 {:flow-amount 0 :lead-time 3 :max nil}]
                                              [:dc-1 :store-17 {:flow-amount 0 :lead-time 5 :max nil}]
                                              [:dc-1 :store-18 {:flow-amount 0 :lead-time 3 :max nil}]
                                              [:dc-1 :store-19 {:flow-amount 0 :lead-time 4 :max nil}]
                                              [:dc-1 :store-20 {:flow-amount 0 :lead-time 3 :max nil}]
                                              [:dc-1 :store-21 {:flow-amount 0 :lead-time 3 :max nil}])
                     ))


(deftest test-get-flow-amount
  (is (= 0 (s/get-flow-amount graph-1 (uber/find-edge graph-1 :dc-0 :store-0)))))

(deftest test-get-existing-inventory
  (is (= 20 (s/get-existing-inventory graph-1 (uber/find-edge graph-1 :dc-0 :store-0)))))


(deftest test-get-target-inventory
  (is (= 50 (s/get-target-inventory graph-1 (uber/find-edge graph-1 :dc-0 :store-0)))))

(deftest test-utility-fn
  (let [utility-fn (s/profit-tier-utility-fn graph-1)]
    (is (= 5/3 (utility-fn (uber/find-edge graph-1 :dc-0 :store-0))))))

(deftest is-edge-valid-fn
  (let [is-edge-valid-1? (s/profit-tier-is-edge-valid-fn graph-1)
        edge-1 (uber/find-edge graph-1 :dc-0 :store-0)
        graph-2 (uber/add-attr graph-1 edge-1 :flow-amount (+ (uber/attr graph-1 edge-1 :flow-amount) 30))
        graph-3 (uber/add-attr graph-1 edge-1 :max 0)
        edge-3 (uber/find-edge graph-3 :dc-0 :store-0)
        edge-2 (uber/find-edge graph-2 :dc-0 :store-0)
        is-edge-valid-2? (s/profit-tier-is-edge-valid-fn graph-2)
        is-edge-valid-3? (s/profit-tier-is-edge-valid-fn graph-3)]
    (is (= true (is-edge-valid-1? edge-1)))
    (is (= false (is-edge-valid-2? edge-2)))
    (is (= false (is-edge-valid-3? edge-3)))))

(deftest is-end-node-fn
  (let [dc-store-0-edge (uber/find-edge graph-1 :dc-0 :store-0)
        store-0-node (uber/dest dc-store-0-edge)
        vendor-dc-0-edge (uber/find-edge graph-1 :vendor :dc-0)
        dc-node (uber/dest vendor-dc-0-edge)
        is-end-node (s/is-profit-tier-end-node-fn graph-1)]
    (is (= true (is-end-node store-0-node)))
    (is (= false (is-end-node dc-node)))))

(deftest test-is-dest-node-store?
  (is (= true (s/is-dest-node-store? graph-1 (uber/find-edge graph-1 :dc-0 :store-0)))))

(deftest test-incremment-flow-amounts
  (let [path (alg/shortest-path graph-1 {:start-node :vendor :end-nodes [:store-0 :store-1 :store-2] :cost-fn (s/profit-tier-utility-fn graph-1)})
        graph-2 (s/incremment-flow-amounts path graph-1)
        edges (alg/edges-in-path path)]
    (is (= 1 (uber/attr graph-2 (uber/find-edge graph-1 :vendor :dc-0) :flow-amount)))
    (is (= 1 (uber/attr graph-2 (uber/find-edge graph-1 :dc-0 :store-0) :flow-amount)))
    (is (= 0 (uber/attr graph-2 (uber/find-edge graph-1 :dc-0 :store-1) :flow-amount)))
    (is (= 0 (uber/attr graph-2 (uber/find-edge graph-1 :dc-0 :store-2) :flow-amount)))))

(deftest test-is-edge-under-max-constraint
  (let [edge-1 (uber/find-edge graph-1 :dc-0 :store-0)
        graph-2 (uber/add-attr graph-1 edge-1 :max 1)
        edge-2 (uber/find-edge graph-2 :dc-0 :store-0)
        graph-3 (uber/add-attr graph-2 edge-2 :flow-amount (+ (uber/attr graph-2 edge-2 :flow-amount) 1))
        edge-3 (uber/find-edge graph-3 :dc-0 :store-0)]
    (is (= true (s/is-edge-under-max-constraint? graph-1 edge-1)))
    (is (= true (s/is-edge-under-max-constraint? graph-2 edge-2)))
    (is (= false (s/is-edge-under-max-constraint? graph-3 edge-3)))))

(deftest test-edges-to-end-node-valid
  (let [edge-0 (uber/find-edge graph-1 :dc-0 :store-0)
        edge-1 (uber/find-edge graph-dc-store-constrained :dc-0 :store-0)
        graph-2 (uber/add-attr graph-dc-store-constrained edge-1 :flow-amount (+ (uber/attr graph-dc-store-constrained edge-1 :flow-amount) 1))]
    (is (= false (s/is-edges-to-end-node-valid? graph-2 (uber/dest edge-1))))
    (is (= true (s/is-edges-to-end-node-valid? graph-1 (uber/dest edge-0))))))

(deftest test-have-end-nodes
  (let [edge-1 (uber/find-edge graph-vendor-dc-constrained :vendor :dc-0)
        graph-2 (uber/add-attr graph-vendor-dc-constrained edge-1 :flow-amount (+ (uber/attr graph-vendor-dc-constrained edge-1 :flow-amount) 1))]
    (is (= true (s/have-end-nodes? graph-vendor-dc-constrained)))
    (is (= false (s/have-end-nodes? graph-2)))))


(deftest test-increment-vendor-date
  (let [graph-plus-day (uber/add-attr graph-1 :vendor :day (t/today-at-midnight))
        graph-incremented-day (s/increment-vendor-date graph-plus-day)
        day-val (uber/attr  graph-incremented-day :vendor :day)]
    (is (= (t/plus (t/today-at-midnight) (t/days 1)) day-val))))

(deftest test-update-nodes-inventory
  (let [store-node (first (s/get-store-nodes graph-1))
        updated-graph (s/update-nodes-inventory graph-1 store-node 100)
        updated-store-node-inv (uber/attr updated-graph store-node :inventory)]
    (is (= updated-store-node-inv 100))))

(deftest test-update-store-inventory
  (let [edge-to-store (first (s/get-store-edges graph-1))
        updated-graph (s/update-store-inventory graph-1 edge-to-store)
        updated-store (uber/dest (uber/find-edge updated-graph (uber/src edge-to-store) (uber/dest edge-to-store)))]
    (is (not= (s/get-inventory graph-1 (uber/dest edge-to-store)) (s/get-inventory updated-graph updated-store)))))

(deftest test-update-inventory
  (let [edge-to-store (first (s/get-store-edges graph-1))
        updated-graph (s/update-inventory graph-1)
        updated-store (uber/dest (uber/find-edge updated-graph (uber/src edge-to-store) (uber/dest edge-to-store)))]
    (is (not= (s/get-inventory graph-1 (uber/dest edge-to-store)) (s/get-inventory updated-graph updated-store)))))

(deftest test-clear-flow-amounts
  (let [cleared-flow-amounts (s/clear-flow-amounts graph-1)]
    (is (= nil (s/get-inventory cleared-flow-amounts (first (s/get-store-edges graph-1)))))))

