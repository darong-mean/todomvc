(ns todocljs.app
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [dispatch dispatch-sync
                                   register-handler subscribe
                                   register-sub
                                   path debug]]))

;; MODEL ----------------------

(def default-db {:tasks {}})

(register-sub
  :model                                                    ;; usage:  (subscribe [:tasks])
  (fn [db _]
    (reaction {:tasks (when (seq (:tasks @db)) (vals (:tasks @db)))})))

;; UPDATE ---------------------

; UTILS ------------------
(def tasks-middle-ware [(path :tasks) debug])
(defn next-id
  [todos]
  ((fnil inc 0) (last (keys todos))))

; HANDLERS ---------------
(register-handler                                           ;; disptached to on app startup
  :initialise-db                                            ;; event id being handled
  (fn [_ _]                                                 ;; the handler
    default-db))
(register-handler :add-todo tasks-middle-ware
  (fn [db [_ text]]
    (let [id (next-id db)]
      (assoc db id {:id id :text text :done false}))))

;; VIEWS ----------------------

(defn view-header-input [{:keys [on-save]}]
  (let [val  (reagent/atom "")
        stop #(reset! val "")
        save #(let [v (-> @val str clojure.string/trim)]
               (if-not (empty? v) (on-save v))
               (stop))]
    (fn [props]
      [:input.new-todo (merge props
                         {; :autofocus true ; autofocus attribute not support yet?
                          :type        "text"
                          :value       @val
                          :on-blur     save
                          :on-change   #(reset! val (-> % .-target .-value))
                          :on-key-down #(case (.-which %)
                                         13 (save)            ; key ENTER
                                         27 (stop)            ; key ESCAPE
                                         nil)
                          })])))

;; This section should be hidden by default and shown when there are todos
(defn view-main [tasks]
  (when (seq tasks)
    [:section.main
     [:input.toggle-all {:type "checkbox"}]
     [:label {:for "toggle-all"} "Mark all as complete"]
     [:ul.todo-list
      ; List items should get the class `editing` when editing and `completed` when marked as completed
      (for [task tasks] ^{:key (:id task)}
        [:li {:class (when (:done task) "completed")}
         [:div.view
          [:input.toggle {:type "checkbox" :checked (:done task)}]
          [:label (:text task)]
          [:button.destroy]]
         [:input.edit {:value "Create a Todo MVC Template"}]])]]))

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

(defn view-container [dispatch model]
  [:section.todoapp
   [:header.header
    [:h1 "todos"]
    [view-header-input {:placeholder "What's need to be done?"
                        :on-save #(dispatch [:add-todo %])}]]
   [view-main (:tasks model)]
   [view-footer (:tasks model)]])

;; MAIN ---------------------
(defn main []
  (let [model (subscribe [:model])]
    (fn [] [view-container dispatch @model])))

(defn start-application []
  (reagent/render-component [main]
    (.getElementById js/document "app")))

(set! (.-onload js/window) #(do (dispatch-sync [:initialise-db])
                                (start-application)))
