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
        
        if (it.uuid_charter) {
            it.label_reason = 'Устав от '  + dt_dmy (it ['ch.date_'])
            it.url_reason = '/charter/' + it.uuid_charter
        } 
        
        it.persons = []
        if (it.uuid_person_customer) {        
            it.persons.push ({
                id: it.uuid_person_customer,
                text: it ['ind_customer.label']
            })            
        }

    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('account_common.active_tab') || 'account_common_log'

        data.__read_only = 1
        
        var it = data.item
        
        fix (it)
        
        it._can = {cancel: 1}
        
        if (!it.is_deleted && it.uuid_org == $_USER.uuid_org) {
        
            switch (it ['ca.id_ctr_status'] || it ['ch.id_ctr_status']) {
            
                    case 40:
                    case 42:
                    case 43:
                    case 34:
                    case 11:
                    case 92:
                    case 93:
                    case 94:
                    case 100:
                        it._can.edit = 1
                        
            }            
                    
            if (it._can.edit) {
            
                it._can.update = 1
            
                switch (it.id_ctr_status) {
                    case 10:
                    case 14:
                        it._can.delete = 1
                }
            
            }
        
        }
        
        done (data)
        
    }

})