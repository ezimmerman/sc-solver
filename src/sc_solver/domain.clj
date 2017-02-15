(ns sc-solver.domain)

(defprotocol Attributes
  "Return the types attributes"
  (attributes [x]))

; Todo remove requirement to have flow-amount
(defrecord Vendor [name type product]
  Attributes
  (attributes [x]
    (hash-map :name name :type type :product product)))

(defrecord Dc [name type]
  Attributes
  (attributes [x]
    (hash-map :name name :type type)))

(defrecord Store [name type inventory target]
  Attributes
  (attributes [x]
    (hash-map :name name :type type :target target :inventory inventory)))

(defrecord Schedule [product
                     source
                     destination
                     frequency
                     lead-time])

(defrecord Constraint [date
                       max])

(defrecord Orderplan [product
                      ship-date
                      source
                      destination
                      qty])