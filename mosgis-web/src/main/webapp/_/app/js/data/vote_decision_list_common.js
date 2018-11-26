define ([], function () {

    var form_name = 'vote_decision_list_common_form'

    $_DO.cancel_vote_decision_list_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'vote_decision_lists'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_vote_decision_list_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_vote_decision_list_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()

        if (!v.decisiontype_vc_nsi_63) die ('decisiontype_vc_nsi_63', 'Пожалуйста, выберите тип вопроса из списка')
        if (!v.questionname) die ('questionname', 'Пожалуйста, введите вопрос')

        if (v.decisiontype_vc_nsi_63 == '2.1' && !v.formingfund_vc_nsi_241) die ('formingfund_vc_nsi_241', 'Пожалуйста, укажите способ формирования фонда капитального ремонта')
        if (v.decisiontype_vc_nsi_63 == '11.1' && !v.managementtype_vc_nsi_25) die ('managementtype_vc_nsi_25', 'Пожалуйста, укажите способ управления МКД')

        if (!v.agree && !v.against && !v.abstent) die ('agree', 'Пожалуйста, заполните как минимум одно поле результата голосования')

        if (!v.agree) {
            v['agree'] = 0
        }
        else if (!v['agree'].match(/^[0-9].[0-9]*$/)) die ('agree', 'Введено неверное количество голосов "За"')

        if (!v.against){
            v['against'] = 0
        }
        else if (!v['against'].match(/^[0-9].[0-9]*$/)) die ('against', 'Введено неверное количество голосов "Против"')

        if (!v.abstent){
            v['abstent'] = 0
        }
        else if (!v['abstent'].match(/^[0-9].[0-9]*$/)) die ('abstent', 'Введено неверное количество голосов "Воздержалось"')

        if (!v.hasOwnProperty('votingresume')) die ('votingresume', 'Пожалуйста, укажите итог голосования')

        v['votingresume'] = (v['votingresume'] == 0) ? "M" : "N"

        query ({type: 'vote_decision_lists', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_vote_decision_list_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'vote_decision_lists', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.undelete_vote_decision_list_common = function (e) {   
        if (!confirm ('Восстановить эту запись, Вы уверены?')) return        
        query ({type: 'vote_decision_lists', action: 'undelete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_vote_decision_list_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('vote_decision_list_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('vote_decision_list_common.active_tab') || 'vote_decision_list_common_log'

        data.__read_only = 1

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']

        permissions = 0

        if (!data.item.is_deleted && data.cach && data.cach.is_own && data.cach.id_ctr_status_gis == 40) {
            permissions = 1
        }

        data.item._can = $_USER.role.admin ? {} : 
        {
            edit: permissions,
            update: 1,
            cancel: 1,
            delete: permissions,
        }

        done (data)
        
    }

})