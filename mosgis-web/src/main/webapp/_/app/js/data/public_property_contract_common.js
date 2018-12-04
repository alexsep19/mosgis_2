define ([], function () {

    var form_name = 'public_property_contract_common_form'
/*    
    $_DO.download_public_property_contract_common = function (e) {    
        
        var box = $('body')

        function label (cur, max) {return String (Math.round (100 * cur / max)) + '%'}

        w2utils.lock (box, label (0, 1))

        download ({

            type:   'contract_docs', 
            id:     $('body').data ('data').item.last_termination.file.uuid,
            action: 'download',

        }, {}, {

            onprogress: function (cur, max) {$('.w2ui-lock-msg').html ('<br><br>' + label (cur, max))},

            onload: function () {w2utils.unlock (box)},

        })
    
    }       
*/
    $_DO.cancel_public_property_contract_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'public_property_contracts'}, {}, function (data) {

            data.__read_only = true

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
        
        query ({type: 'public_property_contracts', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_public_property_contract_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'public_property_contracts', action: 'delete'}, {}, reload_page)
    }
/*    
    $_DO.approve_public_property_contract_common = function (e) {
        if (!confirm ('Утвердить этот договор, Вы уверены?\n\n(дальнейшая правка станет невозможна)')) return        
        query ({type: 'public_property_contracts', action: 'approve'}, {}, reload_page)
    }
    
    $_DO.alter_public_property_contract_common = function (e) {
        if (!confirm ('Открыть этот договор на редактирование, Вы уверены?')) return
        query ({type: 'public_property_contracts', action: 'alter'}, {}, reload_page)
    }
    
    $_DO.refresh_public_property_contract_common = function (e) {
        if (!confirm ('Послать в ГИС ЖКХ запрос на обновление статуса этого договора?')) return
        query ({type: 'public_property_contracts', action: 'refresh'}, {}, reload_page)
    }
    
    $_DO.reload_public_property_contract_common = function (e) {
        if (!confirm ('Все изменения, не переданные в ГИС ЖКХ, будут потеряны. Вы действительно хотите обновить данные из ГИС ЖКХ?')) return
        query ({type: 'public_property_contracts', action: 'reload'}, {}, reload_page)
    }

    $_DO.undelete_public_property_contract_common = function (e) {   
        if (!confirm ('Восстановить эту запись, Вы уверены?')) return        
        query ({type: 'public_property_contracts', action: 'undelete'}, {}, reload_page)
    }
    
    $_DO.terminate_public_property_contract_common = function (e) {   
        use.block ('public_property_contract_terminate_popup')
    }
    
    $_DO.annul_public_property_contract_common = function (e) {
        use.block ('public_property_contract_annul_popup')
    }
    
    $_DO.rollover_public_property_contract_common = function (e) {
        use.block ('public_property_contract_rollover_popup')
    }
*/    
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

        done (data)
        
    }

})