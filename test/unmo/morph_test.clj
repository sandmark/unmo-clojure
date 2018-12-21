(ns unmo.morph-test
  (:require [clojure.test :refer :all]
            [unmo.morph :refer :all]))

(deftest analyze-test
  (testing "analyzeは"
    (let [text "私は医療品安全管理責任者です"]
      (testing "形態素解析を行う"
        (is (= ["私" "は" "医療" "品" "安全" "管理責任者" "です"]
               (->> (analyze text) (map first))))
        (is (= ["代名詞" "助詞" "名詞" "接尾辞" "名詞" "名詞" "助動詞"]
               (->> (analyze text)
                    (map (fn [[word part]]
                           (-> part
                               (clojure.string/split #",")
                               (first)))))))
        (are [n mode] (= n (count (analyze text mode)))
          9 :a
          8 :b
          7 :c))
      (testing "textのみ与えられた場合、split-mode-cで形態素解析を行う"
        (is (= (analyze text :c)
               (analyze text)))))))
