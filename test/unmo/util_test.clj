(ns unmo.util-test
  (:require [clojure.test :refer :all]
            [unmo.util :refer :all]))

(deftest conj-unique-test
  (testing "conj-uniqueは"
    (testing "要素をコレクションに追加する"
      (is (= [:x]
             (conj-unique [] :x))))

    (testing "コレクションに存在する要素は追加しない"
      (is (= [:x]
             (conj-unique [:x] :x))))))

