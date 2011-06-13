(ns dce.test.ClojureException
  (:use [clojure.test]
        [dce.ClojureException :only [try+ throw+]]))

(defrecord oit-exception [error-code duration-ms message])

(def exception-1 (Exception. "exceptional"))
(def oit-exception-1 (oit-exception. 6 1000 "pdf failure"))

(defmacro mega-try [body]
  `(try+
    ~body
    (catch nil? e#
      [:nil e#])
    (catch Integer e#
      [:integer e#])
    (catch keyword? e#
      [:keyword e#])
    (catch symbol? e#
      [:symbol e#])
    (catch :oit-exception e#
      [:oit-exception-map e#])
    (catch oit-exception e#
      [:oit-exception-record e#])
    (catch map? e#
      [:map e# (meta e#)])
    (catch IllegalArgumentException e#
      [:iae e#])
    (catch Exception e#
      [:exception e#])))

(deftest test-try+
  (testing "throwing scalar types (to demonstrate genericity)"
    (is (= [:nil nil] (mega-try (throw+ nil))))
    (is (= [:integer 4] (mega-try (throw+ 4))))
    (is (= [:keyword :awesome] (mega-try (throw+ :awesome))))
    (is (= [:symbol 'yuletide] (mega-try (throw+ 'yuletide)))))
  (testing "wrapped exception"
    (is (= [:exception exception-1] (mega-try (throw+ exception-1)))))
  (testing "unwrapped exception (interop with normal throw)"
    (is (= [:exception exception-1] (mega-try (throw exception-1)))))
  (testing "catching a map by predicate"
    (is (= [:map {:error-code 4} nil] (mega-try (throw+ {:error-code 4})))))
  (testing "catching a map with metadata by predicate"
    (is (= [:map {:error-code 4} {:severity 4}]
           (mega-try (throw+ ^{:severity 4} {:error-code 4})))))
  (testing "catching a map with :type metadata by type"
    (is (= [:oit-exception-map {:error-code 5}]
           (mega-try (throw+ ^{:type :oit-exception} {:error-code 5})))))
  (testing "catching a record acting as a custom exception type"
    (is (= [:oit-exception-record oit-exception-1]
           (mega-try (throw+ (oit-exception. 6 1000 "pdf failure"))))))
  (testing "catching an organic IllegalArgumentException"
    (is (= :iae (first (mega-try (first 1)))))))
