define ([], function () {

    var form_name = 'msp_decision_base_common_form'
    
    $_DO.cancel_msp_decision_base_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'msp_decision_bases'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_msp_decision_base_common = function (e) {

        var data = {item: w2ui [form_name].record}

        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_msp_decision_base_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
                                
        if (!v.decisionname)                   die ('decisionname', 'Укажите, пожалуйста, наименование')
        if (!v.code_vc_nsi_301)                die ('code_vc_nsi_301', 'Укажите, пожалуйста, тип')

        query ({type: 'msp_decision_bases', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_msp_decision_base_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'msp_decision_bases', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.undelete_msp_decision_base_common = function (e) {   
        if (!confirm ('Восстановить эту запись, Вы уверены?')) return        
        query ({type: 'msp_decision_bases', action: 'undelete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_msp_decision_base_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('msp_decision_base_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('msp_decision_base_common.active_tab') || 'msp_decision_base_common_log'

        data.__read_only = 1

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']
        
        data.item.status_label = data.vc_gis_status [data.item.id_ctr_status]
        data.item.err_text = data.item ['out_soap.err_text']
        
        data.item._can = $_USER.role.admin /*|| data.item.id_status == 10*/ ? {} : {
            edit: 1 - data.item.is_deleted,
            update: 1,
            cancel: 1,
            delete: 1 - data.item.is_deleted,
//            undelete: data.item.is_deleted,
        }

        done (data)
        
    }

})