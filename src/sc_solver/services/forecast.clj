(ns sc-solver.services.forecast)

; Simple stand in forecast service for the real thing.
; It's implementation is to simply return the sales forecast as
; anywhere from 0% to 25% of inventory.
;todo this interface isn't reasonable.  We don't want inventory.

(defn get-forecast
  "Given a product, location and day, return the sales forecast"
  [product location day inventory]
  (int (* inventory (* (rand-int 75) 0.01))))