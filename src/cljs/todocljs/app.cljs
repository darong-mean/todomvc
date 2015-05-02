(ns todocljs.app
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [dispatch dispatch-sync
                                   register-handler subscribe
                                   register-sub]]))

;; MODEL ----------------------

(def model
  (reagent/atom {:tasks []}))

(def default-db {:tasks []})

(register-sub
  :model                                                    ;; usage:  (subscribe [:tasks])
  (fn [db _]
    db))

;; UPDATE ---------------------

(register-handler                                           ;; disptached to on app startup
  :initialise-db                                            ;; event id being handled
  (fn [_ _]                                                 ;; the handler
    default-db))

;; VIEWS ----------------------

(defn view-header []
  [:header.header
   [:h1 "todos"]
   [:input.new-todo {; :autofocus true ; autofocus attribute not support yet?
                     :type "text" :placeholder "What's need to be done?"
                     }]])

;; This section should be hidden by default and shown when there are todos
;; This section should be hidden by default and shown when there are todos
(defn view-main [tasks]
  (when (seq tasks)
    [:section.main
     [:input.toggle-all {:type "checkbox"}]
     [:label {:for "toggle-all"} "Mark all as complete"]
     [:ul.todo-list
      ; List items should get the class `editing` when editing and `completed` when marked as completed
      (for [task tasks]
        [:li.completed
         [:div.view
          [:input.toggle {:type "checkbox" :checked true}]
          [:label "Taste Javascript"]
          [:button.destroy]]
         [:input.edit {:value "Create a Todo MVC Template"}]])]]))
;(defn view-main [tasks]
;  [:section.main
;   [:input.toggle-all {:type "checkbox"}]
;   [:label {:for "toggle-all"} "Mark all as complete"]
;   [:ul.todo-list
;    ; List items should get the class `editing` when editing and `completed` when marked as completed
;    [:li.completed
;     [:div.view
;      [:input.toggle {:type "checkbox" :checked true}]
;      [:label "Taste Javascript"]
;      [:button.destroy]]
;     [:input.edit {:value "Create a Todo MVC Template"}]]
;    [:li
;     [:div.view
;      [:input.toggle {:type "checkbox"}]
;      [:label "Buy a unicorn"]
;      [:button.destroy]]
;     [:input.edit {:value "Rule the web"}]]]])

;; This footer should hidden by default and shown when there are todos
(defn view-footer [tasks]
  (when (seq tasks)
    [:footer.footer
     [:span.todo-count [:strong 0] " item left"]            ; This should be `0 items left` by default
     [:ul.filters                                           ; Remove this if you don't implement routing
      [:li [:a.selected {:href "#"} "All"]]
      [:li [:a {:href "#"} "Active"]]
      [:li [:a {:href "#"} "Completed"]]]
     [:button.clear-completed "Clear completed"]            ; Hidden if no completed items are left
     ]))

(defn view-container [model]
  [:section.todoapp
   [view-header]
   [view-main (:tasks model)]
   [view-footer (:tasks model)]])

;; MAIN ---------------------
(defn main []
  (let [model (subscribe [:model])]
    (fn [] [view-container @model])))

(defn start-application []
  (reagent/render-component [main]
    (.getElementById js/document "app")))

(set! (.-onload js/window) #(do (dispatch-sync [:initialise-db])
                                (start-application)))
