(ns todocljs.app
  (:require [reagent.core :as reagent :refer [atom]]))

;; MODEL ----------------------
;; UPDATE ---------------------
;; VIEWS ----------------------

(defn view-header []
  [:header.header
   [:h1 "todos"]
   [:input.new-todo {; :autofocus true ; autofocus attribute not support yet?
                     :type "text" :placeholder "What's need to be done?"
                     }]])

;; This section should be hidden by default and shown when there are todos
(defn view-main []
  [:section.main
   [:input.toggle-all {:type "checkbox"}]
   [:label {:for "toggle-all"} "Mark all as complete"]
   [:ul.todo-list
    ; List items should get the class `editing` when editing and `completed` when marked as completed
    [:li.completed
     [:div.view
      [:input.toggle {:type "checkbox" :checked true}]
      [:label "Taste Javascript"]
      [:button.destroy]]
     [:input.edit {:value "Create a Todo MVC Template"}]]
    [:li
     [:div.view
      [:input.toggle {:type "checkbox"}]
      [:label "Buy a unicorn"]
      [:button.destroy]]
     [:input.edit {:value "Rule the web"}]]]])

;; This footer should hidden by default and shown when there are todos
(defn view-footer []
  [:footer.footer
   [:span.todo-count [:strong 0] " item left"] ; This should be `0 items left` by default
   [:ul.filters                                ; Remove this if you don't implement routing
    [:li [:a.selected {:href "#"} "All"]]
    [:li [:a {:href "#"} "Active"]]
    [:li [:a {:href "#"} "Completed"]]]
   [:button.clear-completed "Clear completed"] ; Hidden if no completed items are left
   ])

(defn view-container []
  [:section.todoapp
   [view-header]
   [view-main]
   [view-footer]])

;; MAIN ---------------------
(defn main []
  (let []
    (fn [] [view-container])))

(defn start-application []
  (reagent/render-component [view-container]
    (.getElementById js/document "app")))

(set! (.-onload js/window) start-application)
