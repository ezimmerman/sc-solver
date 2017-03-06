(ns services.test-forecast
  (:require [clojure.test :refer :all]
            [sc-solver.services.forecast :as f]
            [clj-time.core :as t]))


(deftest test-get-forecast
  ; We shouldn't ever get greater then 25% of the inventory number.
  (is (> 26 (f/get-forecast 0 1 (t/today-at-midnight) 100))))