(ns unmo.dictionary-test
  (:require [clojure.test :refer :all]
            [unmo.dictionary :refer :all]
            [unmo.morph :refer :all]))

(deftest study-test
  (let [study-random   #'unmo.dictionary/study-random
        study-pattern  #'unmo.dictionary/study-pattern
        study-template #'unmo.dictionary/study-template
        study-markov   #'unmo.dictionary/study-markov
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
                 (:template (study {} text parts))))))

      (testing "study-markovを呼び出す"
        (let [dictionary (study-markov {} parts)]
          (is (= (:markov dictionary)
                 (:markov (study {} text parts)))))))))

(deftest parts->markov-test
  (let [parts->markov #'unmo.dictionary/parts->markov]
    (testing "parts->markovは"
      (let [dictionary {"あたし" {"は" ["プログラム"]},
                        "は" {"プログラム" ["の"]},
                        "プログラム" {"の" ["女の子"]},
                        "の" {"女の子" ["です"]},
                        "女の子" {"です" ["%ENDMARK%"]}}
            merged     {"あたし" {"は" ["プログラム"] "が" ["好き"]}
                        "は" {"プログラム" ["の"] "おしゃべり" ["と"]}
                        "プログラム" {"の" ["女の子"]}
                        "の" {"女の子" ["です"] "は" ["おしゃべり"]}
                        "女の子" {"です" ["%ENDMARK%"]}
                        "が" {"好き" ["な"]}
                        "好き" {"な" ["の"]}
                        "な" {"の" ["は"]}
                        "おしゃべり" {"と" ["月餅"]}
                        "と" {"月餅" ["です"]}
                        "月餅" {"です" ["%ENDMARK%"]}}]
        (testing "形態素解析結果をマルコフ辞書形式に変換する"
          (let [parts (analyze "あたしはプログラムの女の子です")]
            (is (= dictionary (parts->markov {} parts)))))

        (testing "初期値を指定した場合、マージされた辞書を返す"
          (let [parts (analyze "あたしが好きなのはおしゃべりと月餅です")]
            (is (= merged (parts->markov dictionary parts)))))))))

(deftest study-markov-test
  (let [study-markov #'unmo.dictionary/study-markov]
    (testing "study-markovは"
      (testing "3単語未満の文章は学習しない"
        (let [result (->> "もういや"
                          (analyze)
                          (study-markov {}))]
          (is (= {} result))))

      (testing ":startsに「文章の開始単語」を記録する"
        (let [result (->> "あたしはプログラムの女の子です"
                          (analyze)
                          (study-markov {}))]
          (is (contains? (get result :markov) :starts))
          (is (= 1 (get-in result [:markov :starts "あたし"])))))

      (testing ":startsの「文章の開始単語」は学習のたびに加算される"
        (let [result (-> {}
                         (study-markov (analyze "あたしはプログラムの女の子です"))
                         (study-markov (analyze "あたしが好きなのはおしゃべりと月餅です")))]
          (is (= 2 (get-in result [:markov :starts "あたし"])))))

      (testing ":dictionaryにマルコフ辞書を記録する"
        (let [result (->> "あたしはプログラムの女の子です"
                          (analyze)
                          (study-markov {}))
              markov (:markov result)
              markov-dict (get-in result [:markov :dictionary])
              prefix-count 5]
          (is (contains? markov :dictionary))
          (is (= prefix-count (count markov-dict)))
          (are [prefix] (contains? markov-dict prefix)
            "あたし" "は" "プログラム" "の" "女の子"))))))

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
            (is (= result doubled))))

        (testing "名詞の無い発言は学習しない"
          (let [parts (unmo.morph/analyze "学んではいけません")
                result-dictionary (study-template dictionary parts)]
            (is (= dictionary result-dictionary))
            (is (not (contains? (:template result-dictionary) 0)))))))))

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
