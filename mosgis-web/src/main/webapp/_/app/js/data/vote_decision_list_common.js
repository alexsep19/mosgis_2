define ([], function () {

    var form_name = 'vote_decision_list_common_form'

    $_DO.cancel_vote_decision_list_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'vote_decision_lists'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data, true)

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
        else if (!v['agree'].match(/^[0-9]*\.?[0-9]+$/)) die ('agree', 'Введено неверное количество голосов "За"')

        if (!v.against){
            v['against'] = 0
        }
        else if (!v['against'].match(/^[0-9]*\.?[0-9]+$/)) die ('against', 'Введено неверное количество голосов "Против"')

        if (!v.abstent){
            v['abstent'] = 0
        }
        else if (!v['abstent'].match(/^[0-9]*\.?[0-9]+$/)) die ('abstent', 'Введено неверное количество голосов "Воздержалось"')

        if (!v.votingresume) die ('votingresume', 'Пожалуйста, укажите итог голосования')

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

        function Permissions (data) {

            if (!data.item.is_deleted && (data.item['protocol.id_prtcl_status_gis'] == 10 || data.item['protocol.id_prtcl_status_gis'] == 11)) {

                if ($_USER.role.admin) return true

                if (data.cach) {

                    return ($_USER.role.nsi_20_1 ||
                            $_USER.role.nsi_20_19 ||
                            $_USER.role.nsi_20_20 ||
                            $_USER.role.nsi_20_21 ||
                            $_USER.role.nsi_20_22) &&
                            data.cach.is_own &&
                            $_USER.uuid_org == data.cach['org.uuid']

                }

                return $_USER.role.nsi_20_8 && $_USER.role['oktmo_' + data.item['fias.oktmo']]
            }

            return false
        }

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('vote_decision_list_common.active_tab') || 'vote_decision_list_common_log'

        data.__read_only = 1

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']

        permissions = Permissions (data)

        data.item._can = {
            edit: permissions,
            update: permissions,
            cancel: 1,
            delete: permissions,
        }

        done (data)
        
    }

})