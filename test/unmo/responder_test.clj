(ns unmo.responder-test
  (:require [clojure.test :refer :all]
            [unmo.responder :refer :all]
            [unmo.morph :refer [analyze]]))

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

    (testing "ランダム辞書が空だった場合、:errorキーを設定して返す"
      (let [dic {:random []}
            param {:responder :random}
            res (response param)]
        (is (contains? res :error))
        (is (clojure.string/includes? (get-in res [:error :message]) "ランダム辞書が空です" ))))))

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
