(ns unmo.responder)

(defmulti response :responder)

(defmethod response :what [{:keys [input] :as params}]
  (->> (str input "ってなに？")
       (assoc params :response)))

(defmethod response :default [{:keys [responder]}]
  (throw (IllegalArgumentException.
          (str "Responder " responder " が存在しません。"))))
