define ([], function () {

    var form_name = 'charter_common_form'
    
    $_DO.cancel_charter_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'charters'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_charter_common = function (e) {

        var data = {item: w2ui [form_name].record}

//        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_charter_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
        
        if ($('body').data ('data').item ['vc_orgs.stateregistrationdate']) {
            delete v.date_
        }
        else {
            if (!v.date_) die ('date_', 'Укажите, пожалуйста, дату государственной регистрации')
        }
        
        if (!v.ddt_m_start) die ('ddt_m_start', 'Укажите, пожалуйста, дату начала ввода показаний ПУ')
        if (!v.ddt_m_end) die ('ddt_m_start', 'Укажите, пожалуйста, дату окончания ввода показаний ПУ')
        
        if (v.ddt_m_start_nxt > v.ddt_m_end_nxt) {
            die ('ddt_m_end_nxt', 'Окончание срока не может предшествовать его началу')
        }        
        else if (v.ddt_m_start_nxt == v.ddt_m_end_nxt) {
            if (v.ddt_m_start > v.ddt_m_end) die ('ddt_m_end', 'Окончание срока не может предшествовать его началу')
        }
        else {
            if (v.ddt_m_start < v.ddt_m_end) die ('ddt_m_end', 'Период сдачи показаний по ИПУ указан некорректно: обнаружено пересечение периодов')
        }
                
        if (!v.ddt_d_start) die ('ddt_d_start', 'Укажите, пожалуйста, срок выставления платёжных документов')
        if (!v.ddt_i_start) die ('ddt_i_start', 'Укажите, пожалуйста, срок внесения платы')

        if (v.ddt_d_start_nxt > v.ddt_i_start_nxt) die ('ddt_i_start_nxt', 'Срок выставления платежных документов не должен превышать срок внесения платы')
        if (v.ddt_d_start_nxt == v.ddt_i_start_nxt) {
            if (v.ddt_d_start > v.ddt_i_start) die ('ddt_i_start', 'Срок выставления платежных документов не должен превышать срок внесения платы')
        }
        
        query ({type: 'charters', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_charter_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'charters', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.approve_charter_common = function (e) {
        if (!confirm ('Утвердить этот устав, Вы уверены?\n\n(дальнейшая правка станет невозможна)')) return
        query ({type: 'charters', action: 'approve'}, {}, reload_page)
    }
    
    $_DO.alter_charter_common = function (e) {
        if (!confirm ('Открыть этот устав на редактирование, Вы уверены?')) return
        query ({type: 'charters', action: 'alter'}, {}, reload_page)
    }
    
    $_DO.refresh_charter_common = function (e) {
        if (!confirm ('Послать в ГИС ЖКХ запрос на обновление статуса этого устава?')) return
        query ({type: 'charters', action: 'refresh'}, {}, reload_page)
    }

    $_DO.undelete_charter_common = function (e) {   
        if (!confirm ('Восстановить эту запись, Вы уверены?')) return        
        query ({type: 'charters', action: 'undelete'}, {}, reload_page)
    }
    
    $_DO.terminate_charter_common = function (e) {   
        use.block ('charter_terminate_popup')
    }
    
    $_DO.annul_charter_common = function (e) {
        use.block ('charter_annul_popup')
    }
    
    $_DO.rollover_charter_common = function (e) {
        use.block ('charter_rollover_popup')
    }
    
    $_DO.choose_tab_charter_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('charter_common.active_tab', name)
            
        use.block (name)        
    
    }
    
    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('charter_common.active_tab') || 'charter_common_log'

        data.__read_only = 1

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']
        
        var it = data.item
        
        it.status_label     = data.vc_gis_status [it.id_ctr_status]
        it.state_label      = data.vc_gis_status [it.id_ctr_state]

        if (it.id_ctr_status != 10) {
            if (it.id_ctr_status != it.id_ctr_status_gis) it.gis_status_label = data.vc_gis_status [it.id_ctr_status_gis]
            it.gis_state_label  = data.vc_gis_status [it.id_ctr_state_gis]
        }

        it.err_text = it ['out_soap.err_text']        

        done (data)
        
    }

})