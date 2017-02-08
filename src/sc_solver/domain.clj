(ns sc-solver.domain)

(defprotocol Attributes
  "Return the types attributes"
  (attributes [x]))

(defrecord Vendor [name type]
  Attributes
  (attributes [x]
    (hash-map)))

(defrecord Dc [name type]
  Attributes
  (attributes [x]
    (hash-map)))

(defrecord Store [name type inventory target]
  Attributes
  (attributes [x]
    (hash-map :name name :type type :target target)))

(defrecord Schedule [product
                     source
                     destination
                     frequency
                     lead-time])

(defrecord Constraint [date
                       max])

(defrecord Orderplan [date
                      source
                      destination
                      qty])