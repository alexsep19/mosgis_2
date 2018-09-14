define ([], function () {

    var form_name = 'mgmt_contract_object_common_form'    

    $_DO.cancel_mgmt_contract_object_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'contract_objects'}, {}, function (data) {

            data.__read_only = true

            data.item.startdate = dt_dmy (data.item.startdate)
            data.item.enddate = dt_dmy (data.item.enddate)
            
            $_F5 (data)

        })

    }
    
    $_DO.edit_mgmt_contract_object_common = function (e) {

        var data = {item: w2ui [form_name].record}

//        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_mgmt_contract_object_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
        
        query ({type: 'contract_objects', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_mgmt_contract_object_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'contract_objects', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.undelete_mgmt_contract_object_common = function (e) {   
        if (!confirm ('Восстановить эту запись, Вы уверены?')) return        
        query ({type: 'contract_objects', action: 'undelete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_mgmt_contract_object_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('mgmt_contract_object_common.active_tab', name)
            
        use.block (name)        
    
    }
    
    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('mgmt_contract_object_common.active_tab') || 'mgmt_contract_object_common_services'

        data.__read_only = 1

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']
        
        var it = data.item
        
        it.status_label     = data.vc_gis_status [it.id_ctr_status]
        if (it.id_ctr_status != it.id_ctr_status_gis) it.gis_status_label = data.vc_gis_status [it.id_ctr_status_gis]
        
        data.item.startdate = dt_dmy (data.item.startdate)
        data.item.enddate = dt_dmy (data.item.enddate)
        
        query ({type: "contract_docs", id: undefined}, {search: [
        
            {field: "uuid_contract", operator: "is", value: data.item.uuid_contract},
            {field: "id_type",       operator: "is", value: 1},
            
        ]}, function (d) {
        
            var a = [{id: "", text: "Текущий договор управления"}]
            $.each (d.tb_contract_files, function () {
                a.push ({id: this.id, text: "Доп. соглашение от " + dt_dmy (this.agreementdate) + " №" + this.agreementnumber})
            })            
            data.agreements = a
            
            done (data)

        })                              
        
    }

})