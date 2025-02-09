(ns status-im.data-store.activities
  (:require [clojure.set :as set]
            [quo.design-system.colors :as colors]
            [status-im.constants :as constants]
            [status-im.data-store.messages :as messages]
            [status-im.utils.config :as config]))

(defn- rpc->type [{:keys [type name] :as chat}]
  (case type
    constants/activity-center-notification-type-reply
    (assoc chat
           :chat-name name
           :chat-type constants/private-group-chat-type)

    constants/activity-center-notification-type-mention
    (assoc chat
           :chat-type constants/private-group-chat-type
           :chat-name name)

    constants/activity-center-notification-type-private-group-chat
    (assoc chat
           :chat-type constants/private-group-chat-type
           :chat-name name
           :public? false
           :group-chat true)

    constants/activity-center-notification-type-one-to-one-chat
    (assoc chat
           :chat-type constants/one-to-one-chat-type
           :chat-name name
           :public? false
           :group-chat false)

    chat))

(defn <-rpc [item]
  (cond-> (-> item
              rpc->type
              (set/rename-keys {:lastMessage               :last-message
                                :replyMessage              :reply-message
                                :chatId                    :chat-id
                                :contactVerificationStatus :contact-verification-status})
              (update :last-message #(when % (messages/<-rpc %)))
              (update :message #(when % (messages/<-rpc %)))
              (update :reply-message #(when % (messages/<-rpc %)))
              (dissoc :chatId))
    (not config/new-activity-center-enabled?)
    (assoc :color (rand-nth colors/chat-colors))))
