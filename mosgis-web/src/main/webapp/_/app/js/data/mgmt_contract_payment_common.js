define ([], function () {

    var form_name = 'mgmt_contract_payment_common_form'    

    $_DO.cancel_mgmt_contract_payment_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'contract_payments'}, {}, function (data) {

            data.__read_only = true

            var it = data.item            
            
            if (it.uuid_file) it.uuid_voting_protocol = '-' + it.uuid_file            
            
            $_F5 (data)

        })

    }
    
    $_DO.edit_mgmt_contract_payment_common = function (e) {

        var data = {item: w2ui [form_name].record}

//        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_mgmt_contract_payment_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
        
        if (/^-/.test (v.uuid_voting_protocol)) {
            v.uuid_file = v.uuid_voting_protocol
            v.uuid_voting_protocol = ""
        }
        else {
            v.uuid_file = ""
        }
        
        query ({type: 'contract_payments', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_mgmt_contract_payment_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'contract_payments', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.download_mgmt_contract_payment_common = function (e) {   
    
        var box = w2ui [form_name].box

        function label (cur, max) {return String (Math.round (100 * cur / max)) + '%'}

        w2utils.lock (box, label (0, 1))

        download ({

            type:   'contract_payment_docs', 
            id:     $('body').data ('data').item.uuid_file,
            action: 'download',

        }, {}, {

            onprogress: function (cur, max) {$('.w2ui-lock-msg').html ('<br><br>' + label (cur, max))},

            onload: function () {w2utils.unlock (box)},

        })

    }
        
    $_DO.annul_mgmt_contract_payment_common = function (e) {   
        use.block ('mgmt_contract_payment_annul_popup');
    }
    
    $_DO.create_house_mgmt_contract_payment_common = function (e) {
        use.block ('mgmt_contract_payment_new_house_passport');
    }
    
    $_DO.choose_tab_mgmt_contract_payment_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('mgmt_contract_payment_common.active_tab', name)
            
        use.block (name)        
    
    }
    
    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('mgmt_contract_payment_common.active_tab') || 'mgmt_contract_payment_common_service_payments'

        data.__read_only = 1
/*
        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']
        
        var it = data.item
        
        it.status_label     = data.vc_gis_status [it.id_ctr_status]
        if (it.id_ctr_status != it.id_ctr_status_gis) it.gis_status_label = data.vc_gis_status [it.id_ctr_status_gis]
*/                
            
        done (data)
        
    }

})