(ns unmo.responder)

(defmulti response
  "渡された発言オブジェクトに対する返答を :response キーに設定して返す。どの思考エンジンが使用されるかは :responder キーの値で変わる。"
  :responder)

(defmethod
  ^{:doc "WhatResponderは入力 :input に対し、常に 'inputってなに？' と返す。"}
  response :what [{:keys [input] :as params}]
  (->> (str input "ってなに？")
       (assoc params :response)))

(defmethod
  ^{:doc "Responderの指定がない、もしくは存在しないResponderを指定された場合、IllegalArgumentException例外を投げる。"}
  response :default [{:keys [responder]}]
  (throw (IllegalArgumentException.
          (str "Responder " responder " が存在しません。"))))
