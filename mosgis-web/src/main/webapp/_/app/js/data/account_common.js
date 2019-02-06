define ([], function () {

    var form_name = 'account_common_form'
    
    $_DO.open_orgs_account_common = function (e) {
    
        var f = w2ui [form_name]
            
        function done () {
            f.refresh ()
        }

        $('body').data ('voc_organizations_popup.callback', function (r) {
            
            if (!r) return done ()
            
            f.record.uuid_org_customer = r.uuid
            f.record.label_org_customer = r.label                    
            done ()

        })

        use.block ('voc_organizations_popup')
    
    }    
    
    $_DO.cancel_account_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'accounts'}, {}, function (data) {

            data.__read_only = true
            
            var it = data.item
        
            it.org_label = it ['org.label']           
            if (it.uuid_org_customer) it.label_org_customer = it ['org_customer.label']

            $_F5 (data)

        })

    }
    
    $_DO.edit_account_common = function (e) {

        var data = {item: w2ui [form_name].record}

        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_account_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
        
        switch (v.isaccountsdivided) {
            case 0: case 1: break
            default: v.isaccountsdivided = null
        }

        switch (v.isrenter) {
            case 0: case 1: break
            default: v.isrenter = null
        }

        if (!(v.totalsquare >= 0.01)) die ('totalsquare', 'Укажите, пожалуйста, корректный размер общей площади')
        if (parseFloat (v.residentialsquare) > v.totalsquare) die ('residentialsquare', 'Жилая площадь не может превышать общую')
        if (parseFloat (v.heatedarea) > v.totalsquare) die ('heatedarea', 'Отапливаемая площадь не может превышать общую')
        
        query ({type: 'accounts', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_account_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'accounts', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_account_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('account_common.active_tab', name)
            
        use.block (name)        
    
    }

    function fix (it) {

        it.status_label = $('body').data ('data').vc_gis_status [it.id_ctr_status]            
        it.org_label = it ['org.label']
        
        if (it.isaccountsdivided == null) it.isaccountsdivided = -1
        if (it.isrenter == null) it.isrenter = -1
        
        if (it.uuid_org_customer) it.label_org_customer = it ['org_customer.label']
        
        if (it.uuid_contract) {
            it.label_reason = 'Договор управления №' + it ['ca.docnum'] + ' от '  + dt_dmy (it ['ca.signingdate'])
            it.url_reason = '/mgmt_contract/' + it.uuid_contract
        } 

    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('account_common.active_tab') || 'account_common_log'

        data.__read_only = 1
        
        var it = data.item
        
        fix (it)

/*        
        data.item.status_label = data.vc_async_entity_states [data.item.id_status]
        data.item.err_text = data.item ['out_soap.err_text']
*/        
        it._can = $_USER.role.admin /*|| data.item.id_status == 10*/ ? {} : {
            edit: 1 - data.item.is_deleted,
            update: 1,
            cancel: 1,
            delete: 1 - data.item.is_deleted,
        }

        done (data)
        
    }

})