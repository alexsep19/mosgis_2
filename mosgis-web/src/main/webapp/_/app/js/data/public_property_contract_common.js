define ([], function () {

    var form_name = 'public_property_contract_common_form'

    $_DO.cancel_public_property_contract_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'public_property_contracts'}, {}, function (data) {

            data.__read_only = true

            var it = data.item
            
            if (it.isgratuitousbasis == 0 && it.other) it.isgratuitousbasis = -1            

            $_F5 (data)

        })

    }
    
    $_DO.edit_public_property_contract_common = function (e) {

        var data = {item: w2ui [form_name].record}

//        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_public_property_contract_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
        
        if (!v.contractnumber) die ('contractnumber', 'Вы забыли указать номер договора')
        if (!v.date_) die ('date_', 'Вы забыли указать дату договора')
        if (v.date_ > new Date ().toISOString ()) die ('date_', 'Дата договора не может находиться в будущем')
        
        if (!v.startdate) die ('startdate', 'Вы забыли указать дату начала действия договора')
        if (!v.enddate) die ('enddate', 'Вы забыли указать предполагаемую дату окончания действия договора')

        if (v.startdate > v.enddate) die ('enddate', 'Дата окончания не может предшествовать дате начала')
                
        if (v.isgratuitousbasis == -1) {
            if (!v.other) die ('other', 'Укажите, пожалуйста, режим оплаты')
        }
        else {
            v.other = ''
        }
                
        if (v.isgratuitousbasis == 0) {
        
            if (!v.ddt_start) die ('ddt_start', 'Укажите, пожалуйста, дату начала периода оплаты')
            if (!v.ddt_end) die ('ddt_end', 'Укажите, пожалуйста, дату окончания периода оплаты')
            
            if (v.ddt_start_nxt > v.ddt_end_nxt) {
                die ('ddt_end_nxt', 'Окончание срока не может предшествовать его началу')
            }        
            else if (v.ddt_start_nxt == v.ddt_end_nxt) {
                if (v.ddt_start > v.ddt_end) die ('ddt_end', 'Окончание срока не может предшествовать его началу')
            }
            else {
                if (v.ddt_start < v.ddt_end) die ('ddt_end', 'Период оплаты указан некорректно: обнаружено пересечение периодов')
            }
            
        }
        else {
            v.ddt_start = null
            v.ddt_end = null
        }        
        
        if (v.isgratuitousbasis != 1) {
        
            if (!(parseFloat (v.payment) >= 0.01)) die ('payment', 'Укажите, пожалуйста, корректный размер платы')
            
            v.isgratuitousbasis = 0

        }
        
        query ({type: 'public_property_contracts', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_public_property_contract_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'public_property_contracts', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.approve_public_property_contract_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'public_property_contracts', action: 'approve'}, {}, reload_page)
    }    
    
    $_DO.alter_public_property_contract_common = function (e) {
        if (!confirm ('Открыть эту карточку на редактирование?')) return
        query ({type: 'public_property_contracts', action: 'alter'}, {data: {}}, reload_page)
    }
    
    $_DO.annul_public_property_contract_common = function (e) {
        use.block ('public_property_contract_annul_popup')
    }    

    $_DO.choose_tab_public_property_contract_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('public_property_contract_common.active_tab', name)
            
        use.block (name)        
    
    }
    
    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('public_property_contract_common.active_tab') || 'public_property_contract_common_log'

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
        
        if (it.id_ctr_status_gis == 110) it.is_annuled = 1
        
        if (it.isgratuitousbasis == 0 && it.other) it.isgratuitousbasis = -1
        
        done (data)
        
    }

})