(ns unmo.responder-test
  (:require [clojure.test :refer :all]
            [unmo.responder :refer :all]
            [unmo.morph :refer [analyze]]))

(deftest markov-responder-test
  (testing "MarkovResponderは"
    (let [study-markov #'unmo.dictionary/study-markov]
      (testing "形態素解析結果partsを受け取り"
        (let [dictionary (study-markov {} (analyze "あたしはプログラムの女の子です"))
              parts (analyze "あたしが好きなのはおしゃべりと月餅です")
              params {:responder :markov :parts parts :dictionary dictionary}
              result (response params)
              res (get result :response)]
          (testing "単語数30個までの文章を生成する"
            (is (>= 30 (-> res (analyze) (count)))))

          (testing "文頭の単語が辞書にある場合"
            (testing "その単語から始まる文章を返す"
              (is (clojure.string/starts-with? res "あたし"))))

          (testing "文頭の単語が辞書にない場合"
            (let [parts (analyze "まったく関係のない文章")
                  params {:responder :markov :parts parts :dictionary dictionary}
                  result (response params)
                  res (get result :response)]
              (testing "文頭になりうるランダムな単語から文章を生成する"
                (is (clojure.string/starts-with? res "あたし"))))))))

    (testing "辞書が空の場合"
      (let [dictionary {}
            parts (analyze "テスト")
            params {:responder :markov :parts parts}
            result (response params)]
        (testing ":errorを設定して返す"
          (is (contains? result :error)))

        (testing ":error :typeに:dictionary-emptyを設定する"
          (is (= :dictionary-empty (get-in result [:error :type]))))

        (testing ":error :messageに詳細メッセージを設定する"
          (is (clojure.string/includes? (get-in result [:error :message])
                                        "辞書が空")))))))

(deftest template-responder-test
  (testing "TemplateResponderは"
    (let [dictionary {:template {1 ["あたしは%noun%です" "あなたは%noun%です"]
                                 2 ["%noun%の%noun%は？"]}}]
      (testing "発言の名詞の数に一致するテンプレートがある場合"
        (let [parts (analyze "晴れた天気だ")
              param {:responder :template :parts parts :dictionary dictionary}]
          (testing "テンプレートのいずれかをランダムで選択し、%noun%を発言の名詞に置き換える"
            (let [result (:response (response param))]
              (is (or (= "あたしは天気です" result)
                      (= "あなたは天気です" result)))))

          (testing "テンプレートの複数の%noun%を発言の名詞にランダムで置き換える"
            (let [parts (analyze "今日は天気がいいな")
                  param (assoc param :parts parts)
                  result (:response (response param))]
              (is (or (= "天気の今日は？" result)
                      (= "今日の天気は？" result)))))))

      (testing "発言の名詞の数に一致するテンプレートが無い場合"
        (let [parts (analyze "あたしはプログラムの女の子で、好きな食べ物は月餅です")
              param {:responder :template :parts parts :dictionary dictionary}]
          (testing ":errorを設定して返す"
            (is (contains? (response param) :error)))

          (testing ":error :messageに詳細メッセージを設定する"
            (is (clojure.string/includes? (get-in (response param) [:error :message])
                                          "テンプレートがありません")))

          (testing ":error :typeに:no-matchを設定する"
            (is (= :no-match (get-in (response param) [:error :type]))))))

      (testing "入力に名詞が見つからなかった場合"
        (let [parts (analyze "こんにちは")
              param {:responder :template :parts parts :dictionary dictionary}
              res   (response param)]
          (testing ":errorを設定して返す"
            (is (contains? res :error)))

          (testing ":error :messageに詳細メッセージを設定する"
            (is (clojure.string/includes? (get-in res [:error :message])
                                          "テンプレートがありません")))

          (testing ":error :typeに:no-matchを設定する"
            (is (= :no-match (get-in res [:error :type])))))))))

(deftest pattern-responder-test
  (testing "PatternResponderは"
    (testing "マッチするパターンを見つけ、"
      (let [dictionary {:pattern {"チョコ(レート)?" ["%match%おいしいよね！"]
                                  "天気"          ["明日晴れるといいなー"]}}
            base-param {:responder :pattern :dictionary dictionary}]
        (testing "候補の中からランダムなものを返す"
          (let [param (assoc base-param :input "今日は天気がいい")
                res   (response param)
                text  (:response res)]
            (is (= "明日晴れるといいなー" text))))

        (testing "%match%を正規表現でマッチしたパターンに置き換えて返す"
          (is (= "チョコおいしいよね！"
                 (-> base-param (assoc :input "チョコ食べたい") (response) (:response))))
          (is (= "チョコレートおいしいよね！"
                 (-> base-param (assoc :input "チョコレート食べたい") (response) (:response)))))))

    (testing "マッチするパターンがなかった場合"
      (let [param {:input "nothing" :dictionary {} :responder :pattern}]
        (testing ":errorを設定して返す"
          (is (contains? (response param) :error)))

        (testing ":error :messageに詳細メッセージを設定する"
          (is (clojure.string/includes? "パターンがありません"
                                        (get-in (response param) [:error :message]))))

        (testing ":error :typeに:no-matchを設定する"
          (is (= :no-match (get-in (response param) [:error :type]))))))))

(deftest random-responder-test
  (testing "RandomResponderは"
    (testing "ランダム辞書から無作為な値を返す"
      (let [dic {:random ["a" "b" "c"]}
            param {:responder :random :dictionary dic}
            res (response param)]
        (is (some #{(:response res)} (:random dic)))))

    (testing "ランダム辞書が空だった場合"
      (testing ":error :messageを設定して返す"
        (let [dictionary {:random []}
              param {:responder :random :dictionary dictionary}
              res (response param)]
          (is (contains? res :error))
          (is (clojure.string/includes? (get-in res [:error :message]) "ランダム辞書が空です"))))

      (testing ":error :typeに:fatalを設定する"
        (let [dictionary {:random []}
              param {:responder :random :dictionary dictionary}
              res (response param)]
          (is (= :fatal (get-in res [:error :type]))))))))

(deftest what-responder-test
  (testing "WhatResponderは"
    (testing "発言を表現するMapオブジェクトを返す"
      (let [res (response {:responder :what :input "test"})]
        (are [k] (contains? res k)
          :responder
          :input
          :response)))

    (testing "入力 s に対し、常に \"sってなに？\" と返す"
      (let [result {:responder :what :input "テスト" :response "テストってなに？"}]
        (is (= result
               (response {:responder :what :input "テスト"})))))))

(deftest default-responder-test
  (testing "Responderを指定しなかった場合、"
    (testing "例外IllegalArgumentExceptionを投げる"
      (is (thrown-with-msg? IllegalArgumentException #"存在しません"
                            (response {})))))

  (testing "存在しないResponderを指定した場合、"
    (testing "例外IllegalArgumentExceptionを投げる"
      (is (thrown-with-msg? IllegalArgumentException #"存在しません"
                            (response {:responder :not-found}))))))
