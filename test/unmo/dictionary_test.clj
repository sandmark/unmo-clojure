(ns unmo.dictionary-test
  (:require [clojure.test :refer :all]
            [unmo.dictionary :refer :all]))

(deftest study-random-test
  (let [study-random #'unmo.dictionary/study-random]
    (testing "study-randomは"
      (testing "発言inputを学習した辞書dictionaryを返す"
        (is (= {:random ["test"]}
               (study-random {} "test"))))

      (testing "重複した発言は学習しない"
        (is (= {:random ["test"]}
               (study-random {:random ["test"]} "test")))))))

(deftest load-dictionary-test
  (testing "loadは"
    (testing "ファイルが存在しない場合、空の辞書を返す"
      (is (= {} (load-dictionary "dummy.txt"))))))
