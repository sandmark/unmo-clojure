(ns unmo.dictionary-test
  (:require [clojure.test :refer :all]
            [unmo.dictionary :refer :all]
            [unmo.morph :refer :all]))

(deftest study-test
  (let [study-random   #'unmo.dictionary/study-random
        study-pattern  #'unmo.dictionary/study-pattern
        study-template #'unmo.dictionary/study-template
        text  "あたしはプログラムの女の子です"
        parts (analyze text)]
    (testing "studyは"
      (testing "study-randomを呼び出す"
        (let [dictionary (study-random {} text)]
          (is (= (:random dictionary)
                 (:random (study {} text parts))))))

      (testing "study-patternを呼び出す"
        (let [dictionary (study-pattern {} text parts)]
          (is (= (:pattern dictionary)
                 (:pattern (study {} text parts))))))

      (testing "study-templateを呼び出す"
        (let [dictionary (study-template {} parts)]
          (is (= (:template dictionary)
                 (:template (study {} text parts)))))))))

(deftest study-template-test
  (let [study-template #'unmo.dictionary/study-template]
    (testing "study-templateは"
      (let [dictionary {}
            input "あたしはプログラムの女の子です"
            parts (analyze input)]
        (testing ":templateを持つMapを返す"
          (is (contains? (study-template dictionary parts) :template)))

        (testing "名詞の数をキーにする"
          (let [template (-> (study-template dictionary parts)
                             (get :template))]
            (is (contains? template 2))))

        (testing "発言の名詞部分を%noun%に変換する"
          (let [result (-> (study-template dictionary parts)
                           (get-in [:template 2])
                           (first))]
            (is (= "あたしは%noun%の%noun%です" result))))

        (testing "重複は学習しない"
          (let [result (study-template dictionary parts)
                doubled (study-template result parts)]
            (is (= result doubled))))))))

(deftest study-pattern-test
  (let [study-pattern #'unmo.dictionary/study-pattern]
    (testing "study-patternは発言inputと辞書dictionaryと形態素解析結果partsを受け取り"
      (let [expect {:pattern {"プログラム" ["あたしはプログラムの女の子です"]
                              "女の子"    ["あたしはプログラムの女の子です"]}}
            input  "あたしはプログラムの女の子です"
            parts  (analyze input)]
        (testing "{'名詞' ['名詞を含む文章']}の形式で学習する"
          (is (= expect (study-pattern {} input parts))))

        (testing "重複は学習しない"
          (is (= expect (study-pattern expect input parts))))))))

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
