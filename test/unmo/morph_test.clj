(ns unmo.morph-test
  (:require [clojure.test :refer :all]
            [unmo.morph :refer :all]))

(deftest noun?-test
  (testing "noun?は"
    (testing "形態素を表すベクタ [word part] を受け取り、品詞が名詞であれば真を返す"
      (let [text "あたしはプログラムの女の子です。"
            parts (analyze text)]
        (are [bool morph] (= bool (noun? morph))
          false (nth parts 0)
          false (nth parts 1)
          true  (nth parts 2)
          false (nth parts 3)
          true  (nth parts 4)
          false (nth parts 5)
          false (nth parts 6))))))

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
