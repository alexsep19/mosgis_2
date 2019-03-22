define ([], function () {

    var form_name = 'general_needs_municipal_resource_common_form'
    
    $_DO.cancel_general_needs_municipal_resource_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'general_needs_municipal_resources'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_general_needs_municipal_resource_common = function (e) {

        var data = {item: w2ui [form_name].record}

        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_general_needs_municipal_resource_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
                                
        if (!v.parentcode)                   die ('parentcode', 'Укажите, пожалуйста, раздел')
        if (!v.generalmunicipalresourcename) die ('generalmunicipalresourcename', 'Укажите, пожалуйста, наименование')
        if (!v.code_vc_nsi_2)                die ('code_vc_nsi_2', 'Укажите, пожалуйста, вид коммунального ресурса')
        if (!v.okei)                         die ('okei', 'Укажите, пожалуйста, единицы измерения')

        query ({type: 'general_needs_municipal_resources', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_general_needs_municipal_resource_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'general_needs_municipal_resources', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.undelete_general_needs_municipal_resource_common = function (e) {   
        if (!confirm ('Восстановить эту запись, Вы уверены?')) return        
        query ({type: 'general_needs_municipal_resources', action: 'undelete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_general_needs_municipal_resource_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('general_needs_municipal_resource_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('general_needs_municipal_resource_common.active_tab') || 'general_needs_municipal_resource_common_log'

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