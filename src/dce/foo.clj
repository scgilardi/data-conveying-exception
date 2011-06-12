(ns dce.foo
  (:use dce.ClojureException))

(defn test-func [x y]
  (try+ (let [a 1 b 2]
          (throw+ :hi))
        (catch keyword? e
          (prn e)
          (prn &thrown-context)
          (clojure.pprint/pprint (:stack &thrown-context)))))
